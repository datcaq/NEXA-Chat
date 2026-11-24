package com.nexa.chat.feature.messaging.ui;

import org.junit.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

/** Standard JDK future contract; no custom publication policy or shadow state. */
public class MessagingFutureTest {
    private static <T> T result(CompletionStage<T> stage) throws Exception {
        return stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }
    @Test public void standardSuccessQueriesAndAggregatesAgree() throws Exception {
        CompletableFuture<String> root = new CompletableFuture<>();
        CompletableFuture<?> all = CompletableFuture.allOf(root), any = CompletableFuture.anyOf(root);
        CompletionStage<String> combined = root.thenCombine(root, String::concat);
        assertFalse(root.isDone()); assertEquals("pending", root.getNow("pending"));
        assertTrue(root.complete("ok"));
        assertEquals("ok", root.get()); assertEquals("ok", result(root)); assertEquals("ok", root.join());
        assertEquals("ok", root.getNow("other")); assertTrue(root.isDone());
        assertFalse(root.isCancelled()); assertFalse(root.isCompletedExceptionally());
        assertTrue(all.isDone()); assertEquals("ok", result(any)); assertEquals("okok", result(combined));
    }
    @Test public void standardExceptionalComposition() throws Exception {
        CompletableFuture<String> root = new CompletableFuture<>();
        CompletionStage<String> recovery = root.thenApply(String::trim).exceptionally(e -> "recovered")
                .thenCompose(v -> CompletableFuture.completedFuture(v + "!"));
        root.completeExceptionally(new IllegalStateException());
        assertTrue(root.isCompletedExceptionally()); assertFalse(root.isCancelled());
        assertThrows(CompletionException.class, root::join);
        assertThrows(ExecutionException.class, () -> root.get(5, TimeUnit.SECONDS));
        assertEquals("recovered!", result(recovery));
    }
    @Test public void standardCancellationQueriesAgree() {
        CompletableFuture<String> root = new CompletableFuture<>();
        root.cancel(false);
        assertTrue(root.isDone()); assertTrue(root.isCancelled()); assertTrue(root.isCompletedExceptionally());
        assertThrows(CancellationException.class, root::get);
        assertThrows(CancellationException.class, root::join);
        assertThrows(CancellationException.class, () -> root.getNow("fallback"));
    }
    @Test public void rejectingExplicitExecutorFailsDependentWithoutCallerRunsFallback() throws Exception {
        CompletableFuture<String> root = new CompletableFuture<>();
        CompletionStage<?> checked = root.handleAsync((v, e) -> { fail(); return null; },
                task -> { throw new RejectedExecutionException(); });
        root.cancel(false);
        try { result(checked); fail(); }
        catch (ExecutionException e) { assertTrue(e.getCause() instanceof RejectedExecutionException); }
        assertTrue(root.isCancelled());
    }
    @Test public void throwingCallbackFailureCanBeAwaited() throws Exception {
        CompletableFuture<String> root = new CompletableFuture<>();
        CompletionStage<?> checked = root.thenApply(v -> { throw new IllegalArgumentException(); });
        root.complete("ok");
        try { result(checked); fail(); }
        catch (ExecutionException e) { assertTrue(e.getCause() instanceof IllegalArgumentException); }
    }
    @Test public void handleAndWhenCompleteAreStandard() throws Exception {
        CompletableFuture<String> root = new CompletableFuture<>();
        AtomicInteger calls = new AtomicInteger();
        CompletionStage<String> checked = root.whenComplete((v, e) -> {
            assertEquals("ok", v); assertNull(e); calls.incrementAndGet();
        }).handle((v, e) -> { assertNull(e); return v; });
        root.complete("ok"); assertEquals("ok", result(checked)); assertEquals(1, calls.get());
    }
    @Test public void copyAndMinimalStageKeepStandardOutcome() throws Exception {
        CompletableFuture<String> root = new CompletableFuture<>();
        CompletionStage<String> copy = root.copy(), minimal = root.minimalCompletionStage();
        root.complete("ok"); assertEquals("ok", result(copy)); assertEquals("ok", result(minimal));
    }
    @Test public void concurrentFirstWinnerIsAuthoritative() throws Exception {
        CompletableFuture<String> root = new CompletableFuture<>();
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> a = workers.submit(() -> { assertTrue(start.await(5, TimeUnit.SECONDS)); return root.complete("A"); });
            Future<Boolean> b = workers.submit(() -> { assertTrue(start.await(5, TimeUnit.SECONDS)); return root.complete("B"); });
            start.countDown(); assertNotEquals(a.get(5, TimeUnit.SECONDS), b.get(5, TimeUnit.SECONDS));
            assertEquals(root.join(), result(root)); assertEquals(root.join(), result(CompletableFuture.anyOf(root)));
        } finally { start.countDown(); workers.shutdownNow(); }
    }
    @Test public void standardObtrudeSemantics() {
        CompletableFuture<String> root = CompletableFuture.completedFuture("first");
        assertFalse(root.complete("second")); root.obtrudeValue("replacement"); assertEquals("replacement", root.join());
        root.obtrudeException(new IllegalArgumentException());
        assertThrows(CompletionException.class, root::join);
    }
    @Test public void explicitAsyncConsumerWaitsForItsExecutor() throws Exception {
        CompletableFuture<String> root = new CompletableFuture<>();
        java.util.Queue<Runnable> ui = new ConcurrentLinkedQueue<>();
        CompletionStage<String> consumer = root.thenApplyAsync(String::trim, ui::add);
        root.complete(" ok ");
        assertTrue(root.isDone()); assertFalse(consumer.toCompletableFuture().isDone());
        ui.remove().run(); assertEquals("ok", result(consumer));
    }
    @Test public void newerExceptionalCompositionUsesStandardInlineExecutorPolicy() throws Exception {
        CompletableFuture<String> root = new CompletableFuture<>();
        Thread caller = Thread.currentThread();
        CompletionStage<String> recovery = root.exceptionallyComposeAsync(error -> {
            assertSame(caller, Thread.currentThread());
            return CompletableFuture.completedFuture("ok");
        }, Runnable::run);
        root.cancel(false); assertEquals("ok", result(recovery));
    }
    @Test public void cancellationFailureAndSuccessRaceHasOneOutcome() throws Exception {
        CompletableFuture<String> root = new CompletableFuture<>();
        ExecutorService workers = Executors.newFixedThreadPool(3);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?> success = workers.submit(() -> { await(start); root.complete("ok"); });
            Future<?> failure = workers.submit(() -> { await(start); root.completeExceptionally(new IllegalStateException()); });
            Future<?> cancel = workers.submit(() -> { await(start); root.cancel(false); });
            start.countDown();
            success.get(5, TimeUnit.SECONDS); failure.get(5, TimeUnit.SECONDS); cancel.get(5, TimeUnit.SECONDS);
            CompletionStage<Void> checked = root.handle((value, error) -> {
                assertTrue(root.isDone());
                if (error == null) { assertFalse(root.isCompletedExceptionally()); assertEquals("ok", root.getNow("fallback")); }
                else { assertTrue(root.isCompletedExceptionally()); assertEquals(error instanceof CancellationException, root.isCancelled()); }
                return null;
            });
            result(checked);
        } finally { start.countDown(); workers.shutdownNow(); }
    }
    private static void await(CountDownLatch latch) {
        try { assertTrue(latch.await(5, TimeUnit.SECONDS)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }
    @Test public void newerJvmQueriesUseSameStateWhenAvailable() throws Exception {
        CompletableFuture<String> root = CompletableFuture.completedFuture("ok");
        try {
            assertEquals("SUCCESS", CompletableFuture.class.getMethod("state").invoke(root).toString());
            assertEquals("ok", CompletableFuture.class.getMethod("resultNow").invoke(root));
            IllegalStateException error = new IllegalStateException();
            CompletableFuture<String> failed = new CompletableFuture<>(); failed.completeExceptionally(error);
            assertSame(error, CompletableFuture.class.getMethod("exceptionNow").invoke(failed));
        } catch (NoSuchMethodException olderJvm) { org.junit.Assume.assumeNoException(olderJvm); }
    }
}
