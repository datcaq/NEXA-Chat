package com.nexa.chat.feature.messaging.ui;

import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import org.junit.Test;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

/** Targeted R3 reproductions. Production dispatch/settlement; no SecurityManager or sleeps. */
public class MessagingFix4Test {
    private static void await(CountDownLatch latch) {
        try { assertTrue("Latch timeout", latch.await(5, TimeUnit.SECONDS)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }
    private static <T> T result(CompletionStage<T> stage) throws Exception {
        return stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }
    private static MessagingExecution.AccountLease lease(MessagingExecution runtime) {
        return runtime.acquire(UUID.randomUUID(), UUID.randomUUID());
    }
    private static MessagingController controller(MessagingExecution.AccountLease lease, ConversationRepository repo) {
        return new MessagingController(lease.userId, lease.epoch, repo, lease);
    }
    private static ConversationRepository repo(java.lang.reflect.InvocationHandler handler) {
        return (ConversationRepository) Proxy.newProxyInstance(ConversationRepository.class.getClassLoader(),
                new Class<?>[]{ConversationRepository.class}, handler);
    }
    @Test public void fourInvalidSendsDoNotConsumeReservationsOrPreventRetirement() throws Exception {
        MessagingExecution runtime = new MessagingExecution();
        MessagingExecution.AccountLease lease = lease(runtime);
        AtomicInteger calls = new AtomicInteger();
        MessagingController c = controller(lease, repo((p,m,args) -> {
            calls.incrementAndGet(); return CompletableFuture.completedFuture(Collections.emptyList());
        }));
        try {
            c.editDraft(" ", "retained"); long revision = c.draftRevision(" ");
            for (int i=0;i<4;i++) {
                assertThrows(IllegalArgumentException.class, () -> c.sendDraft(" "));
                assertEquals(0,lease.callerReservations()); assertEquals(0,lease.operationReservations());
                assertEquals(0,lease.sourceStages()); assertEquals("retained",c.draft(" "));
                assertEquals(revision,c.draftRevision(" "));
            }
            assertTrue(result(c.listConversations()).isEmpty()); assertEquals(1,calls.get());
        } finally { runtime.close(); }
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void invalidSendRacingInvalidationDoesNotDoubleRelease() throws Exception {
        MessagingExecution runtime = new MessagingExecution();
        MessagingExecution.AccountLease lease = lease(runtime);
        MessagingController c = controller(lease, repo((p,m,args) -> { throw new AssertionError("No repository call"); }));
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        c.editDraft(" ", "keep");
        try {
            Future<?> send = workers.submit(() -> {
                await(start);
                try { result(c.sendDraft(" ")); fail(); }
                catch (IllegalArgumentException | CancellationException expected) { }
                catch (Exception error) { throw new AssertionError(error); }
            });
            Future<?> invalidate = workers.submit(() -> { await(start); c.invalidate(); });
            start.countDown(); send.get(5,TimeUnit.SECONDS); invalidate.get(5,TimeUnit.SECONDS);
            assertEquals(0,lease.callerReservations()); assertEquals(0,lease.operationReservations());
        } finally { start.countDown(); workers.shutdownNow(); runtime.close(); }
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void repositoryThrowAfterReservationSettlesExactlyOnceDuringInvalidation() throws Exception {
        MessagingExecution runtime = new MessagingExecution();
        MessagingExecution.AccountLease lease = lease(runtime);
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        MessagingController c = controller(lease,repo((p,m,args) -> {
            entered.countDown(); await(release); throw new IllegalStateException("after admission");
        }));
        CompletionStage<?> root = c.listConversations();
        try {
            await(entered); assertEquals(1,lease.operationReservations());
            c.invalidate(); assertThrows(CancellationException.class, () -> result(root));
        } finally { release.countDown(); runtime.close(); }
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
        assertEquals(0,lease.callerReservations()); assertEquals(0,lease.operationReservations());
    }
    @Test public void postedTerminalSurvivesNormalObserverFailureWhilePresentationInactive() throws Exception {
        terminalFailureRace(false);
    }
    @Test public void explicitDetachStillSuppressesPostedTerminalAfterNormalFailure() throws Exception {
        terminalFailureRace(true);
    }
    private void terminalFailureRace(boolean detach) throws Exception {
        MessagingExecution runtime = new MessagingExecution();
        MessagingExecution.AccountLease lease = lease(runtime);
        CompletableFuture<List<Conversation>> remote = new CompletableFuture<>();
        MessagingController c = controller(lease,repo((p,m,args) -> remote));
        MessagingPresentation presentation = new MessagingPresentation(c.getRouter());
        presentation.setAttached(true); assertFalse(presentation.isActive());
        BlockingQueue<Runnable> ui = new LinkedBlockingQueue<>();
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicInteger normal = new AtomicInteger(), terminal = new AtomicInteger();
        ExecutorService worker = Executors.newSingleThreadExecutor(), main = Executors.newSingleThreadExecutor();
        MessagingController.ViewSubscription subscription = c.observe(ui::add, () -> {
            normal.incrementAndGet(); entered.countDown(); await(release);
            throw new IllegalStateException("normal render failed");
        }, terminal::incrementAndGet);
        Future<?> draining = worker.submit(ui.remove());
        CompletionStage<?> pending = c.listConversations();
        try {
            await(entered);
            main.submit(() -> c.invalidate()).get(5,TimeUnit.SECONDS);
            assertFalse(c.isActive()); assertEquals(1,ui.size());
            release.countDown(); draining.get(5,TimeUnit.SECONDS);
            if (detach) subscription.close();
            Runnable task; while ((task=ui.poll())!=null) task.run();
            c.invalidate(); assertEquals(detach ? 0 : 1,terminal.get());
            assertEquals(1,normal.get()); assertFalse(subscription.isOpen());
            assertThrows(CancellationException.class, () -> result(pending));
        } finally {
            release.countDown(); remote.complete(Collections.emptyList()); runtime.close();
            worker.shutdownNow(); main.shutdownNow();
        }
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void normalObserverFailureBeforeInvalidationRetainsTerminalOnly() throws Exception {
        MessagingExecution runtime = new MessagingExecution();
        MessagingExecution.AccountLease lease = lease(runtime);
        MessagingController c = controller(lease,repo((p,m,args) -> CompletableFuture.completedFuture(Collections.emptyList())));
        Queue<Runnable> ui = new ConcurrentLinkedQueue<>();
        AtomicInteger normal = new AtomicInteger(), terminal = new AtomicInteger();
        MessagingController.ViewSubscription subscription = c.observe(ui::add, () -> {
            normal.incrementAndGet(); throw new IllegalStateException();
        },terminal::incrementAndGet);
        try {
            ui.remove().run(); assertFalse(subscription.isOpen());
            result(c.listConversations()); assertEquals(1,normal.get());
            c.invalidate(); c.invalidate();
            Runnable task; while((task=ui.poll())!=null)task.run();
            assertEquals(1,terminal.get()); assertEquals(1,normal.get());
        } finally { runtime.close(); }
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void reentrantCloseAfterTerminalClaimDoesNotDuplicate() throws Exception {
        MessagingExecution runtime = new MessagingExecution();
        MessagingExecution.AccountLease lease = lease(runtime);
        MessagingController c = controller(lease,repo((p,m,args) -> CompletableFuture.completedFuture(Collections.emptyList())));
        Queue<Runnable> ui = new ConcurrentLinkedQueue<>(); AtomicInteger terminal = new AtomicInteger();
        AtomicReference<MessagingController.ViewSubscription> holder = new AtomicReference<>();
        holder.set(c.observe(ui::add, () -> {}, () -> {
            terminal.incrementAndGet(); holder.get().close(); c.invalidate();
        }));
        try {
            c.invalidate(); Runnable task; while((task=ui.poll())!=null)task.run();
            assertEquals(1,terminal.get()); holder.get().close(); c.invalidate(); assertEquals(1,terminal.get());
        } finally { runtime.close(); }
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }

    private static final class StartupFactory implements ThreadFactory {
        final int failAt;
        final boolean failCreation;
        final boolean holdFirst;
        final AtomicInteger created = new AtomicInteger();
        final List<Thread> threads = new CopyOnWriteArrayList<>();
        final CountDownLatch running = new CountDownLatch(1), release = new CountDownLatch(1);
        final CountDownLatch failing = new CountDownLatch(1), allowFailure = new CountDownLatch(1);
        boolean pauseFailure;
        StartupFactory(int failAt, boolean failCreation, boolean holdFirst) {
            this.failAt=failAt; this.failCreation=failCreation; this.holdFirst=holdFirst;
        }
        public Thread newThread(Runnable work) {
            int number=created.incrementAndGet();
            // Inject dispatch startup at its factory boundary: some JDK executors start Thread
            // instances internally without dispatching an overridden Thread.start().
            if(number==failAt && (failCreation || number==1))
                throw new SecurityException("worker creation");
            Thread thread=new Thread(() -> {
                if(number==1 && holdFirst) { running.countDown(); await(release); }
                work.run();
            }) {
                @Override public synchronized void start() {
                    if(number==failAt) {
                        if(holdFirst)await(running);
                        failing.countDown(); if(pauseFailure)await(allowFailure);
                        throw new SecurityException("worker start");
                    }
                    super.start();
                }
            };
            threads.add(thread); return thread;
        }
    }
    private void failedStartupRetires(int failAt, boolean creation) throws Exception {
        StartupFactory factory=new StartupFactory(failAt,creation,false);
        MessagingExecution runtime=new MessagingExecution(factory);
        try {
            assertThrows(SecurityException.class, () -> lease(runtime));
            // The runtime stays usable after rollback and actual termination.
            assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
            assertEquals(0,runtime.liveLeases());
            MessagingExecution.AccountLease next=lease(runtime); next.close();
        } finally { factory.release.countDown(); factory.allowFailure.countDown(); runtime.close(); }
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
        for(Thread thread:factory.threads)assertFalse(thread.isAlive());
    }
    @Test public void workerCreationFailureBeforeAnyStartRetires() throws Exception { failedStartupRetires(1,true); }
    @Test public void dispatchWorkerStartupFailureRetires() throws Exception { failedStartupRetires(1,false); }
    @Test public void failureAfterOneSettlementWorkerRetires() throws Exception { failedStartupRetires(3,false); }
    @Test public void failureAfterThreeSettlementWorkersRetires() throws Exception { failedStartupRetires(5,false); }

    @Test public void failedLeaseStaysChargedUntilStartedWorkerActuallyExits() throws Exception {
        StartupFactory factory=new StartupFactory(2,false,true);
        MessagingExecution runtime=new MessagingExecution(factory);
        MessagingExecution.AccountLease other=null;
        try {
            assertThrows(SecurityException.class, () -> lease(runtime));
            assertEquals(1,runtime.liveLeases()); assertFalse(runtime.awaitTermination(20,TimeUnit.MILLISECONDS));
            other=lease(runtime); assertEquals(2,runtime.liveLeases());
            assertThrows(RejectedExecutionException.class, () -> lease(runtime));
            assertTrue(factory.threads.stream().filter(Thread::isAlive).count()<=MessagingExecution.MAX_LEASES*5);
        } finally {
            factory.release.countDown(); factory.allowFailure.countDown();
            if(other!=null)other.close();runtime.close();
        }
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
        for(Thread thread:factory.threads)assertFalse(thread.isAlive());
    }
    @Test public void runtimeCloseRacingPartialStartupFailureIsSerialized() throws Exception {
        StartupFactory factory=new StartupFactory(3,false,true);factory.pauseFailure=true;
        MessagingExecution runtime=new MessagingExecution(factory);
        ExecutorService callers=Executors.newFixedThreadPool(2);
        CountDownLatch closeStarted=new CountDownLatch(1);
        try {
            Future<?> acquiring=callers.submit(() -> assertThrows(SecurityException.class, () -> lease(runtime)));
            await(factory.failing);
            Future<?> closing=callers.submit(() -> { closeStarted.countDown();runtime.close(); });
            await(closeStarted);factory.allowFailure.countDown();
            acquiring.get(5,TimeUnit.SECONDS);closing.get(5,TimeUnit.SECONDS);
            assertEquals(1,runtime.liveLeases());
            assertThrows(RejectedExecutionException.class, () -> lease(runtime));
        } finally {
            factory.allowFailure.countDown();factory.release.countDown();runtime.close();callers.shutdownNow();
        }
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
        for(Thread thread:factory.threads)assertFalse(thread.isAlive());
    }
}
