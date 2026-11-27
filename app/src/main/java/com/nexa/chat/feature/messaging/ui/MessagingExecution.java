package com.nexa.chat.feature.messaging.ui;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Host-owned bounded runtime. Close is nonblocking; a retiring lease stays charged until
 * every invocation/source, completion call (including synchronous callbacks), and worker exits.
 * Repository cancellation is deliberately not inferred from caller future cancellation.
 */
public final class MessagingExecution implements AutoCloseable {
    public static final int MAX_LEASES = 2, CALLERS = 4, OPERATIONS = 4;
    public static final int SETTLEMENT_WORKERS = 4, MAILBOX_CAPACITY = 4;
    private final Set<AccountLease> leases = new HashSet<>();
    private boolean closed;
    private final ThreadFactory workerFactory;

    public MessagingExecution() { this(Thread::new); }
    // Package-local fault injection only; production uses ordinary managed daemon threads.
    MessagingExecution(ThreadFactory workerFactory) {
        this.workerFactory = Objects.requireNonNull(workerFactory);
    }

    public synchronized AccountLease acquire(UUID userId, UUID epoch) {
        Objects.requireNonNull(userId); Objects.requireNonNull(epoch);
        reap();
        if (closed || leases.size() == MAX_LEASES)
            throw new RejectedExecutionException("Messaging lease capacity exhausted");
        for (AccountLease lease : leases)
            if (lease.userId.equals(userId) && lease.epoch.equals(epoch))
                throw new RejectedExecutionException("Duplicate Messaging epoch");
        AccountLease lease = new AccountLease(userId, epoch);
        leases.add(lease);
        lease.start();
        return lease;
    }
    private void reap() { leases.removeIf(AccountLease::terminated); }
    public synchronized int liveLeases() { reap(); return leases.size(); }
    private synchronized void workerStopped() { notifyAll(); }
    public synchronized boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long end = System.nanoTime() + unit.toNanos(timeout);
        reap();
        while (!leases.isEmpty()) {
            long left = end - System.nanoTime();
            if (left <= 0) return false;
            TimeUnit.NANOSECONDS.timedWait(this, Math.min(left, TimeUnit.MILLISECONDS.toNanos(10)));
            reap();
        }
        return true;
    }
    @Override public void close() {
        List<AccountLease> copy;
        synchronized (this) { closed = true; copy = new ArrayList<>(leases); }
        for (AccountLease lease : copy) lease.close();
    }

    interface Settlement { void settle(); }

    public final class AccountLease implements AutoCloseable {
        public final UUID userId, epoch;
        final Object gate = new Object();
        private final ArrayDeque<Settlement> settlements = new ArrayDeque<>();
        private final ThreadPoolExecutor dispatch;
        private final List<Thread> threads = new ArrayList<>();
        private int callers, operations, sources;
        private boolean closing, bound;
        private Runnable invalidator;
        // Test seam only; production always uses its owned bounded dispatcher.
        Executor testDispatch;
        AccountLease(UUID userId, UUID epoch) {
            this.userId = userId; this.epoch = epoch;
            dispatch = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(MAILBOX_CAPACITY), task -> {
                Thread t = workerFactory.newThread(() -> { try { task.run(); } finally { workerExited(); } });
                t.setName("messaging-repository-" + epoch);
                t.setDaemon(true);
                synchronized (gate) { threads.add(t); }
                return t;
            }, new ThreadPoolExecutor.AbortPolicy());
        }
        void start() {
            try {
                if (!dispatch.prestartCoreThread())
                    throw new RejectedExecutionException("Messaging dispatch worker did not start");
                for (int i = 0; i < SETTLEMENT_WORKERS; i++) {
                    Thread t = Objects.requireNonNull(workerFactory.newThread(this::settlementLoop));
                    t.setName("messaging-settlement-" + epoch + "-" + i);
                    t.setDaemon(true);
                    synchronized (gate) { threads.add(t); }
                    t.start();
                }
            } catch (RuntimeException | Error failure) {
                // acquire still owns the runtime monitor: no controller can bind this failed lease.
                // Keep it charged while any started thread is alive. Never join on the caller/UI.
                synchronized (gate) { closeLocked(); }
                throw failure;
            }
        }
        void bind(UUID user, UUID expectedEpoch, Runnable invalidate) {
            synchronized (gate) {
                if (closing || bound || !userId.equals(user) || !epoch.equals(expectedEpoch))
                    throw new IllegalArgumentException("Lease ownership mismatch or already bound");
                bound = true; invalidator = invalidate;
            }
        }
        boolean admitLocked(boolean operation) {
            if (closing || callers == CALLERS || (operation && operations == OPERATIONS)) return false;
            callers++;
            if (operation) operations++;
            return true;
        }
        RepositoryOperation operationLocked() { return new RepositoryOperation(); }
        void readyLocked(Settlement token) {
            if (settlements.size() == MAILBOX_CAPACITY) throw new AssertionError("Reserved mailbox overflow");
            settlements.add(token); gate.notifyAll();
        }
        void callerReturned() {
            synchronized (gate) { callers--; quiesceLocked(); }
        }
        void dispatch(RepositoryOperation operation, Runnable task, java.util.function.Consumer<RuntimeException> rejected) {
            Invocation invocation = new Invocation(operation, task);
            try { (testDispatch == null ? dispatch : testDispatch).execute(invocation); }
            catch (RuntimeException error) {
                try { rejected.accept(error); } finally { operation.invocationReturned(); }
            }
        }
        void closeLocked() {
            closing = true; invalidator = null;
            for (Runnable queued : dispatch.getQueue().toArray(new Runnable[0])) {
                if (dispatch.remove(queued)) ((Invocation) queued).operation.invocationReturned();
            }
            quiesceLocked();
        }
        private void quiesceLocked() {
            if (closing && operations == 0) dispatch.shutdown();
            gate.notifyAll();
        }
        @Override public void close() {
            Runnable callback;
            synchronized (gate) { callback = invalidator; if (callback == null) closeLocked(); }
            if (callback != null) callback.run();
        }
        private void settlementLoop() {
            try {
                while (true) {
                    Settlement work;
                    synchronized (gate) {
                        while (settlements.isEmpty()) {
                            if (closing && callers == 0 && operations == 0) return;
                            try { gate.wait(); } catch (InterruptedException ignored) { /* close is cooperative */ }
                        }
                        work = settlements.remove();
                    }
                    try { work.settle(); } finally { callerReturned(); }
                }
            } finally { workerExited(); }
        }
        private final class Invocation implements Runnable {
            final RepositoryOperation operation;
            final Runnable task;
            Invocation(RepositoryOperation operation, Runnable task) { this.operation = operation; this.task = task; }
            public void run() {
                try { task.run(); } finally { operation.invocationReturned(); }
            }
        }
        private void workerExited() {
            synchronized (gate) { gate.notifyAll(); }
            workerStopped();
        }
        private boolean terminated() {
            synchronized (gate) {
                if (!closing || callers != 0 || operations != 0 || !settlements.isEmpty()) return false;
                // NEW threads whose start failed consume no live worker. Started threads must
                // actually exit; a finally notification alone is not proof of termination.
                for (Thread thread : threads) if (thread.isAlive()) return false;
                return dispatch.isTerminated();
            }
        }
        public int callerReservations() { synchronized (gate) { return callers; } }
        public int operationReservations() { synchronized (gate) { return operations; } }
        public int sourceStages() { synchronized (gate) { return sources; } }
        public int settlementMailboxSize() { synchronized (gate) { return settlements.size(); } }

        final class RepositoryOperation {
            private int pendingSources;
            private boolean returned, released;
            <T> CompletionStage<T> source(Supplier<CompletionStage<T>> call) {
                CompletionStage<T> stage = Objects.requireNonNull(call.get());
                synchronized (gate) { pendingSources++; sources++; gate.notifyAll(); }
                AtomicBoolean terminal = new AtomicBoolean();
                stage.whenComplete((value, error) -> {
                    if (terminal.compareAndSet(false, true)) {
                        synchronized (gate) { pendingSources--; sources--; releaseLocked(); }
                    }
                });
                return stage;
            }
            void invocationReturned() {
                synchronized (gate) { returned = true; releaseLocked(); }
            }
            private void releaseLocked() {
                if (!released && returned && pendingSources == 0) {
                    released = true; operations--; quiesceLocked();
                }
            }
        }
    }
}
