package com.nexa.chat.feature.messaging.ui;

import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.ChatFailure;
import com.nexa.chat.feature.messaging.repository.ConversationRepository;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * TV2 owner of one user/epoch, retained independently of Views; not a Session implementation.
 *
 * Linearization: reservation inserts a unique Ticket under gate. Dispatch then claims that ticket
 * under gate, but calls the executor/repository outside it. Invalidate closes acceptance and removes
 * tickets atomically. Unclaimed work is skipped. A claimed handoff can still reach a repository
 * after invalidation: without transport cancellation no stronger guarantee is possible.
 * Completion checks both scope and attempt identity before mutation. Result/observer delivery is
 * outside gate. Already claimed deliveries may finish; consumers must check their current binding.
 *
 * No repository, executor, observer, caller Runnable or future completion executes under gate.
 * The host injects one account lease from its retained MessagingExecution runtime.
 * Session coordinator must invalidate before exposing a replacement account.
 */
public final class MessagingController {
    private final Object gate;
    private final MessagingExecution.AccountLease execution;
    private final MessagingRouter router;
    private final UUID userId;
    private final UUID epoch;
    private final ConversationRepository repository;
    private final MessagingLogic logic = new MessagingLogic(new HashMap<>());
    private final Map<String, Operation> operations = new LinkedHashMap<>();
    private final Map<Long, Ticket<?>> pending = new LinkedHashMap<>();
    private final Map<String, DirectOperation> pendingDirect = new HashMap<>();
    private final Set<ViewSubscription> observers = new HashSet<>();
    private long nextTicket;
    private boolean active = true;

    public MessagingController(UUID userId, UUID epoch, ConversationRepository repository,
                               MessagingExecution.AccountLease execution) {
        this.userId = Objects.requireNonNull(userId);
        this.epoch = Objects.requireNonNull(epoch);
        this.repository = Objects.requireNonNull(repository);
        this.execution = Objects.requireNonNull(execution);
        this.gate = execution.gate;
        this.router = new MessagingRouter(userId, epoch, gate);
        execution.bind(userId, epoch, this::invalidate);
    }

    public MessagingRouter getRouter() { return router; }

    /**
     * Every command owns an intent, not a repository request. Superseded callers still receive
     * the operation result; only the latest intent can navigate. Each caller owns its future:
     * cancelling/completing that future neither cancels shared work nor controls navigation.
     * Standard future completion runs on this account's reserved settlement workers.
     */
    public CompletionStage<Conversation> openDirectAndNavigate(String peerUserId) {
        return direct(peerUserId, true);
    }

    private CompletionStage<Conversation> direct(String peer, boolean navigate) {
        Objects.requireNonNull(peer);
        DirectOperation operation;
        Ticket<Conversation> caller;
        boolean created;
        synchronized (gate) {
            if (!active) { operation = null; caller = null; created = false; }
            else {
                operation = pendingDirect.get(peer);
                created = operation == null;
                if (!execution.admitLocked(created)) return failed(capacity());
                if (created) {
                    operation = new DirectOperation(peer);
                    operation.resource = execution.operationLocked();
                    pendingDirect.put(peer, operation);
                }
                if (navigate) operation.intent = router.beginDirect();
                caller = reserveLocked(false);
                operation.callers.add(caller);
            }
        }
        if (caller == null) return failed(inactive());
        if (created) dispatchDirect(operation);
        return caller.future;
    }

    private void dispatchDirect(DirectOperation operation) {
        try {
            execution.dispatch(operation.resource, () -> {
                synchronized (gate) {
                    if (!active || pendingDirect.get(operation.peer) != operation || operation.dispatched) return;
                    operation.dispatched = true;
                }
                try {
                    operation.resource.source(() -> repository.openDirect(operation.peer)).whenComplete(
                            (value, error) -> finishDirect(operation, value, error));
                } catch (RuntimeException error) { finishDirect(operation, null, error); }
            }, error -> finishDirect(operation, null, error));
        } catch (RuntimeException error) { finishDirect(operation, null, error); }
    }

    private void finishDirect(DirectOperation operation, Conversation value, Throwable error) {
        if (error == null && value == null) error = new IllegalArgumentException("Missing conversation");
        List<Ticket<Conversation>> callers;
        boolean routeChanged = false;
        synchronized (gate) {
            if (!active || pendingDirect.get(operation.peer) != operation) return;
            pendingDirect.remove(operation.peer);
            if (error == null) routeChanged = router.completeDirectLocked(
                    operation.intent, value.getId(), value.getTitle());
            callers = new ArrayList<>(operation.callers);
            operation.callers.clear();
            for (Ticket<Conversation> ticket : callers) {
                ticket.value = value;
                ticket.error = error;
                ticket.reconciled = true;
            }
        }
        if (routeChanged) router.publish();
        for (Ticket<Conversation> ticket : callers) deliver(ticket);
    }

    public UUID getCurrentUserId() { return userId; }
    public UUID getSessionEpoch() { return epoch; }

    public boolean isActive() {
        synchronized (gate) { return active; }
    }

    public String draft(String conversationId) {
        synchronized (gate) { return active ? logic.draft(conversationId) : ""; }
    }

    public long draftRevision(String conversationId) {
        synchronized (gate) { return active ? logic.revision(conversationId) : 0; }
    }

    public void editDraft(String conversationId, String text) {
        Objects.requireNonNull(conversationId);
        Objects.requireNonNull(text);
        synchronized (gate) {
            if (!active) throw inactive();
            logic.edit(conversationId, text);
        }
    }

    public Throwable lastFailure(String conversationId) {
        FailureInfo info = null;
        synchronized (gate) {
            if (active) {
                for (Operation operation : operations.values()) {
                    if (operation.message.getConversationId().equals(conversationId)) info = operation.failure;
                }
            }
        }
        return info == null ? null : info.asException();
    }

    public boolean isSending(String conversationId) {
        synchronized (gate) { return active && sendingLocked(conversationId); }
    }

    public boolean canRetry(String clientMessageId) {
        synchronized (gate) { return retryEligibleLocked(operations.get(clientMessageId)); }
    }

    public List<OutboxEntry> localEntries(String conversationId) {
        synchronized (gate) {
            List<OutboxEntry> rows = new ArrayList<>();
            if (active) {
                for (Operation operation : operations.values()) {
                    if (operation.message.getConversationId().equals(conversationId)) rows.add(operation.entry);
                }
            }
            return Collections.unmodifiableList(rows);
        }
    }

    public CompletionStage<List<Conversation>> listConversations() {
        return request(resource -> resource.source(repository::listConversations),
                rows -> Collections.unmodifiableList(new ArrayList<>(rows)));
    }

    public CompletionStage<Conversation> openDirect(String peerUserId) {
        return direct(peerUserId, false);
    }

    public CompletionStage<List<OutboxEntry>> loadMessages(String conversationId) {
        return request(resource -> MessagingLogic.load(repository, conversationId, resource),
                rows -> mergeLocalLocked(conversationId, rows));
    }

    public CompletionStage<OutboxEntry> sendDraft(String conversationId) {
        Operation operation = null;
        Ticket<OutboxEntry> ticket = null;
        RuntimeException rejection = null;
        synchronized (gate) {
            if (!active) rejection = inactive();
            else {
                String text = logic.draft(conversationId);
                long revision = logic.revision(conversationId);
                if (text.trim().isEmpty()) rejection = new IllegalArgumentException("Empty draft");
                else if (sendingLocked(conversationId)) rejection = new IllegalStateException("Send in flight");
                else {
                    for (Operation old : operations.values()) {
                        if (old.message.getConversationId().equals(conversationId)
                                && old.draftRevision == revision && old.message.getText().equals(text)) {
                            rejection = new IllegalStateException("Use existing operation and clientMessageId");
                            break;
                        }
                    }
                }
                if (rejection == null) {
                    // Validation and draft snapshot stay atomic with invalidation, before admission.
                    // Preserve the synchronous IllegalArgumentException for an invalid identifier.
                    Message message = new Message(UUID.randomUUID().toString(), conversationId,
                            userId.toString(), MessageType.TEXT, text);
                    if (!execution.admitLocked(true)) rejection = capacity();
                    else {
                        operation = new Operation(message, revision);
                        operations.put(message.getClientMessageId(), operation);
                        ticket = reserveSendLocked(operation, false);
                    }
                }
            }
        }
        if (rejection != null) return failed(rejection);
        dispatchSend(operation, ticket);
        return ticket.future;
    }

    public CompletionStage<OutboxEntry> retry(String clientMessageId) {
        Operation operation;
        Ticket<OutboxEntry> ticket = null;
        RuntimeException rejection = null;
        synchronized (gate) {
            operation = operations.get(clientMessageId);
            if (!active) rejection = inactive();
            else if (!retryEligibleLocked(operation)) rejection = new IllegalStateException("Retry not eligible");
            else if (!execution.admitLocked(true)) rejection = capacity();
            else ticket = reserveSendLocked(operation, true);
        }
        if (rejection != null) return failed(rejection);
        dispatchSend(operation, ticket);
        return ticket.future;
    }

    private Ticket<OutboxEntry> reserveSendLocked(Operation operation, boolean retry) {
        if (!logic.begin(operation.message, retry)) throw new IllegalStateException("Send in flight");
        Ticket<OutboxEntry> ticket = reserveLocked(true);
        operation.attempt = ticket;
        operation.inFlight = true;
        operation.failure = null;
        operation.entry = OutboxEntry.draft(operation.message).transitionTo(OutboxState.PENDING)
                .transitionTo(OutboxState.SENDING);
        return ticket;
    }

    private void dispatchSend(Operation operation, Ticket<OutboxEntry> ticket) {
        executeDispatch(ticket, () -> {
            try {
                ticket.resource.source(() -> repository.send(operation.message)).whenComplete(
                        (entry, error) -> finishSend(operation, ticket, entry, error));
            } catch (RuntimeException error) { finishSend(operation, ticket, null, error); }
        }, error -> finishSend(operation, ticket, null, error));
        publish();
    }

    private void finishSend(Operation operation, Ticket<OutboxEntry> ticket, OutboxEntry entry, Throwable error) {
        if (error == null && (entry == null || !operation.message.samePayload(entry.getMessage())
                || entry.getState() == OutboxState.DRAFT)) {
            error = new IllegalArgumentException("Mismatched or unsubmitted send result");
        }
        FailureInfo failure = FailureInfo.from(error);
        Throwable logicFailure = failure == null ? null : failure.asException();
        synchronized (gate) {
            if (!currentLocked(ticket) || ticket.reconciled || operation.attempt != ticket) return;
            if (operation.entry.getState() == OutboxState.SENT) {
                entry = operation.entry;
                error = null;
                failure = null;
                logicFailure = null;
            }
            operation.inFlight = false;
            operation.attempt = null;
            operation.failure = failure;
            operation.entry = error == null ? entry : operation.entry.transitionTo(OutboxState.FAILED);
            logic.finish(operation.message, operation.draftRevision, entry, logicFailure);
            ticket.value = entry;
            ticket.error = error;
            ticket.reconciled = true;
        }
        publish();
        deliver(ticket);
    }

    private <T, R> CompletionStage<R> request(Function<MessagingExecution.AccountLease.RepositoryOperation, CompletionStage<T>> work, Function<T, R> apply) {
        Ticket<R> ticket;
        synchronized (gate) {
            if (!active) return failed(inactive());
            if (!execution.admitLocked(true)) return failed(capacity());
            ticket = reserveLocked(true);
        }
        executeDispatch(ticket, () -> {
            try {
                work.apply(ticket.resource).whenComplete((value, error) -> finishRequest(ticket, value, error, apply));
            } catch (RuntimeException error) { finishRequest(ticket, null, error, apply); }
        }, error -> finishRequest(ticket, null, error, apply));
        return ticket.future;
    }

    /** apply is an internal state reducer, never a caller-supplied callback. */
    private <T, R> void finishRequest(Ticket<R> ticket, T value, Throwable error, Function<T, R> apply) {
        synchronized (gate) {
            if (!currentLocked(ticket) || ticket.reconciled) return;
            try {
                if (error == null) ticket.value = apply.apply(value);
                ticket.error = error;
            } catch (RuntimeException invalidResult) { ticket.error = invalidResult; }
            ticket.reconciled = true;
        }
        deliver(ticket);
    }

    private <T> Ticket<T> reserveLocked(boolean operation) {
        Ticket<T> ticket = new Ticket<>(++nextTicket);
        if (operation) ticket.resource = execution.operationLocked();
        pending.put(ticket.id, ticket);
        return ticket;
    }

    private boolean currentLocked(Ticket<?> ticket) {
        return active && pending.get(ticket.id) == ticket;
    }

    /** The claim is the handoff boundary, not an atomic transport start/cancel. */
    private boolean claimDispatch(Ticket<?> ticket) {
        synchronized (gate) {
            if (!currentLocked(ticket) || ticket.dispatched) return false;
            ticket.dispatched = true;
            return true;
        }
    }

    private void executeDispatch(Ticket<?> ticket, Runnable dispatch,
                                 java.util.function.Consumer<RuntimeException> rejected) {
        try {
            execution.dispatch(ticket.resource, () -> {
                if (claimDispatch(ticket)) dispatch.run();
            }, rejected);
        } catch (RuntimeException error) { rejected.accept(error); }
    }

    private <T> void deliver(Ticket<T> ticket) {
        synchronized (gate) {
            if (!currentLocked(ticket) || !ticket.reconciled || ticket.queued) return;
            ticket.queued = true;
            execution.readyLocked(ticket);
        }
    }

    /** Idempotent. State closes before any external cancellation delivery runs. */
    public void invalidate() { invalidate(epoch); }

    public void invalidate(UUID expectedEpoch) {
        if (!epoch.equals(expectedEpoch)) return;
        List<Ticket<?>> cancelled;
        List<ViewSubscription> terminal;
        synchronized (gate) {
            if (!active) return;
            active = false;
            router.invalidateLocked();
            cancelled = new ArrayList<>(pending.values());
            pending.clear();
            for (DirectOperation operation : pendingDirect.values()) operation.callers.clear();
            pendingDirect.clear();
            logic.clear();
            operations.clear();
            terminal = new ArrayList<>(observers);
            observers.clear();
            for (ViewSubscription subscription : terminal) {
                subscription.invalidated = true;
                subscription.listener = null;
            }
            for (Ticket<?> ticket : cancelled) {
                ticket.value = null;
                ticket.error = inactive();
                ticket.reconciled = true;
                if (!ticket.queued) {
                    ticket.queued = true;
                    execution.readyLocked(ticket);
                }
            }
            execution.closeLocked();
        }
        // Terminal notices bypass the normal observer drain and reserved settlements.
        // Observer executors must provide nonblocking handoff (the View uses Handler.post).
        for (ViewSubscription subscription : terminal) subscription.signalTerminal();
    }

    /** Atomic registration: a closed scope returns a closed handle, never throws for that race. */
    public ViewSubscription observe(Executor executor, Runnable observer) {
        return observe(executor, observer, null);
    }

    /** Optional terminal callback carries no private data; used only to clear an attached UI. */
    public ViewSubscription observe(Executor executor, Runnable observer, Runnable onInvalidated) {
        Objects.requireNonNull(executor);
        Objects.requireNonNull(observer);
        ViewSubscription subscription = new ViewSubscription(executor, observer, onInvalidated);
        synchronized (gate) {
            if (active) observers.add(subscription);
            else subscription.closeLocked();
        }
        subscription.signal();
        return subscription;
    }

    private void publish() {
        List<ViewSubscription> subscriptions;
        synchronized (gate) { subscriptions = new ArrayList<>(observers); }
        for (ViewSubscription subscription : subscriptions) subscription.signal();
    }

    private boolean sendingLocked(String conversationId) {
        for (Operation operation : operations.values()) {
            if (operation.inFlight && operation.message.getConversationId().equals(conversationId)) return true;
        }
        return false;
    }

    private boolean retryEligibleLocked(Operation operation) {
        return active && operation != null && !sendingLocked(operation.message.getConversationId())
                && logic.canRetry(operation.message) && operation.entry.getState() != OutboxState.SENT;
    }

    private List<OutboxEntry> mergeLocalLocked(String conversationId, List<OutboxEntry> rows) {
        Map<String, OutboxEntry> merged = new LinkedHashMap<>();
        for (OutboxEntry row : rows) {
            Message message = row.getMessage();
            String key = message.getSenderId() + "/" + message.getClientMessageId();
            merged.put(key, row);
            Operation operation = operations.get(message.getClientMessageId());
            if (operation != null && userId.toString().equals(message.getSenderId())) {
                if (!operation.message.samePayload(message)) throw new IllegalArgumentException("Conflicting identity");
                if (row.getState() == OutboxState.SENT && operation.entry.getState() != OutboxState.SENT) {
                    operation.entry = row;
                    operation.failure = null;
                    logic.finish(operation.message, operation.draftRevision, row, null);
                }
            }
        }
        for (Operation operation : operations.values()) {
            Message message = operation.message;
            if (!message.getConversationId().equals(conversationId)) continue;
            String key = message.getSenderId() + "/" + message.getClientMessageId();
            OutboxEntry row = merged.get(key);
            if (row == null || row.getState() != OutboxState.SENT) merged.put(key, operation.entry);
        }
        return Collections.unmodifiableList(new ArrayList<>(merged.values()));
    }

    private static CancellationException inactive() { return new CancellationException("Messaging scope invalidated"); }

    private static <T> CompletionStage<T> failed(Throwable error) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(error);
        return future;
    }

    private static RejectedExecutionException capacity() {
        return new RejectedExecutionException("Messaging account capacity exhausted");
    }

    private final class Ticket<T> implements MessagingExecution.Settlement {
        final long id;
        final CompletableFuture<T> future = new CompletableFuture<>();
        MessagingExecution.AccountLease.RepositoryOperation resource;
        boolean dispatched, reconciled, deliveryClaimed, queued;
        T value;
        Throwable error;
        Ticket(long id) { this.id = id; }
        @Override public void settle() {
            T result;
            Throwable failure;
            synchronized (gate) {
                if (deliveryClaimed) throw new AssertionError("Duplicate terminal settlement");
                deliveryClaimed = true;
                result = value; failure = error;
                pending.remove(id);
                value = null; error = null;
            }
            if (failure == null) future.complete(result);
            else future.completeExceptionally(failure);
        }
    }

    private final class DirectOperation {
        MessagingExecution.AccountLease.RepositoryOperation resource;
        final String peer;
        final List<Ticket<Conversation>> callers = new ArrayList<>();
        long intent = -1;
        boolean dispatched;
        DirectOperation(String peer) { this.peer = peer; }
    }

    private final class Operation {
        final Message message;
        final long draftRevision;
        Ticket<OutboxEntry> attempt;
        OutboxEntry entry;
        boolean inFlight;
        FailureInfo failure;
        Operation(Message message, long draftRevision) {
            this.message = message;
            this.draftRevision = draftRevision;
        }
    }

    /** Retain a code only, not an arbitrary Throwable/cause/transport payload or stack trace. */
    private static final class FailureInfo {
        final ChatFailure.Code code;
        FailureInfo(ChatFailure.Code code) { this.code = code; }
        static FailureInfo from(Throwable error) {
            if (error == null) return null;
            while (error.getCause() != null) error = error.getCause();
            return new FailureInfo(error instanceof ChatFailure ? ((ChatFailure) error).getCode() : null);
        }
        RuntimeException asException() {
            return code == null ? new IllegalStateException("Messaging operation failed") : new ChatFailure(code);
        }
    }

    /**
     * One serialized drain per subscription; notifications are coalesced pull-latest signals,
     * never captured snapshots. A callback claimed before close/invalidate may finish outside gate.
     * Android bindings additionally check attachment/generation/current scope at execution.
     */
    public final class ViewSubscription implements AutoCloseable {
        private Executor executor;
        private Runnable listener;
        private Runnable onInvalidated;
        private boolean invalidated;
        private boolean normalFailed;
        private boolean closed;
        private boolean dirty;
        private boolean scheduled;

        private ViewSubscription(Executor executor, Runnable listener, Runnable onInvalidated) {
            this.executor = executor;
            this.listener = listener;
            this.onInvalidated = onInvalidated;
        }

        public boolean isOpen() {
            synchronized (gate) { return !closed && !invalidated && !normalFailed; }
        }

        private void signalTerminal() {
            Executor delivery;
            synchronized (gate) {
                if (closed) return;
                delivery = executor;
            }
            try {
                delivery.execute(() -> {
                    Runnable terminal;
                    synchronized (gate) {
                        if (closed) return;
                        // Terminal claim and explicit close serialize on gate. Close before claim
                        // suppresses delivery; close after claim cannot recall this detached callback.
                        terminal = onInvalidated;
                        closeLocked();
                    }
                    try { if (terminal != null) terminal.run(); }
                    catch (RuntimeException ignored) { /* detached terminal */ }
                });
            } catch (RuntimeException rejected) { close(); }
        }

        private void signal() {
            Executor delivery;
            synchronized (gate) {
                if (closed || normalFailed || !active) return;
                dirty = true;
                if (scheduled) return;
                scheduled = true;
                delivery = executor;
            }
            try { delivery.execute(this::drain); }
            catch (RuntimeException rejected) { close(); }
        }

        private void drain() {
            while (true) {
                Runnable callback;
                synchronized (gate) {
                    if (closed || normalFailed || invalidated || !active || !dirty) {
                        scheduled = false;
                        return;
                    }
                    dirty = false;
                    callback = listener;
                }
                try { if (callback != null) callback.run(); }
                catch (RuntimeException observerFailure) { failNormal(); return; }
                // Reentrant changes mark dirty; keep draining without recursive inline execute.
            }
        }

        // A failed normal renderer is no longer open for normal signals, but remains terminal-only
        // until explicit detach or terminal claim. Failure is never an implicit detach.
        private void failNormal() {
            synchronized (gate) {
                if (closed) return;
                normalFailed = true;
                listener = null;
                dirty = false;
                scheduled = false;
                if (onInvalidated == null) closeLocked();
            }
        }

        private void closeLocked() {
            closed = true;
            dirty = false;
            listener = null;
            onInvalidated = null;
            executor = null;
            observers.remove(this);
        }

        @Override public void close() {
            synchronized (gate) { closeLocked(); }
        }
    }
}
