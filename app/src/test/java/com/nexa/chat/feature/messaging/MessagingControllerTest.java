package com.nexa.chat.feature.messaging;

import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import com.nexa.chat.feature.messaging.ui.MessagingController;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/** JVM lifecycle tests model detach/recreation by closing/replacing View subscriptions. */
public class MessagingControllerTest {
    private static final UUID USER = UUID.fromString("6f0cbf50-6972-4c77-9d25-01da1e0f6101");
    private final ControlledRepository repository = new ControlledRepository(USER);
    private final MessagingController controller =
            new MessagingController(USER, UUID.randomUUID(), repository);
    private final String conversation = repository.conversation;

    @Test public void detachDoesNotDiscardValidAck() {
        QueueExecutor ui = new QueueExecutor();
        AtomicInteger rendered = new AtomicInteger();
        MessagingController.ViewSubscription oldView = controller.observe(ui, rendered::incrementAndGet);
        ui.drain();
        oldView.close();
        controller.editDraft(conversation, "hello");
        CompletionStage<OutboxEntry> request = controller.sendDraft(conversation);
        repository.ack(0);
        ui.drain();
        assertEquals(1, rendered.get());
        assertTrue(controller.isActive());
        assertEquals(OutboxState.SENT, request.toCompletableFuture().join().getState());
        assertEquals("", controller.draft(conversation));
        assertEquals(OutboxState.SENT, controller.localEntries(conversation).get(0).getState());
    }

    @Test public void recreationRetainsDraftRevisionAndInFlightLock() {
        QueueExecutor ui = new QueueExecutor();
        MessagingController.ViewSubscription oldView = controller.observe(ui, () -> {});
        controller.editDraft(conversation, "hello");
        long revision = controller.draftRevision(conversation);
        controller.sendDraft(conversation);
        oldView.close();
        AtomicInteger newRender = new AtomicInteger();
        MessagingController.ViewSubscription newView = controller.observe(ui, newRender::incrementAndGet);
        ui.drain();
        assertEquals("hello", controller.draft(conversation));
        assertEquals(revision, controller.draftRevision(conversation));
        assertTrue(controller.isSending(conversation));
        assertFailure(controller.sendDraft(conversation), IllegalStateException.class);
        assertEquals(1, repository.sent.size());
        repository.ack(0);
        ui.drain();
        assertFalse(controller.isSending(conversation));
        assertEquals("", controller.draft(conversation));
        assertEquals(2, newRender.get());
        newView.close();
    }

    @Test public void retrySurvivesRecreationAndUsesOriginalMessage() {
        QueueExecutor ui = new QueueExecutor();
        MessagingController.ViewSubscription oldView = controller.observe(ui, () -> {});
        controller.editDraft(conversation, "hello");
        CompletionStage<OutboxEntry> first = controller.sendDraft(conversation);
        repository.fail(0, ChatFailure.Code.TIMEOUT);
        assertFailure(first, ChatFailure.Code.TIMEOUT);
        Message original = repository.sent.get(0);
        oldView.close();
        MessagingController.ViewSubscription newView = controller.observe(ui, () -> {});
        ui.drain();
        assertTrue(controller.canRetry(original.getClientMessageId()));
        CompletionStage<OutboxEntry> retry = controller.retry(original.getClientMessageId());
        assertSame(original, repository.sent.get(1));
        assertFailure(controller.retry(original.getClientMessageId()), IllegalStateException.class);
        assertFailure(controller.sendDraft(conversation), IllegalStateException.class);
        assertEquals(2, repository.sent.size());
        repository.ack(1);
        assertEquals(OutboxState.SENT, retry.toCompletableFuture().join().getState());
        assertFalse(controller.canRetry(original.getClientMessageId()));
        newView.close();
    }

    @Test public void oldAckCannotClearNewDraft() {
        controller.editDraft(conversation, "hello");
        controller.sendDraft(conversation);
        controller.editDraft(conversation, "new draft");
        long revision = controller.draftRevision(conversation);
        repository.ack(0);
        assertEquals("new draft", controller.draft(conversation));
        assertEquals(revision, controller.draftRevision(conversation));
    }

    @Test public void retryKeepsOriginalRevisionAndPayloadWhenNewDraftExists() {
        controller.editDraft(conversation, "hello");
        controller.sendDraft(conversation);
        repository.fail(0, ChatFailure.Code.TIMEOUT);
        Message original = repository.sent.get(0);
        controller.editDraft(conversation, "another draft");
        controller.retry(original.getClientMessageId());
        assertSame(original, repository.sent.get(1));
        repository.ack(1);
        assertEquals("another draft", controller.draft(conversation));
    }

    @Test public void accountACompletionCannotMutateAccountB() {
        controller.editDraft(conversation, "A private");
        CompletionStage<OutboxEntry> request = controller.sendDraft(conversation);
        QueueExecutor ui = new QueueExecutor();
        AtomicInteger staleRender = new AtomicInteger();
        controller.observe(ui, staleRender::incrementAndGet);
        controller.invalidate();

        UUID userB = UUID.randomUUID();
        ControlledRepository repoB = new ControlledRepository(userB);
        MessagingController accountB = new MessagingController(userB, UUID.randomUUID(), repoB);
        accountB.editDraft(conversation, "B private");
        long revisionB = accountB.draftRevision(conversation);
        repository.ack(0);
        ui.drain();

        assertFailure(request, CancellationException.class);
        assertEquals("", controller.draft(conversation));
        assertTrue(controller.localEntries(conversation).isEmpty());
        assertFalse(controller.canRetry(repository.sent.get(0).getClientMessageId()));
        assertEquals(0, staleRender.get());
        assertEquals("B private", accountB.draft(conversation));
        assertEquals(revisionB, accountB.draftRevision(conversation));
        assertTrue(accountB.isActive());
        assertEquals(0, repoB.sent.size());
        assertEquals(userB, accountB.getCurrentUserId());
        assertNotEquals(controller.getSessionEpoch(), accountB.getSessionEpoch());
    }

    @Test public void invalidatedScopeRejectsAllNewWork() {
        controller.invalidate();
        controller.invalidate();
        int calls = repository.calls;
        assertFailure(controller.listConversations(), CancellationException.class);
        assertFailure(controller.openDirect("peer"), CancellationException.class);
        assertFailure(controller.loadMessages(conversation), CancellationException.class);
        assertFailure(controller.sendDraft(conversation), CancellationException.class);
        assertFailure(controller.retry("old"), CancellationException.class);
        assertThrows(CancellationException.class, () -> controller.editDraft(conversation, "no"));
        MessagingController.ViewSubscription closed = controller.observe(Runnable::run, () -> fail("Must not run"));
        assertFalse(closed.isOpen());
        closed.close();
        assertFalse(controller.isActive());
        assertEquals(calls, repository.calls);
    }

    @Test public void inlineCompletionRegistersStateBeforeCallback() {
        repository.inline = true;
        controller.editDraft(conversation, "hello");
        AtomicInteger observed = new AtomicInteger();
        MessagingController.ViewSubscription view = controller.observe(Runnable::run, observed::incrementAndGet);
        OutboxEntry result = controller.sendDraft(conversation).toCompletableFuture().join();
        assertEquals(OutboxState.SENT, result.getState());
        assertFalse(controller.isSending(conversation));
        assertEquals("", controller.draft(conversation));
        assertEquals(1, repository.sent.size());
        assertTrue(observed.get() >= 2);
        view.close();
    }

    @Test public void inlineObserverCanInvalidateBeforeObserveReturns() {
        controller.observe(Runnable::run, controller::invalidate).close();
        assertFalse(controller.isActive());
        assertFailure(controller.sendDraft(conversation), CancellationException.class);
    }

    @Test public void queuedCallbackIsSuppressedByClose() {
        QueueExecutor ui = new QueueExecutor();
        AtomicInteger callbacks = new AtomicInteger();
        MessagingController.ViewSubscription subscription = controller.observe(ui, callbacks::incrementAndGet);
        subscription.close();
        subscription.close();
        ui.drain();
        assertEquals(0, callbacks.get());
    }

    @Test public void terminalErrorsDoNotEnableRetryAndKeepDraft() {
        for (ChatFailure.Code code : new ChatFailure.Code[] {
                ChatFailure.Code.UNAUTHENTICATED, ChatFailure.Code.FORBIDDEN, ChatFailure.Code.CONFLICT }) {
            ControlledRepository repo = new ControlledRepository(USER);
            MessagingController scope = new MessagingController(USER, UUID.randomUUID(), repo);
            scope.editDraft(repo.conversation, "hello");
            CompletionStage<OutboxEntry> request = scope.sendDraft(repo.conversation);
            repo.fail(0, code);
            assertFailure(request, code);
            String id = repo.sent.get(0).getClientMessageId();
            assertFalse(scope.canRetry(id));
            assertFailure(scope.retry(id), IllegalStateException.class);
            assertEquals("hello", scope.draft(repo.conversation));
            assertEquals(1, repo.sent.size());
        }
    }

    @Test public void offlineEligibilitySurvivesReplacingSubscriber() {
        repository.inline = true;
        repository.fake.setFailure(FakeConversationRepository.Failure.OFFLINE);
        controller.editDraft(conversation, "hello");
        OutboxEntry pending = controller.sendDraft(conversation).toCompletableFuture().join();
        QueueExecutor ui = new QueueExecutor();
        controller.observe(ui, () -> {}).close();
        MessagingController.ViewSubscription replacement = controller.observe(ui, () -> {});
        ui.drain();
        assertEquals(OutboxState.PENDING, pending.getState());
        assertTrue(controller.canRetry(pending.getMessage().getClientMessageId()));
        repository.fake.setFailure(FakeConversationRepository.Failure.NONE);
        controller.retry(pending.getMessage().getClientMessageId()).toCompletableFuture().join();
        assertSame(repository.sent.get(0), repository.sent.get(1));
        assertEquals(1, repository.fake.listMessages(conversation).toCompletableFuture().join().size());
        replacement.close();
    }

    @Test public void historyAckWinsOverLateTimeoutWithoutDuplicate() {
        controller.editDraft(conversation, "hello");
        CompletionStage<OutboxEntry> request = controller.sendDraft(conversation);
        repository.persist(0);
        List<OutboxEntry> rows = controller.loadMessages(conversation).toCompletableFuture().join();
        assertEquals(1, rows.size());
        assertEquals(OutboxState.SENT, rows.get(0).getState());
        assertTrue(controller.isSending(conversation));
        repository.fail(0, ChatFailure.Code.TIMEOUT);
        assertEquals(OutboxState.SENT, request.toCompletableFuture().join().getState());
        assertEquals(1, controller.loadMessages(conversation).toCompletableFuture().join().size());
        assertFalse(controller.canRetry(repository.sent.get(0).getClientMessageId()));
    }

    @Test public void lateLoadAfterInvalidationCannotReturnPrivateRows() {
        CompletableFuture<List<ServerMessage>> history = new CompletableFuture<>();
        repository.delayedHistory = history;
        CompletionStage<List<OutboxEntry>> rows = controller.loadMessages(conversation);
        controller.invalidate();
        history.complete(Collections.emptyList());
        assertFailure(rows, CancellationException.class);
        assertTrue(controller.localEntries(conversation).isEmpty());
    }

    @Test public void sameUserNewEpochDoesNotReuseState() {
        controller.editDraft(conversation, "old draft");
        controller.invalidate();
        MessagingController signedInAgain = new MessagingController(USER, UUID.randomUUID(),
                new ControlledRepository(USER));
        assertEquals("", signedInAgain.draft(conversation));
        assertNotEquals(controller.getSessionEpoch(), signedInAgain.getSessionEpoch());
    }

    @Test public void completionFirstRaceIsSerializedWithInvalidation() throws Exception {
        controller.editDraft(conversation, "hello");
        CompletionStage<OutboxEntry> request = controller.sendDraft(conversation);
        CountDownLatch completingInsideGate = new CountDownLatch(1);
        CountDownLatch releaseCompletion = new CountDownLatch(1);
        CountDownLatch invalidationStarted = new CountDownLatch(1);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            CompletionStage<Void> checked = request.handle((value, error) -> {
                assertNull(error);
                assertEquals(OutboxState.SENT, controller.localEntries(conversation).get(0).getState());
                completingInsideGate.countDown();
                await(releaseCompletion);
                return null;
            });
            Future<?> completion = workers.submit(() -> repository.ack(0));
            await(completingInsideGate);
            Future<?> invalidation = workers.submit(() -> {
                invalidationStarted.countDown();
                controller.invalidate();
            });
            await(invalidationStarted);
            // Invalidation must finish while an external future continuation is still blocked.
            invalidation.get(5, TimeUnit.SECONDS);
            assertFalse(controller.isActive());
            releaseCompletion.countDown();
            completion.get(5, TimeUnit.SECONDS);
            checked.toCompletableFuture().get(5, TimeUnit.SECONDS);
            assertFalse(controller.isActive());
            assertTrue(controller.localEntries(conversation).isEmpty());
        } finally {
            releaseCompletion.countDown();
            workers.shutdownNow();
        }
    }

    @Test public void invalidationFirstRaceDropsCompletion() throws Exception {
        controller.editDraft(conversation, "hello");
        CompletionStage<OutboxEntry> request = controller.sendDraft(conversation);
        CountDownLatch invalidatingInsideGate = new CountDownLatch(1);
        CountDownLatch releaseInvalidation = new CountDownLatch(1);
        CountDownLatch completionStarted = new CountDownLatch(1);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            CompletionStage<Void> checked = request.handle((value, error) -> {
                assertTrue(error instanceof CancellationException);
                invalidatingInsideGate.countDown();
                await(releaseInvalidation);
                return null;
            });
            Future<?> invalidation = workers.submit(controller::invalidate);
            await(invalidatingInsideGate);
            Future<?> completion = workers.submit(() -> {
                completionStarted.countDown();
                repository.ack(0);
            });
            await(completionStarted);
            // Late repository completion is ignored without waiting for cancellation continuations.
            completion.get(5, TimeUnit.SECONDS);
            assertFalse(controller.isActive());
            releaseInvalidation.countDown();
            invalidation.get(5, TimeUnit.SECONDS);
            checked.toCompletableFuture().get(5, TimeUnit.SECONDS);
            assertEquals("", controller.draft(conversation));
            assertTrue(controller.localEntries(conversation).isEmpty());
            assertFalse(controller.isSending(conversation));
        } finally {
            releaseInvalidation.countDown();
            workers.shutdownNow();
        }
    }

    @Test public void slowObserverDoesNotHoldStateMonitor() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<MessagingController.ViewSubscription> observing = workers.submit(() ->
                    controller.observe(Runnable::run, () -> { entered.countDown(); await(release); }));
            await(entered);
            Future<?> invalidation = workers.submit(controller::invalidate);
            invalidation.get(5, TimeUnit.SECONDS);
            assertFalse(controller.isActive());
            release.countDown();
            assertFalse(observing.get(5, TimeUnit.SECONDS).isOpen());
        } finally { release.countDown(); workers.shutdownNow(); }
    }

    @Test public void throwingObserverIsIsolatedFromOperationState() {
        AtomicInteger callbacks = new AtomicInteger();
        MessagingController.ViewSubscription broken = controller.observe(Runnable::run, () -> {
            callbacks.incrementAndGet();
            throw new IllegalStateException("Observer failure");
        });
        assertFalse(broken.isOpen());
        repository.inline = true;
        controller.editDraft(conversation, "hello");
        assertEquals(OutboxState.SENT, controller.sendDraft(conversation).toCompletableFuture().join().getState());
        assertEquals(1, callbacks.get());
        assertTrue(controller.isActive());
    }

    @Test public void blockedRepositoryDispatchDoesNotBlockInvalidation() throws Exception {
        repository.sendEntered = new CountDownLatch(1);
        repository.releaseSend = new CountDownLatch(1);
        repository.inline = true;
        controller.editDraft(conversation, "hello");
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<CompletionStage<OutboxEntry>> sending = workers.submit(() -> controller.sendDraft(conversation));
            await(repository.sendEntered);
            workers.submit(controller::invalidate).get(5, TimeUnit.SECONDS);
            assertFalse(controller.isActive());
            assertTrue(controller.localEntries(conversation).isEmpty());
            repository.releaseSend.countDown();
            assertFailure(sending.get(5, TimeUnit.SECONDS), CancellationException.class);
            // This dispatch was handed off before invalidation; fake may still persist it.
            assertEquals(1, repository.fake.listMessages(conversation).toCompletableFuture().join().size());
            assertEquals("", controller.draft(conversation));
        } finally { repository.releaseSend.countDown(); workers.shutdownNow(); }
    }

    @Test public void queuedReservationIsSkippedWhenInvalidationWinsBeforeHandoff() {
        QueueExecutor dispatch = new QueueExecutor();
        MessagingController queued = new MessagingController(USER, UUID.randomUUID(), repository, dispatch);
        queued.editDraft(conversation, "hello");
        CompletionStage<OutboxEntry> sending = queued.sendDraft(conversation);
        assertTrue(queued.isSending(conversation));
        assertEquals(0, repository.sent.size());
        queued.invalidate();
        dispatch.drain();
        assertFailure(sending, CancellationException.class);
        assertEquals(0, repository.sent.size());
        assertFalse(queued.isSending(conversation));
    }

    @Test(timeout = 10000) public void reentrantObserverDrainsWithoutRecursiveInlineDelivery() {
        repository.inline = true;
        AtomicInteger depth = new AtomicInteger();
        AtomicInteger maxDepth = new AtomicInteger();
        AtomicInteger callbacks = new AtomicInteger();
        List<CompletionStage<OutboxEntry>> requests = new ArrayList<>();
        MessagingController.ViewSubscription subscription = controller.observe(Runnable::run, () -> {
            int current = depth.incrementAndGet();
            maxDepth.set(Math.max(maxDepth.get(), current));
            try {
                if (callbacks.getAndIncrement() == 0) {
                    controller.editDraft(conversation, "hello");
                    requests.add(controller.sendDraft(conversation));
                }
            } finally { depth.decrementAndGet(); }
        });
        assertEquals(1, maxDepth.get());
        assertEquals(2, callbacks.get());
        assertEquals(OutboxState.SENT, requests.get(0).toCompletableFuture().join().getState());
        subscription.close();
    }

    @Test public void observerExecutorRejectionDoesNotLoseOperations() {
        Executor rejecting = task -> { throw new RejectedExecutionException("Rejected observer"); };
        assertFalse(controller.observe(rejecting, () -> fail("Must not run")).isOpen());
        repository.inline = true;
        controller.editDraft(conversation, "hello");
        assertEquals(OutboxState.SENT, controller.sendDraft(conversation).toCompletableFuture().join().getState());
        assertFalse(controller.isSending(conversation));
        assertTrue(controller.isActive());
    }

    @Test public void dispatchExecutorRejectionTerminatesAttemptWithoutLosingDraft() {
        Executor rejecting = task -> { throw new RejectedExecutionException("Rejected dispatch"); };
        MessagingController rejected = new MessagingController(USER, UUID.randomUUID(), repository, rejecting);
        rejected.editDraft(conversation, "hello");
        assertFailure(rejected.sendDraft(conversation), RejectedExecutionException.class);
        assertEquals(0, repository.sent.size());
        assertEquals("hello", rejected.draft(conversation));
        assertFalse(rejected.isSending(conversation));
        assertEquals(OutboxState.FAILED, rejected.localEntries(conversation).get(0).getState());
    }

    @Test public void repositoryThrowTerminatesAttemptAndDoesNotRetainThrowable() {
        IllegalArgumentException original = new IllegalArgumentException("Transport detail");
        repository.dispatchFailure = original;
        controller.editDraft(conversation, "hello");
        assertSame(original, assertFailure(controller.sendDraft(conversation), IllegalArgumentException.class));
        assertFalse(controller.isSending(conversation));
        assertEquals("hello", controller.draft(conversation));
        assertNotSame(original, controller.lastFailure(conversation));
        assertNull(controller.lastFailure(conversation).getCause());
    }

    @Test public void queuedNotificationsPullLatestStateInsteadOfOldSnapshot() {
        QueueExecutor ui = new QueueExecutor();
        List<OutboxState> observed = new ArrayList<>();
        controller.observe(ui, () -> {
            List<OutboxEntry> rows = controller.localEntries(conversation);
            if (!rows.isEmpty()) observed.add(rows.get(0).getState());
        });
        repository.inline = true;
        controller.editDraft(conversation, "hello");
        controller.sendDraft(conversation).toCompletableFuture().join();
        ui.drain();
        assertEquals(Collections.singletonList(OutboxState.SENT), observed);
    }

    @Test public void subscribeInvalidateRaceReturnsClosedHandleWithoutException() throws Exception {
        QueueExecutor ui = new QueueExecutor();
        AtomicInteger callbacks = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<MessagingController.ViewSubscription> observing = workers.submit(() -> {
                await(start);
                return controller.observe(ui, callbacks::incrementAndGet);
            });
            Future<?> invalidation = workers.submit(() -> { await(start); controller.invalidate(); });
            start.countDown();
            MessagingController.ViewSubscription handle = observing.get(5, TimeUnit.SECONDS);
            invalidation.get(5, TimeUnit.SECONDS);
            ui.drain();
            assertFalse(handle.isOpen());
            assertEquals(0, callbacks.get());
            handle.close();
        } finally { start.countDown(); workers.shutdownNow(); }
    }

    @Test public void repeatedDetachAttachDoesNotDuplicateObserver() {
        QueueExecutor ui = new QueueExecutor();
        AtomicInteger callbacks = new AtomicInteger();
        for (int i = 0; i < 5; i++) controller.observe(ui, callbacks::incrementAndGet).close();
        MessagingController.ViewSubscription attached = controller.observe(ui, callbacks::incrementAndGet);
        ui.drain();
        assertEquals(1, callbacks.get());
        attached.close();
        repository.inline = true;
        controller.editDraft(conversation, "hello");
        controller.sendDraft(conversation);
        ui.drain();
        assertEquals(1, callbacks.get());
    }

    @Test public void terminalNotificationClearsUiWithoutDeliveringQueuedPrivateNotification() {
        QueueExecutor ui = new QueueExecutor();
        AtomicInteger privateRender = new AtomicInteger();
        AtomicInteger cleared = new AtomicInteger();
        MessagingController.ViewSubscription handle =
                controller.observe(ui, privateRender::incrementAndGet, cleared::incrementAndGet);
        controller.invalidate();
        assertFalse(handle.isOpen());
        ui.drain();
        assertEquals(0, privateRender.get());
        assertEquals(1, cleared.get());
    }

    @Test public void detachSuppressesQueuedTerminalNotification() {
        QueueExecutor ui = new QueueExecutor();
        AtomicInteger callbacks = new AtomicInteger();
        MessagingController.ViewSubscription handle =
                controller.observe(ui, callbacks::incrementAndGet, callbacks::incrementAndGet);
        controller.invalidate();
        handle.close();
        ui.drain();
        assertEquals(0, callbacks.get());
    }

    @Test public void terminalFailureMetadataClearsOnSuccessfulRetry() {
        controller.editDraft(conversation, "hello");
        controller.sendDraft(conversation);
        repository.fail(0, ChatFailure.Code.TIMEOUT);
        String id = repository.sent.get(0).getClientMessageId();
        assertEquals(ChatFailure.Code.TIMEOUT, ((ChatFailure) controller.lastFailure(conversation)).getCode());
        controller.retry(id);
        assertNull(controller.lastFailure(conversation));
        repository.ack(1);
        assertNull(controller.lastFailure(conversation));
        assertFalse(controller.canRetry(id));
    }

    private static Throwable assertFailure(CompletionStage<?> request, Class<? extends Throwable> expected) {
        Throwable failure;
        try {
            request.toCompletableFuture().get(5, TimeUnit.SECONDS);
            throw new AssertionError("Expected " + expected.getSimpleName());
        } catch (ExecutionException error) {
            failure = error.getCause();
        } catch (CancellationException error) {
            failure = error;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError(error);
        } catch (TimeoutException error) {
            throw new AssertionError("Request did not terminate", error);
        }
        assertEquals(expected, failure.getClass());
        return failure;
    }

    private static void assertFailure(CompletionStage<?> request, ChatFailure.Code code) {
        assertEquals(code, ((ChatFailure) assertFailure(request, ChatFailure.class)).getCode());
    }

    private static void await(CountDownLatch latch) {
        try { assertTrue("Bounded latch wait", latch.await(5, TimeUnit.SECONDS)); }
        catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError(error);
        }
    }

    private static final class QueueExecutor implements Executor {
        private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
        @Override public void execute(Runnable task) { tasks.add(task); }
        void drain() {
            Runnable task;
            while ((task = tasks.poll()) != null) task.run();
        }
    }

    private static final class ControlledRepository implements ConversationRepository {
        final FakeConversationRepository fake;
        final String conversation;
        final List<Message> sent = new ArrayList<>();
        final List<CompletableFuture<OutboxEntry>> replies = new ArrayList<>();
        CompletableFuture<List<ServerMessage>> delayedHistory;
        boolean inline;
        RuntimeException dispatchFailure;
        CountDownLatch sendEntered;
        CountDownLatch releaseSend;
        int calls;

        ControlledRepository(UUID user) {
            fake = new FakeConversationRepository(user.toString());
            conversation = fake.openDirect("peer").toCompletableFuture().join().getId();
        }

        @Override public CompletionStage<OutboxEntry> send(Message message) {
            calls++;
            sent.add(message);
            if (sendEntered != null) {
                sendEntered.countDown();
                await(releaseSend);
            }
            if (dispatchFailure != null) throw dispatchFailure;
            if (inline) return fake.send(message);
            CompletableFuture<OutboxEntry> reply = new CompletableFuture<>();
            replies.add(reply);
            return reply;
        }

        OutboxEntry persist(int index) { return fake.send(sent.get(index)).toCompletableFuture().join(); }
        void ack(int index) { replies.get(index).complete(persist(index)); }
        void fail(int index, ChatFailure.Code code) {
            replies.get(index).completeExceptionally(new ChatFailure(code));
        }
        @Override public CompletionStage<List<Conversation>> listConversations() {
            calls++; return fake.listConversations();
        }
        @Override public CompletionStage<Conversation> openDirect(String peer) {
            calls++; return fake.openDirect(peer);
        }
        @Override public CompletionStage<List<ServerMessage>> listMessages(String id) {
            calls++; return delayedHistory == null ? fake.listMessages(id) : delayedHistory;
        }
        @Override public CompletionStage<MessagePage> loadMessages(String id, Long before, int limit) {
            calls++; return fake.loadMessages(id, before, limit);
        }
        @Override public CompletionStage<List<OutboxEntry>> listOutbox(String id) {
            calls++; return fake.listOutbox(id);
        }
        @Override public CompletionStage<Void> markRead(String id, ServerMessage message) {
            calls++; return fake.markRead(id, message);
        }
    }
}
