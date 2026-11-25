package com.nexa.chat.feature.groups.data;

import static org.junit.Assert.*;

import com.nexa.chat.feature.groups.data.GroupRequestLifecycleTest.Pending;
import com.nexa.chat.feature.groups.data.GroupRequestLifecycleTest.Result;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;

public class GroupRequestRejectionTest {
  private final Queue<Runnable> work = new ConcurrentLinkedQueue<>();
  private final AtomicLong now = new AtomicLong();
  private final List<RemoteGroupRepository> repositories = new ArrayList<>();

  private RemoteGroupRepository repository(Executor executor, GroupRequestRuntime[] out) {
    GroupRequestRuntime runtime = new GroupRequestRuntime(executor, now::get);
    if (out != null) out[0] = runtime;
    return repository(runtime);
  }

  private RemoteGroupRepository repository(GroupRequestRuntime runtime) {
    GroupApi api =
        (GroupApi)
            java.lang.reflect.Proxy.newProxyInstance(
                GroupApi.class.getClassLoader(),
                new Class[] {GroupApi.class},
                (p, m, a) -> {
                  throw new AssertionError("No API fixture");
                });
    RemoteGroupRepository repo = new RemoteGroupRepository(api, runtime, 100, 50);
    repositories.add(repo);
    return repo;
  }

  private void drain() {
    int count = 0;
    while (!work.isEmpty()) {
      assertTrue(++count < 1000);
      work.remove().run();
    }
  }

  @After
  public void cleanup() {
    for (RemoteGroupRepository r : repositories) r.close();
    drain();
  }

  @Test
  public void admittedDispatchRejectionSettlesAndRecoversAccountCapacity() {
    AtomicBoolean reject = new AtomicBoolean(true);
    RemoteGroupRepository repo =
        repository(
            r -> {
              if (reject.getAndSet(false))
                throw new RejectedExecutionException("dispatch rejected");
              work.add(r);
            },
            null);
    Result<String> result = new Result<>();
    GroupRequestHandle h =
        repo.execute(
            () -> {
              fail("Rejected factory");
              return null;
            },
            result);
    drain();
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
    assertEquals(0, repo.inFlight());
    assertEquals("REQUEST_DISPATCH_REJECTED", result.error);
    assertEquals(GroupRequestHandle.Outcome.DISPATCH_REJECTED, h.outcome());
  }

  private void rejectionRace(boolean timeout) throws Exception {
    CountDownLatch submitted = new CountDownLatch(1), rejectNow = new CountDownLatch(1);
    AtomicBoolean first = new AtomicBoolean(true);
    GroupRequestRuntime[] runtime = new GroupRequestRuntime[1];
    RemoteGroupRepository repo =
        repository(
            r -> {
              if (first.getAndSet(false)) {
                submitted.countDown();
                try {
                  assertTrue(rejectNow.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                  throw new AssertionError(e);
                }
                throw new RejectedExecutionException("controlled dispatch rejection");
              }
              work.add(r);
            },
            runtime);
    ExecutorService caller = Executors.newSingleThreadExecutor();
    Result<String> result = new Result<>();
    try {
      Future<GroupRequestHandle> future =
          caller.submit(
              () ->
                  repo.execute(
                      () -> {
                        fail("Rejected dispatch must never invoke factory");
                        return null;
                      },
                      result));
      assertTrue(submitted.await(5, TimeUnit.SECONDS));
      GroupRequestHandle h = repo.requests().get(0);
      if (timeout) {
        now.set(100);
        runtime[0].poll();
      } else h.cancel();
      rejectNow.countDown();
      future.get(5, TimeUnit.SECONDS);
      drain();
      if (timeout) {
        assertEquals("REQUEST_TIMEOUT", result.error);
        assertEquals(1, result.failures);
      } else assertEquals(0, result.failures + result.successes);
      assertEquals(GroupRequestHandle.State.SETTLED, h.state());
      assertEquals(0, repo.inFlight());
    } finally {
      rejectNow.countDown();
      caller.shutdownNow();
    }
  }

  @Test
  public void dispatchRejectionAfterExplicitCancelDoesNotLeak() throws Exception {
    rejectionRace(false);
  }

  @Test
  public void dispatchRejectionPreservesAlreadySelectedTimeout() throws Exception {
    rejectionRace(true);
  }

  @Test
  public void rejectedCancelDoesNotDiscardTimeoutOrFabricateTermination() {
    AtomicInteger submission = new AtomicInteger();
    GroupRequestRuntime[] runtime = new GroupRequestRuntime[1];
    RemoteGroupRepository repo =
        repository(
            r -> {
              if (submission.incrementAndGet() == 2)
                throw new RejectedExecutionException("cancel rejected");
              work.add(r);
            },
            runtime);
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    drain();
    now.set(100);
    runtime[0].poll();
    drain();
    assertEquals("REQUEST_TIMEOUT", result.error);
    assertEquals(1, result.failures);
    assertEquals(0, call.cancels);
    assertEquals(GroupRequestHandle.State.CANCELLATION_REQUESTED, h.state());
    assertEquals(1, repo.inFlight());
    now.set(150);
    runtime[0].poll();
    assertEquals(1, repo.inFlight());
    call.success("late");
    drain();
    assertEquals(0, result.successes);
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void rejectedResultAfterTerminalSuppressesWithoutDuplicateDelivery() {
    AtomicInteger submission = new AtomicInteger();
    RemoteGroupRepository repo =
        repository(
            r -> {
              if (submission.incrementAndGet() == 2)
                throw new RejectedExecutionException("result rejected");
              work.add(r);
            },
            null);
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    drain();
    call.success("result");
    call.failure();
    drain();
    assertEquals(0, result.successes + result.failures);
    assertEquals(0, repo.inFlight());
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
    assertEquals(GroupRequestHandle.Delivery.SUPPRESSED, h.delivery());
  }

  @Test
  public void queuedThenRejectedDispatchCannotExecuteLateFactory() {
    AtomicBoolean first = new AtomicBoolean(true);
    RemoteGroupRepository repo =
        repository(
            r -> {
              work.add(r);
              if (first.getAndSet(false))
                throw new RejectedExecutionException("queued then rejected");
            },
            null);
    AtomicInteger factoryCalls = new AtomicInteger();
    Result<String> result = new Result<>();
    GroupRequestHandle h =
        repo.execute(
            () -> {
              factoryCalls.incrementAndGet();
              return new Pending<String>();
            },
            result);
    drain();
    assertEquals(0, factoryCalls.get());
    assertEquals("REQUEST_DISPATCH_REJECTED", result.error);
    assertEquals(1, result.failures);
    assertEquals(0, repo.inFlight());
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
  }

  @Test
  public void explicitCancellationTaskRejectionRetainsUnknownTransport() {
    AtomicInteger submitted = new AtomicInteger();
    RemoteGroupRepository repo =
        repository(
            r -> {
              if (submitted.incrementAndGet() == 2)
                throw new RejectedExecutionException("cancel rejection");
              work.add(r);
            },
            null);
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    drain();
    h.cancel();
    drain();
    assertEquals(0, call.cancels);
    assertEquals(1, repo.inFlight());
    assertEquals(GroupRequestHandle.State.CANCELLATION_REQUESTED, h.state());
    assertTrue(
        repo.diagnostics().snapshot().stream()
            .anyMatch(e -> e.code.equals("CANCEL_EXECUTOR_REJECTED")));
    call.success("late");
    call.failure();
    drain();
    assertEquals(0, result.successes + result.failures);
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void rejectedTimeoutResultRemainsObservableWithoutInlineFallback() {
    AtomicInteger submitted = new AtomicInteger();
    GroupRequestRuntime[] runtime = new GroupRequestRuntime[1];
    RemoteGroupRepository repo =
        repository(
            r -> {
              if (submitted.incrementAndGet() == 3)
                throw new RejectedExecutionException("timeout result rejected");
              work.add(r);
            },
            runtime);
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    drain();
    now.set(100);
    runtime[0].poll();
    drain();
    assertEquals(GroupRequestHandle.Outcome.TIMEOUT, h.outcome());
    assertEquals(GroupRequestHandle.Delivery.SUPPRESSED, h.delivery());
    assertEquals(0, result.successes + result.failures);
    assertEquals(1, repo.inFlight());
    assertTrue(
        repo.diagnostics().snapshot().stream()
            .anyMatch(e -> e.code.equals("TIMEOUT_RESULT_EXECUTOR_REJECTED")));
    call.failure();
    drain();
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void inlineResultThenExecutorThrowDoesNotDeliverTwice() {
    AtomicInteger submitted = new AtomicInteger();
    RemoteGroupRepository repo =
        repository(
            r -> {
              int n = submitted.incrementAndGet();
              r.run();
              if (n == 2) throw new RejectedExecutionException("after inline result");
            },
            null);
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    call.success("one");
    call.success("duplicate");
    assertEquals(1, result.successes);
    assertEquals(0, result.failures);
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
    assertEquals(0, repo.inFlight());
    assertTrue(
        repo.diagnostics().snapshot().stream()
            .anyMatch(e -> e.code.equals("RESULT_EXECUTOR_FAILURE_AFTER_START")));
  }

  @Test
  public void inlineCancelThenExecutorThrowStillNeedsTransportTerminal() {
    AtomicInteger submitted = new AtomicInteger();
    RemoteGroupRepository repo =
        repository(
            r -> {
              int n = submitted.incrementAndGet();
              r.run();
              if (n == 2) throw new RejectedExecutionException("after inline cancel");
            },
            null);
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    h.cancel();
    assertEquals(1, call.cancels);
    assertEquals(1, repo.inFlight());
    call.failure();
    call.success("late");
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
    assertEquals(0, repo.inFlight());
    assertEquals(0, result.successes + result.failures);
    assertTrue(
        repo.diagnostics().snapshot().stream()
            .anyMatch(e -> e.code.equals("CANCEL_EXECUTOR_FAILURE_AFTER_START")));
  }

  @Test
  public void allSubmissionsRejectedNeverLeaksAccountCapacity() {
    AtomicBoolean reject = new AtomicBoolean(true);
    RemoteGroupRepository repo =
        repository(
            r -> {
              if (reject.get()) throw new RejectedExecutionException("all work rejected");
              work.add(r);
            },
            null);
    for (int i = 0; i < 80; i++) {
      GroupRequestHandle h =
          repo.execute(
              () -> {
                fail("rejected factory");
                return null;
              },
              new Result<>());
      assertEquals(GroupRequestHandle.State.SETTLED, h.state());
      assertEquals(GroupRequestHandle.Delivery.SUPPRESSED, h.delivery());
      assertEquals(0, repo.inFlight());
    }
    reject.set(false);
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    repo.execute(() -> call, result);
    drain();
    call.success("recovered");
    drain();
    assertEquals("recovered", result.value);
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void cancelledRejectedDispatchesReleaseGlobalCapacityExactlyOnce() {
    AtomicReference<RemoteGroupRepository> active = new AtomicReference<>();
    AtomicBoolean rejectionMode = new AtomicBoolean(true);
    GroupRequestRuntime runtime =
        new GroupRequestRuntime(
            r -> {
              if (rejectionMode.get()) {
                active.get().requests().get(0).cancel();
                throw new RejectedExecutionException("cancel raced dispatch rejection");
              }
              work.add(r);
            },
            now::get);
    RemoteGroupRepository first = repository(runtime);
    active.set(first);
    for (int i = 0; i < 80; i++) {
      GroupRequestHandle h =
          first.execute(
              () -> {
                fail("must not dispatch");
                return null;
              },
              new Result<>());
      assertEquals(GroupRequestHandle.State.SETTLED, h.state());
      assertEquals(0, first.inFlight());
    }
    rejectionMode.set(false);
    List<GroupRequestHandle> live = new ArrayList<>();
    for (int n = 0; n < 4; n++) {
      RemoteGroupRepository account = repository(runtime);
      for (int i = 0; i < 16; i++)
        live.add(account.execute(() -> new Pending<String>(), new Result<>()));
    }
    RemoteGroupRepository overflow = repository(runtime);
    assertThrows(
        RejectedExecutionException.class,
        () -> overflow.execute(() -> new Pending<String>(), new Result<>()));
    for (GroupRequestHandle h : live) h.cancel();
    drain();
    GroupRequestHandle recovered = overflow.execute(() -> new Pending<String>(), new Result<>());
    recovered.cancel();
    drain();
    assertEquals(GroupRequestHandle.State.SETTLED, recovered.state());
  }

  @Test
  public void explicitCancelWithdrawsQueuedTimeoutButRetainsUnknownPhysicalWork() {
    GroupRequestRuntime[] runtime = new GroupRequestRuntime[1];
    RemoteGroupRepository repo = repository(work::add, runtime);
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    drain();
    now.set(100);
    runtime[0].poll();
    h.cancel();
    drain();
    assertEquals(GroupRequestHandle.Outcome.TIMEOUT, h.outcome());
    assertEquals(GroupRequestHandle.Delivery.SUPPRESSED, h.delivery());
    assertEquals(0, result.successes + result.failures);
    assertEquals(1, repo.inFlight());
    call.success("too late");
    drain();
    assertEquals(0, repo.inFlight());
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
  }

  @Test
  public void executorRunsInlineThenThrowsCannotDoubleReleaseOrFabricateTermination() {
    AtomicBoolean first = new AtomicBoolean(true);
    RemoteGroupRepository repo =
        repository(
            r -> {
              r.run();
              if (first.getAndSet(false))
                throw new RejectedExecutionException("throw after execution");
            },
            null);
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    assertEquals(GroupRequestHandle.State.STARTED, h.state());
    assertEquals(1, repo.inFlight());
    call.success("one");
    call.failure();
    assertEquals(1, result.successes);
    assertEquals(0, result.failures);
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
    assertEquals(0, repo.inFlight());
  }
}
