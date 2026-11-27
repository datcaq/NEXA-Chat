package com.nexa.chat.feature.messaging.ui;

import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import org.junit.Test;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

public class MessagingCleanupTest {
    @org.junit.After public void closeScopes() { com.nexa.chat.feature.messaging.ui.TestScopes.closeAll(); }
    private final CompletableFuture<List<Conversation>> remote = new CompletableFuture<>();
    private final ConversationRepository repository = (ConversationRepository) Proxy.newProxyInstance(
            ConversationRepository.class.getClassLoader(), new Class<?>[]{ConversationRepository.class},
            (proxy, method, args) -> remote);
    private final MessagingController controller = com.nexa.chat.feature.messaging.ui.TestScopes.create(UUID.randomUUID(), UUID.randomUUID(), repository);
    private final MessagingRouter router = controller.getRouter();
    private static void await(CountDownLatch latch) {
        try { assertTrue(latch.await(5, TimeUnit.SECONDS)); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new AssertionError(error); }
    }
    private static void cancelled(CompletionStage<?> future) throws Exception {
        try { future.toCompletableFuture().get(5, TimeUnit.SECONDS); fail("Expected cancellation"); }
        catch (CancellationException expected) { }
    }
    @Test public void blockedCancellationCannotHoldTeardownOrInactiveTerminalCleanup() throws Exception {
        Queue<Runnable> ui = new ConcurrentLinkedQueue<>();
        AtomicInteger cleared = new AtomicInteger();
        MessagingPresentation presentation = new MessagingPresentation(router);
        presentation.setAttached(true); router.openConversation("PRIVATE", "Private title");
        controller.editDraft("PRIVATE", "Private draft");
        assertFalse(presentation.isActive());
        controller.observe(task -> { assertFalse(Thread.holdsLock(router.gate)); ui.add(task); },
                () -> fail("Private notification must be replaced"), () -> {
                    assertFalse(Thread.holdsLock(router.gate)); cleared.incrementAndGet();
                });
        CompletionStage<List<Conversation>> pending = controller.listConversations();
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicReference<Thread> callbackThread = new AtomicReference<>();
        CompletionStage<?> checked = pending.handle((value, error) -> {
            assertTrue(error instanceof CancellationException);
            assertFalse(Thread.holdsLock(router.gate));
            callbackThread.set(Thread.currentThread()); entered.countDown(); await(release);
            return null;
        });
        ExecutorService main = Executors.newSingleThreadExecutor();
        try {
            AtomicReference<Thread> caller = new AtomicReference<>();
            main.submit(() -> { caller.set(Thread.currentThread()); controller.invalidate(); }).get(5, TimeUnit.SECONDS);
            await(entered);
            Runnable task; while ((task = ui.poll()) != null) task.run();
            assertEquals(1, cleared.get()); assertNotSame(caller.get(), callbackThread.get());
            assertFalse(controller.isActive()); assertFalse(presentation.canType("PRIVATE", true));
            assertNull(router.currentRoute().getConversationId()); assertEquals("", router.currentRoute().getTitle());
            assertEquals("", controller.draft("PRIVATE")); cancelled(pending);
            controller.invalidate(); assertEquals(1, cleared.get());
        } finally { release.countDown(); main.shutdownNow(); }
        // handle replaces cancellation with success; any assertion failure must fail this await.
        checked.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }
    @Test public void allCallbackWorkersBlockedStillCannotStrandRootFuturesOrTerminal() throws Exception {
        CountDownLatch entered = new CountDownLatch(4), release = new CountDownLatch(1);
        List<CompletionStage<?>> roots = new ArrayList<>(), callbacks = new ArrayList<>();
        Queue<Runnable> ui = new ConcurrentLinkedQueue<>();
        AtomicInteger terminal = new AtomicInteger();
        controller.observe(ui::add, () -> {}, terminal::incrementAndGet);
        try {
            for (int i = 0; i < 4; i++) {
                CompletionStage<?> root = controller.listConversations();
                roots.add(root);
                callbacks.add(root.handle((value, error) -> {
                    entered.countDown(); await(release); return null;
                }));
            }
            remote.complete(Collections.emptyList());
            await(entered);
            for (CompletionStage<?> root : roots) assertTrue(root.toCompletableFuture().isDone());
            try { controller.listConversations().toCompletableFuture().get(5, TimeUnit.SECONDS); fail(); }
            catch (ExecutionException expected) { assertTrue(expected.getCause() instanceof RejectedExecutionException); }
            controller.invalidate();
            Runnable task; while ((task = ui.poll()) != null) task.run();
            assertEquals(1, terminal.get()); assertFalse(controller.isActive());
        } finally { release.countDown(); }
        for (CompletionStage<?> callback : callbacks) callback.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }
    @Test public void oldTerminalCannotTargetReplacementScope() throws Exception {
        Queue<Runnable> ui = new ConcurrentLinkedQueue<>();
        AtomicInteger oldClears = new AtomicInteger(), newClears = new AtomicInteger();
        MessagingController.ViewSubscription old = controller.observe(ui::add, () -> {}, oldClears::incrementAndGet);
        CompletionStage<?> request = controller.listConversations(); controller.invalidate(); old.close();
        MessagingController next = com.nexa.chat.feature.messaging.ui.TestScopes.create(UUID.randomUUID(), UUID.randomUUID(), repository);
        next.getRouter().openConversation("NEW");
        next.observe(ui::add, () -> {}, newClears::incrementAndGet);
        remote.complete(Collections.emptyList());
        Runnable task; while ((task = ui.poll()) != null) task.run();
        cancelled(request); assertEquals(0, oldClears.get()); assertEquals(0, newClears.get());
        assertEquals("NEW", next.getRouter().currentRoute().getConversationId());
    }
    @Test public void terminalExecutorRejectionClosesHandleAndDoesNotStrandCancellation() throws Exception {
        AtomicInteger executes = new AtomicInteger(), cleared = new AtomicInteger();
        Executor executor = task -> {
            assertFalse(Thread.holdsLock(router.gate));
            if (executes.incrementAndGet() == 1) task.run();
            else throw new RejectedExecutionException();
        };
        MessagingController.ViewSubscription handle = controller.observe(executor, () -> {}, cleared::incrementAndGet);
        CompletionStage<?> request = controller.listConversations(); controller.invalidate(); cancelled(request);
        assertFalse(handle.isOpen()); assertEquals(0, cleared.get()); assertEquals(2, executes.get());
    }
    @Test public void throwingTerminalDoesNotPreventOtherTerminalOrCancellation() throws Exception {
        AtomicInteger cleared = new AtomicInteger();
        MessagingController.ViewSubscription broken = controller.observe(Runnable::run, () -> {}, () -> { throw new IllegalStateException(); });
        controller.observe(Runnable::run, () -> {}, cleared::incrementAndGet);
        CompletionStage<?> request = controller.listConversations(); controller.invalidate(); cancelled(request);
        assertFalse(broken.isOpen()); assertEquals(1, cleared.get());
    }
    @Test public void claimedObserverMayFinishAfterCloseWithoutHoldingGate() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        ExecutorService worker = Executors.newSingleThreadExecutor();
        AtomicInteger calls = new AtomicInteger();
        MessagingController.ViewSubscription handle = controller.observe(worker, () -> {
            assertFalse(Thread.holdsLock(router.gate)); entered.countDown(); await(release); calls.incrementAndGet();
        });
        try { await(entered); handle.close(); controller.invalidate(); assertFalse(controller.isActive()); }
        finally { release.countDown(); worker.shutdown(); }
        assertTrue(worker.awaitTermination(5, TimeUnit.SECONDS)); assertEquals(1, calls.get());
    }
}
