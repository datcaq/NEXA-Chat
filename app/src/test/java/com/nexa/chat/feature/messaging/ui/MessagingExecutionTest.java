package com.nexa.chat.feature.messaging.ui;

import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import org.junit.Test;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import static org.junit.Assert.*;

public class MessagingExecutionTest {
    private static ConversationRepository repo(java.lang.reflect.InvocationHandler handler) {
        return (ConversationRepository) Proxy.newProxyInstance(ConversationRepository.class.getClassLoader(),
                new Class<?>[]{ConversationRepository.class}, handler);
    }
    private static MessagingController controller(MessagingExecution.AccountLease lease, ConversationRepository repo) {
        return new MessagingController(lease.userId, lease.epoch, repo, lease);
    }
    private static MessagingExecution.AccountLease lease(MessagingExecution runtime) {
        return runtime.acquire(UUID.randomUUID(), UUID.randomUUID());
    }
    private static void await(CountDownLatch latch) {
        try { assertTrue("Latch timeout", latch.await(5, TimeUnit.SECONDS)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }
    private static void until(MessagingExecution.AccountLease lease, BooleanSupplier condition) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        synchronized (lease.gate) {
            while (!condition.getAsBoolean()) {
                long remaining = end - System.nanoTime(); assertTrue("Accounting timeout", remaining > 0);
                TimeUnit.NANOSECONDS.timedWait(lease.gate, remaining);
            }
        }
    }
    private static <T> T result(CompletionStage<T> s) throws Exception { return s.toCompletableFuture().get(5, TimeUnit.SECONDS); }
    private static void rejected(CompletionStage<?> stage) throws Exception {
        try { result(stage); fail("Must reject"); }
        catch (ExecutionException e) { assertTrue(e.getCause() instanceof RejectedExecutionException); }
    }
    @Test public void fourBlockedCallbacksRejectNestedWorkAndOtherAccountProgresses() throws Exception {
        MessagingExecution runtime = new MessagingExecution();
        MessagingExecution.AccountLease a = lease(runtime), b = lease(runtime);
        CompletableFuture<List<Conversation>> remote = new CompletableFuture<>();
        CountDownLatch invoked = new CountDownLatch(4), entered = new CountDownLatch(4), release = new CountDownLatch(1);
        MessagingController old = controller(a, repo((p,m,args) -> { invoked.countDown(); return remote; }));
        MessagingController fresh = controller(b, repo((p,m,args) -> CompletableFuture.completedFuture(Collections.emptyList())));
        List<CompletionStage<?>> roots = new ArrayList<>(), checks = new ArrayList<>();
        try {
            for (int i=0;i<4;i++) {
                CompletionStage<?> root = old.listConversations(); roots.add(root);
                checks.add(root.handle((v,e) -> {
                    try { rejected(old.listConversations()); }
                    catch (Exception failure) { throw new AssertionError(failure); }
                    // Original nested-pool dependency now uses standard CF and needs no publication task.
                    CompletableFuture<String> inner = new CompletableFuture<>();
                    inner.complete("ok"); assertEquals("ok!", inner.thenApply(x -> x+"!").join());
                    entered.countDown(); await(release); return null;
                }));
            }
            await(invoked); remote.complete(Collections.emptyList()); await(entered);
            assertEquals(4, a.callerReservations());
            for (CompletionStage<?> root : roots) assertTrue(root.toCompletableFuture().isDone());
            rejected(old.listConversations());
            assertTrue(result(fresh.listConversations()).isEmpty());
            old.invalidate(); assertFalse(old.isActive());
            assertThrows(RejectedExecutionException.class, () -> lease(runtime));
        } finally { release.countDown(); runtime.close(); remote.complete(Collections.emptyList()); }
        for (CompletionStage<?> check : checks) result(check);
        assertTrue(runtime.awaitTermination(5, TimeUnit.SECONDS)); assertEquals(0, runtime.liveLeases());
    }
    @Test public void historySecondCallThrowKeepsFirstPhysicalSourceCharged() throws Exception {
        MessagingExecution runtime = new MessagingExecution(); MessagingExecution.AccountLease a = lease(runtime);
        CompletableFuture<List<ServerMessage>> history = new CompletableFuture<>();
        MessagingController c = controller(a, repo((p,m,args) -> {
            if (m.getName().equals("listMessages")) return history;
            throw new IllegalStateException("second source throw");
        }));
        try {
            assertThrows(ExecutionException.class, () -> result(c.loadMessages("c")));
            assertEquals(1, a.operationReservations()); assertEquals(1, a.sourceStages());
            c.invalidate(); until(a, () -> a.callerReservations()==0);
            assertFalse(runtime.awaitTermination(20, TimeUnit.MILLISECONDS));
            assertEquals(1, runtime.liveLeases()); assertFalse(history.isCancelled());
        } finally { history.complete(Collections.emptyList()); runtime.close(); }
        assertTrue(runtime.awaitTermination(5, TimeUnit.SECONDS));
    }
    @Test public void historyFailureDoesNotForgetOtherPendingSource() throws Exception {
        MessagingExecution runtime = new MessagingExecution(); MessagingExecution.AccountLease a = lease(runtime);
        CompletableFuture<List<ServerMessage>> history = new CompletableFuture<>();
        CompletableFuture<List<OutboxEntry>> outbox = new CompletableFuture<>();
        CountDownLatch invoked = new CountDownLatch(2);
        MessagingController c = controller(a, repo((p,m,args) -> {
            invoked.countDown(); return m.getName().equals("listMessages") ? history : outbox;
        }));
        try {
            CompletionStage<?> root = c.loadMessages("c"); await(invoked);
            until(a, () -> a.sourceStages()==2);
            history.completeExceptionally(new IllegalStateException()); c.invalidate();
            assertThrows(CancellationException.class, () -> result(root));
            assertEquals(1, a.sourceStages()); assertEquals(1, a.operationReservations());
            assertFalse(outbox.isCancelled());
        } finally { history.complete(Collections.emptyList()); outbox.complete(Collections.emptyList()); runtime.close(); }
        assertTrue(runtime.awaitTermination(5, TimeUnit.SECONDS));
    }
    @Test public void eightHistorySourcesAndFourOperationsAreBounded() throws Exception {
        MessagingExecution runtime = new MessagingExecution(); MessagingExecution.AccountLease a = lease(runtime);
        CompletableFuture<List<ServerMessage>> history = new CompletableFuture<>();
        CompletableFuture<List<OutboxEntry>> outbox = new CompletableFuture<>();
        CountDownLatch invoked = new CountDownLatch(8);
        MessagingController c = controller(a, repo((p,m,args) -> {
            invoked.countDown(); return m.getName().equals("listMessages") ? history : outbox;
        }));
        try {
            for(int i=0;i<4;i++) c.loadMessages("c");
            await(invoked); until(a, () -> a.sourceStages()==8); assertEquals(8,a.sourceStages()); assertEquals(4,a.operationReservations());
            rejected(c.loadMessages("c")); c.invalidate();
            until(a, () -> a.callerReservations()==0);
            assertEquals(4,a.operationReservations()); assertEquals(8,a.sourceStages());
        } finally { history.complete(Collections.emptyList()); outbox.complete(Collections.emptyList()); runtime.close(); }
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void directBlockedFanoutDoesNotBlockOtherReservedCaller() throws Exception {
        MessagingExecution runtime = new MessagingExecution(); MessagingExecution.AccountLease a = lease(runtime);
        CompletableFuture<Conversation> remote = new CompletableFuture<>(); CountDownLatch invoked = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        MessagingController c = controller(a,repo((p,m,args)->{ calls.incrementAndGet(); invoked.countDown(); return remote; }));
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        CompletionStage<?> first=c.openDirectAndNavigate("A");
        CompletionStage<?> checked=first.thenRun(()->{entered.countDown();await(release);});
        CompletionStage<Conversation> second=c.openDirectAndNavigate("A");
        try {
            await(invoked); remote.complete(new Conversation("A",ConversationType.DIRECT)); await(entered);
            assertEquals("A",result(second).getId()); assertEquals(1,calls.get());
            c.invalidate(); assertFalse(c.isActive()); until(a, () -> a.callerReservations()==1); assertEquals(1,a.callerReservations());
        } finally {release.countDown();runtime.close();}
        result(checked); assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void rejectionPreservesDraftAndNavigationIntent() throws Exception {
        MessagingExecution runtime=new MessagingExecution(); MessagingExecution.AccountLease a=lease(runtime);
        CompletableFuture<List<Conversation>> remote=new CompletableFuture<>();
        MessagingController c=controller(a,repo((p,m,args)->remote));
        try {
            for(int i=0;i<4;i++)c.listConversations();
            c.editDraft("c","keep"); long intent=c.getRouter().navigationIntent();
            rejected(c.sendDraft("c")); rejected(c.openDirectAndNavigate("peer"));
            assertEquals("keep",c.draft("c")); assertTrue(c.localEntries("c").isEmpty());
            assertEquals(intent,c.getRouter().navigationIntent()); assertFalse(c.isSending("c"));
        } finally {runtime.close();remote.complete(Collections.emptyList());}
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void blockedInvocationRetainsLeaseAndQueuedWorkIsRemoved() throws Exception {
        MessagingExecution runtime=new MessagingExecution(); MessagingExecution.AccountLease a=lease(runtime);
        CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1); AtomicInteger calls=new AtomicInteger();
        MessagingController c=controller(a,repo((p,m,args)->{
            calls.incrementAndGet();entered.countDown();await(release);return CompletableFuture.completedFuture(Collections.emptyList());
        }));
        try {
            CompletionStage<?> first=c.listConversations();await(entered);
            for(int i=0;i<3;i++)c.listConversations();
            c.invalidate();assertThrows(CancellationException.class,()->result(first));
            assertEquals(1,a.operationReservations());assertEquals(1,calls.get());
            assertFalse(runtime.awaitTermination(20,TimeUnit.MILLISECONDS));
        } finally {release.countDown();runtime.close();}
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));assertEquals(1,calls.get());
    }
    @Test public void wrongEpochDuplicateInvalidateAndLateResultAreSafe() throws Exception {
        MessagingExecution runtime=new MessagingExecution();MessagingExecution.AccountLease a=lease(runtime);
        CompletableFuture<Conversation> remote=new CompletableFuture<>();CountDownLatch called=new CountDownLatch(1);
        MessagingController c=controller(a,repo((p,m,args)->{called.countDown();return remote;}));
        try {
            CompletionStage<?> root=c.openDirectAndNavigate("A");await(called);
            c.invalidate(UUID.randomUUID());assertTrue(c.isActive());assertFalse(root.toCompletableFuture().isDone());
            c.invalidate(a.epoch);c.invalidate(a.epoch);assertThrows(CancellationException.class,()->result(root));
            remote.complete(new Conversation("OLD",ConversationType.DIRECT));
            assertNull(c.getRouter().currentRoute().getConversationId());
        } finally {runtime.close();remote.complete(new Conversation("OLD",ConversationType.DIRECT));}
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void terminalIsIndependentOfBlockedNormalObserverDrain() throws Exception {
        MessagingExecution runtime=new MessagingExecution();MessagingExecution.AccountLease a=lease(runtime);
        MessagingController c=controller(a,repo((p,m,args)->CompletableFuture.completedFuture(Collections.emptyList())));
        ExecutorService ui=Executors.newFixedThreadPool(2);
        CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1),terminal=new CountDownLatch(1);
        try {
            c.observe(ui,()->{entered.countDown();await(release);},terminal::countDown);
            await(entered);c.invalidate();await(terminal);assertFalse(c.isActive());
        } finally {release.countDown();ui.shutdown();runtime.close();}
        assertTrue(ui.awaitTermination(5,TimeUnit.SECONDS));assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void reservedSettlementMailboxHasExactBoundAndExactlyOnceReturn() throws Exception {
        MessagingExecution runtime=new MessagingExecution(); MessagingExecution.AccountLease a=lease(runtime);
        AtomicInteger settled=new AtomicInteger();
        try {
            synchronized(a.gate) {
                for(int i=0;i<4;i++) {
                    assertTrue(a.admitLocked(false));
                    a.readyLocked(settled::incrementAndGet);
                }
                assertEquals(4,a.settlementMailboxSize());
                assertFalse(a.admitLocked(false));assertEquals(4,a.callerReservations());
            }
            until(a,()->a.callerReservations()==0);
            assertEquals(4,settled.get());assertEquals(0,a.settlementMailboxSize());
        } finally {runtime.close();}
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void manualCompletionAndCancellationCannotReleasePhysicalCapacity() throws Exception {
        MessagingExecution runtime=new MessagingExecution();MessagingExecution.AccountLease a=lease(runtime);
        CompletableFuture<List<Conversation>> remote=new CompletableFuture<>();CountDownLatch called=new CountDownLatch(4);
        MessagingController c=controller(a,repo((p,m,args)->{called.countDown();return remote;}));
        try {
            for(int i=0;i<4;i++) {
                CompletableFuture<List<Conversation>> root=c.listConversations().toCompletableFuture();
                assertEquals(CompletableFuture.class,root.getClass());
                if(i%2==0)root.cancel(false);else root.complete(Collections.emptyList());
            }
            await(called);assertEquals(4,a.callerReservations());assertEquals(4,a.operationReservations());
            rejected(c.listConversations());c.invalidate();until(a,()->a.callerReservations()==0);
            assertEquals(4,a.operationReservations());assertFalse(remote.isCancelled());
            assertFalse(runtime.awaitTermination(20,TimeUnit.MILLISECONDS));
        } finally {remote.complete(Collections.emptyList());runtime.close();}
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void standardInlineExplicitExecutorRunsOnSettlementWorker() throws Exception {
        MessagingExecution runtime=new MessagingExecution();MessagingExecution.AccountLease a=lease(runtime);
        CompletableFuture<List<Conversation>> remote=new CompletableFuture<>();
        MessagingController c=controller(a,repo((p,m,args)->remote));
        Thread main=Thread.currentThread();
        CompletionStage<Thread> checked=c.listConversations().handleAsync((v,e)->{
            assertTrue(e instanceof CancellationException);assertFalse(Thread.holdsLock(a.gate));
            return Thread.currentThread();
        },Runnable::run);
        try {
            c.invalidate();Thread worker=result(checked);assertNotSame(main,worker);
            assertTrue(worker.getName().startsWith("messaging-settlement-"));
        } finally {remote.complete(Collections.emptyList());runtime.close();}
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void leaseCanBeReusedOnlyAfterActualRetirement() throws Exception {
        MessagingExecution runtime=new MessagingExecution();MessagingExecution.AccountLease a=lease(runtime),b=lease(runtime);
        CompletableFuture<List<Conversation>> remote=new CompletableFuture<>();CountDownLatch called=new CountDownLatch(1);
        MessagingController c=controller(a,repo((p,m,args)->{called.countDown();return remote;}));
        try {
            c.listConversations();await(called);c.invalidate();
            assertThrows(RejectedExecutionException.class,()->lease(runtime));
            remote.complete(Collections.emptyList());a.close();b.close();
            assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
            MessagingExecution.AccountLease next=lease(runtime);
            assertEquals(1,runtime.liveLeases());next.close();
        } finally {remote.complete(Collections.emptyList());runtime.close();}
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
    }
    @Test public void ownershipDuplicateAndShutdownRejection() throws Exception {
        MessagingExecution runtime=new MessagingExecution();MessagingExecution.AccountLease a=lease(runtime);
        try {
            assertThrows(RejectedExecutionException.class,()->runtime.acquire(a.userId,a.epoch));
            ConversationRepository repo=repo((p,m,args)->CompletableFuture.completedFuture(Collections.emptyList()));
            assertThrows(IllegalArgumentException.class,()->new MessagingController(UUID.randomUUID(),a.epoch,repo,a));
            controller(a,repo);
            assertThrows(IllegalArgumentException.class,()->controller(a,repo));
        } finally {runtime.close();}
        assertTrue(runtime.awaitTermination(5,TimeUnit.SECONDS));
        assertThrows(RejectedExecutionException.class,()->lease(runtime));
    }
}
