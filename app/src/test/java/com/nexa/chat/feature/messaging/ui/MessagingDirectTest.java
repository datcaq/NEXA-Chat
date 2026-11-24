package com.nexa.chat.feature.messaging.ui;

import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import org.junit.Test;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.BiConsumer;
import static org.junit.Assert.*;

public class MessagingDirectTest {
    @org.junit.After public void closeScopes() { com.nexa.chat.feature.messaging.ui.TestScopes.closeAll(); }
    private final List<String> peers = new CopyOnWriteArrayList<>();
    private final List<Reply> replies = new CopyOnWriteArrayList<>();
    private RuntimeException thrown;
    private final ConversationRepository repo = (ConversationRepository) Proxy.newProxyInstance(
            ConversationRepository.class.getClassLoader(), new Class<?>[]{ConversationRepository.class},
            (proxy, method, args) -> {
                assertFalse(Thread.holdsLock(this.controller.getRouter().gate));
                if (!method.getName().equals("openDirect")) throw new AssertionError(method.getName());
                peers.add((String) args[0]);
                if (thrown != null) throw thrown;
                Reply reply = new Reply(); replies.add(reply); return reply;
            });
    private final MessagingController controller = com.nexa.chat.feature.messaging.ui.TestScopes.create(UUID.randomUUID(), UUID.randomUUID(), repo);
    private final MessagingRouter router = controller.getRouter();
    private Conversation chat(String id) { return new Conversation(id, ConversationType.DIRECT); }
    private static <T> T result(CompletionStage<T> stage) throws Exception {
        return stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }
    private static void cancelled(CompletionStage<?> stage) throws Exception {
        try { result(stage); fail("Expected cancellation"); }
        catch (CancellationException expected) { }
    }
    private static void failed(CompletionStage<?> stage) throws Exception {
        try { result(stage); fail("Expected failure"); }
        catch (ExecutionException expected) { assertTrue(expected.getCause() instanceof IllegalStateException); }
    }
    @Test public void samePendingPeerSharesOperationAndTransitionsOnce() throws Exception {
        AtomicInteger signals = new AtomicInteger();
        router.observe(Runnable::run, () -> { assertFalse(Thread.holdsLock(router.gate)); signals.incrementAndGet(); });
        CompletionStage<Conversation> a = controller.openDirectAndNavigate("A");
        CompletionStage<Conversation> b = controller.openDirectAndNavigate("A");
        assertNotSame(a, b); assertEquals(Collections.singletonList("A"), peers);
        Conversation value = chat("A"); replies.get(0).complete(value);
        assertSame(value, result(a)); assertSame(value, result(b));
        assertEquals("A", router.currentRoute().getConversationId());
        assertEquals(1, router.currentRoute().getRevision()); assertEquals(2, signals.get());
    }
    @Test public void concurrentDuplicateReservesOneOperation() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<CompletionStage<Conversation>> open = () -> { assertTrue(start.await(5, TimeUnit.SECONDS)); return controller.openDirectAndNavigate("A"); };
            Future<CompletionStage<Conversation>> a = pool.submit(open), b = pool.submit(open);
            start.countDown();
            CompletionStage<Conversation> x = a.get(5, TimeUnit.SECONDS), y = b.get(5, TimeUnit.SECONDS);
            assertEquals(1, peers.size()); replies.get(0).complete(chat("A"));
            assertEquals("A", result(x).getId()); assertEquals("A", result(y).getId());
            assertEquals(1, router.currentRoute().getRevision());
        } finally { start.countDown(); pool.shutdownNow(); }
    }
    @Test public void differentPeersAndSupersededFutureStillSucceeds() throws Exception {
        CompletionStage<Conversation> a = controller.openDirectAndNavigate("A"), b = controller.openDirectAndNavigate("B");
        assertEquals(Arrays.asList("A", "B"), peers);
        replies.get(1).complete(chat("B")); replies.get(0).complete(chat("A"));
        assertEquals("A", result(a).getId()); assertEquals("B", result(b).getId());
        assertEquals("B", router.currentRoute().getConversationId()); assertEquals(1, router.currentRoute().getRevision());
    }
    @Test public void abaReusesAAndLateBCannotWin() throws Exception {
        CompletionStage<Conversation> a1 = controller.openDirectAndNavigate("A"), b = controller.openDirectAndNavigate("B"),
                a2 = controller.openDirectAndNavigate("A");
        assertEquals(Arrays.asList("A", "B"), peers);
        replies.get(0).complete(chat("A")); replies.get(1).complete(chat("B"));
        assertEquals("A", result(a1).getId()); assertEquals("A", result(a2).getId()); assertEquals("B", result(b).getId());
        assertEquals("A", router.currentRoute().getConversationId()); assertEquals(1, router.currentRoute().getRevision());
    }
    @Test public void backThenNewIntentReusesPendingOperation() throws Exception {
        CompletionStage<Conversation> first = controller.openDirectAndNavigate("A");
        assertFalse(router.handleBack());
        CompletionStage<Conversation> again = controller.openDirectAndNavigate("A");
        assertEquals(1, peers.size()); replies.get(0).complete(chat("A"));
        result(first); result(again); assertEquals("A", router.currentRoute().getConversationId());
    }
    @Test public void backWithoutNewIntentPreventsNavigationButNotResult() throws Exception {
        CompletionStage<Conversation> first = controller.openDirectAndNavigate("A");
        router.handleBack(); replies.get(0).complete(chat("A"));
        assertEquals("A", result(first).getId()); assertNull(router.currentRoute().getConversationId());
    }
    @Test public void errorThenRetryUsesNewIdentityAndOldCallbackCannotRemoveIt() throws Exception {
        CompletionStage<Conversation> first = controller.openDirectAndNavigate("A");
        Reply old = replies.get(0); old.completeExceptionally(new IllegalStateException("failed")); failed(first);
        CompletionStage<Conversation> retry = controller.openDirectAndNavigate("A");
        old.replay(chat("OLD"), null);
        CompletionStage<Conversation> duplicate = controller.openDirectAndNavigate("A");
        assertEquals(Arrays.asList("A", "A"), peers);
        assertFalse(retry.toCompletableFuture().isDone()); assertNull(router.currentRoute().getConversationId());
        replies.get(1).complete(chat("NEW"));
        assertEquals("NEW", result(retry).getId()); assertEquals("NEW", result(duplicate).getId());
        assertEquals("NEW", router.currentRoute().getConversationId());
    }
    @Test public void synchronousRepositoryThrowDoesNotStrandPendingEntry() throws Exception {
        thrown = new IllegalStateException("throw");
        CompletionStage<Conversation> first = controller.openDirectAndNavigate("A"); failed(first);
        thrown = null;
        CompletionStage<Conversation> retry = controller.openDirectAndNavigate("A");
        assertEquals(2, peers.size()); replies.get(0).complete(chat("A"));
        assertEquals("A", result(retry).getId()); assertEquals("A", router.currentRoute().getConversationId());
    }
    @Test public void invalidationCancelsEveryCallerAndLateCompletionCannotRestoreScope() throws Exception {
        CompletionStage<Conversation> a = controller.openDirectAndNavigate("A"), b = controller.openDirectAndNavigate("A");
        controller.invalidate(); controller.invalidate(); cancelled(a); cancelled(b);
        replies.get(0).complete(chat("PRIVATE"));
        assertFalse(controller.isActive()); assertNull(router.currentRoute().getConversationId()); assertEquals("", router.currentRoute().getTitle());
        cancelled(controller.openDirectAndNavigate("A")); assertEquals(1, peers.size());
    }
    @Test public void callerCancelDoesNotCancelSharedOperationOrOtherCaller() throws Exception {
        CompletionStage<Conversation> a = controller.openDirectAndNavigate("A"), b = controller.openDirectAndNavigate("A");
        assertTrue(a.toCompletableFuture().cancel(true));
        assertFalse(replies.get(0).isCancelled()); assertFalse(b.toCompletableFuture().isDone());
        replies.get(0).complete(chat("A"));
        cancelled(a); assertEquals("A", result(b).getId()); assertEquals(1, peers.size());
        assertEquals("A", router.currentRoute().getConversationId());
    }
    @Test public void callerCannotCompleteInternalOperation() throws Exception {
        CompletionStage<Conversation> a = controller.openDirectAndNavigate("A");
        a.toCompletableFuture().complete(chat("FORGED"));
        CompletionStage<Conversation> b = controller.openDirectAndNavigate("A");
        assertFalse(b.toCompletableFuture().isDone()); assertNull(router.currentRoute().getConversationId());
        assertEquals(1, peers.size()); replies.get(0).complete(chat("REAL"));
        assertEquals("FORGED", result(a).getId()); assertEquals("REAL", result(b).getId());
        assertEquals("REAL", router.currentRoute().getConversationId());
    }
    @Test public void subscriptionsAndRecreationDoNotDispatchAgain() throws Exception {
        CompletionStage<Conversation> a = controller.openDirectAndNavigate("A");
        for (int i = 0; i < 5; i++) router.observe(Runnable::run, () -> {}).close();
        assertEquals(1, peers.size()); replies.get(0).complete(chat("A")); result(a);
        assertEquals("A", router.currentRoute().getConversationId());
    }
    @Test public void nonNavigatingDirectSharesOperationWithoutTakingIntent() throws Exception {
        CompletionStage<Conversation> a = controller.openDirect("A"), b = controller.openDirectAndNavigate("A");
        assertEquals(1, peers.size()); replies.get(0).complete(chat("A")); result(a); result(b);
        assertEquals("A", router.currentRoute().getConversationId());
    }
    @Test public void invalidationBeforeQueuedDispatchSkipsRepository() throws Exception {
        Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
        MessagingController queued = com.nexa.chat.feature.messaging.ui.TestScopes.create(UUID.randomUUID(), UUID.randomUUID(), repo, queue::add);
        CompletionStage<Conversation> a = queued.openDirectAndNavigate("A");
        queued.invalidate(); queue.remove().run(); cancelled(a); assertTrue(peers.isEmpty());
    }
    @Test public void dispatchRejectionReleasesOperationAndAllowsRetry() throws Exception {
        AtomicBoolean reject = new AtomicBoolean(true);
        MessagingController scope = com.nexa.chat.feature.messaging.ui.TestScopes.create(UUID.randomUUID(), UUID.randomUUID(), repo, task -> {
            if (reject.get()) throw new RejectedExecutionException();
            task.run();
        });
        CompletionStage<Conversation> first = scope.openDirectAndNavigate("A");
        try { result(first); fail("Expected dispatch rejection"); }
        catch (ExecutionException expected) { assertTrue(expected.getCause() instanceof RejectedExecutionException); }
        assertTrue(peers.isEmpty()); reject.set(false);
        CompletionStage<Conversation> second = scope.openDirectAndNavigate("A");
        assertEquals(1, peers.size()); replies.get(0).complete(chat("A"));
        assertEquals("A", result(second).getId()); assertEquals("A", scope.getRouter().currentRoute().getConversationId());
    }
    private static final class Reply extends CompletableFuture<Conversation> {
        BiConsumer<? super Conversation, ? super Throwable> callback;
        @Override public CompletableFuture<Conversation> whenComplete(BiConsumer<? super Conversation, ? super Throwable> action) {
            callback = action; return super.whenComplete(action);
        }
        void replay(Conversation value, Throwable error) { callback.accept(value, error); }
    }
}
