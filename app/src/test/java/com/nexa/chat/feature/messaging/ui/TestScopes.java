package com.nexa.chat.feature.messaging.ui;

import com.nexa.chat.feature.messaging.repository.ConversationRepository;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/** Legacy reducer fixtures use inline dispatch; new runtime tests use production dispatch. */
public final class TestScopes {
    private static final List<MessagingExecution> runtimes = new ArrayList<>();
    private static final List<CompletableFuture<?>> sources = new CopyOnWriteArrayList<>();
    private static final Set<Runnable> queued = ConcurrentHashMap.newKeySet();
    public static MessagingController create(UUID user, UUID epoch, ConversationRepository repository) {
        return create(user, epoch, repository, Runnable::run);
    }
    public static synchronized MessagingController create(UUID user, UUID epoch,
            ConversationRepository repository, Executor dispatcher) {
        MessagingExecution runtime = new MessagingExecution();
        runtimes.add(runtime);
        MessagingExecution.AccountLease lease = runtime.acquire(user, epoch);
        lease.testDispatch = work -> {
            Runnable tracked = new Runnable() {
                public void run() { if (queued.remove(this)) work.run(); }
            };
            queued.add(tracked);
            try { dispatcher.execute(tracked); }
            catch (RuntimeException error) { queued.remove(tracked); throw error; }
        };
        ConversationRepository tracked = (ConversationRepository) Proxy.newProxyInstance(
                ConversationRepository.class.getClassLoader(), new Class<?>[]{ConversationRepository.class},
                (proxy, method, args) -> {
                    try {
                        Object stage = method.invoke(repository, args);
                        if (stage instanceof CompletableFuture<?>) sources.add((CompletableFuture<?>) stage);
                        return stage;
                    } catch (InvocationTargetException failure) { throw failure.getCause(); }
                });
        return new MessagingController(user, epoch, tracked, lease);
    }
    public static synchronized void closeAll() {
        try {
            for (MessagingExecution runtime : runtimes) runtime.close();
            for (Runnable task : new ArrayList<>(queued)) task.run();
            // These are test-owned controllable fakes; acknowledge their actual source termination.
            for (CompletableFuture<?> source : sources) source.complete(null);
            for (MessagingExecution runtime : runtimes)
                org.junit.Assert.assertTrue("Fixture lease did not retire",
                        runtime.awaitTermination(5, TimeUnit.SECONDS));
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt(); throw new AssertionError(error);
        } finally { runtimes.clear(); sources.clear(); queued.clear(); }
    }
}
