package com.nexa.chat.feature.groups.data;

import static org.junit.Assert.*;

import com.nexa.chat.feature.groups.*;
import com.nexa.chat.feature.groups.repository.GroupRepository;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import okio.Timeout;
import org.junit.*;
import retrofit2.*;

public class GroupRequestLifecycleTest {
  final Queue<Runnable> work = new ConcurrentLinkedQueue<>();
  final AtomicLong time = new AtomicLong();
  final GroupRequestRuntime runtime = new GroupRequestRuntime(work::add, time::get);
  RemoteGroupRepository repo;

  @Before
  public void setup() {
    repo =
        new RemoteGroupRepository(
            (GroupApi)
                java.lang.reflect.Proxy.newProxyInstance(
                    GroupApi.class.getClassLoader(),
                    new Class[] {GroupApi.class},
                    (p, m, a) -> {
                      throw new AssertionError("Unexpected API factory");
                    }),
            runtime,
            100,
            50);
  }

  void drain() {
    int guard = 0;
    while (!work.isEmpty()) {
      assertTrue("bounded tasks", ++guard < 1000);
      work.remove().run();
    }
  }

  @After
  public void close() {
    repo.close();
    drain();
  }

  static final class Result<T> implements GroupRepository.Result<T> {
    int successes, failures;
    String error;
    T value;

    public void success(T value) {
      this.value = value;
      successes++;
    }

    public void failure(String code) {
      error = code;
      failures++;
    }
  }

  static final class Pending<T> implements Call<T> {
    volatile Callback<T> callback;
    int enqueues, cancels;
    Runnable enqueueHook = () -> {}, cancelHook = () -> {};

    public Response<T> execute() {
      throw new UnsupportedOperationException();
    }

    public void enqueue(Callback<T> callback) {
      this.callback = callback;
      enqueues++;
      enqueueHook.run();
    }

    public boolean isExecuted() {
      return enqueues > 0;
    }

    public void cancel() {
      cancels++;
      cancelHook.run();
    }

    public boolean isCanceled() {
      return cancels > 0;
    }

    public Call<T> clone() {
      return new Pending<>();
    }

    public okhttp3.Request request() {
      return new okhttp3.Request.Builder().url("https://example.invalid/").build();
    }

    public Timeout timeout() {
      return Timeout.NONE;
    }

    void success(T value) {
      callback.onResponse(this, Response.success(value));
    }

    void failure() {
      callback.onFailure(this, new IOException("injected"));
    }
  }

  @Test
  public void successShowsAllStatesAndReleasesExactlyOnce() {
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    assertEquals(GroupRequestHandle.State.ADMITTED, h.state());
    drain();
    assertEquals(GroupRequestHandle.State.STARTED, h.state());
    call.success("ok");
    assertEquals(GroupRequestHandle.State.TERMINATED, h.state());
    assertEquals(1, repo.inFlight());
    drain();
    assertEquals("ok", result.value);
    assertEquals(1, result.successes);
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
    assertTrue(
        h.visitedStates()
            .containsAll(
                EnumSet.of(
                    GroupRequestHandle.State.ADMITTED,
                    GroupRequestHandle.State.DISPATCHED,
                    GroupRequestHandle.State.STARTED,
                    GroupRequestHandle.State.TERMINATED,
                    GroupRequestHandle.State.SETTLED)));
    assertEquals(GroupRequestHandle.Delivery.RESULT_DELIVERED, h.delivery());
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void cancelBeforeDispatchNeverCallsFactoryAndSettlesAfterOwnedTaskReturns() {
    AtomicInteger factories = new AtomicInteger();
    Result<String> result = new Result<>();
    GroupRequestHandle h =
        repo.execute(
            () -> {
              factories.incrementAndGet();
              return new Pending<String>();
            },
            result);
    h.cancel();
    assertEquals(GroupRequestHandle.State.CANCELLATION_REQUESTED, h.state());
    assertEquals(1, repo.inFlight());
    drain();
    assertEquals(0, factories.get());
    assertEquals(0, repo.inFlight());
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
    assertEquals(0, result.successes + result.failures);
  }

  @Test
  public void cancelAfterDispatchIsNotTerminationAndLateSuccessIsSuppressed() {
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    drain();
    h.cancel();
    drain();
    assertEquals(1, call.cancels);
    assertEquals(1, repo.inFlight());
    assertEquals(GroupRequestHandle.State.CANCELLATION_REQUESTED, h.state());
    call.success("late");
    drain();
    assertEquals(0, result.successes + result.failures);
    assertEquals(0, repo.inFlight());
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
  }

  @Test
  public void duplicateTerminalCallbacksDeliverAndReleaseOnlyOnce() {
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    repo.execute(() -> call, result);
    drain();
    call.success("first");
    call.success("duplicate");
    call.failure();
    drain();
    assertEquals(1, result.successes);
    assertEquals("first", result.value);
    assertEquals(0, result.failures);
    assertEquals(0, repo.inFlight());
    assertEquals(
        2,
        repo.diagnostics().snapshot().stream()
            .filter(e -> e.code.equals("DUPLICATE_TERMINAL"))
            .count());
  }

  @Test
  public void requestFailureSettlesOnce() {
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    repo.execute(() -> call, result);
    drain();
    call.failure();
    drain();
    assertEquals("NETWORK_ERROR", result.error);
    assertEquals(1, result.failures);
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void capacityRejectsBeforeFactoryAndRecoversOnlyAfterQuiescence() {
    List<Pending<String>> calls = new ArrayList<>();
    for (int i = 0; i < RemoteGroupRepository.REQUEST_CAPACITY; i++) {
      Pending<String> call = new Pending<>();
      calls.add(call);
      repo.execute(() -> call, new Result<>());
    }
    assertThrows(
        RejectedExecutionException.class,
        () ->
            repo.execute(
                () -> {
                  fail("Rejected factory");
                  return null;
                },
                new Result<>()));
    drain();
    calls.get(0).success("done");
    drain();
    assertEquals(RemoteGroupRepository.REQUEST_CAPACITY - 1, repo.inFlight());
    Pending<String> replacement = new Pending<>();
    repo.execute(() -> replacement, new Result<>());
    drain();
    repo.close();
    drain();
    for (int i = 1; i < calls.size(); i++) calls.get(i).failure();
    replacement.failure();
    drain();
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void concurrentAdmissionCannotExceedPerAccountLimit() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(8);
    AtomicInteger accepted = new AtomicInteger(), rejected = new AtomicInteger();
    CountDownLatch start = new CountDownLatch(1);
    List<Future<?>> jobs = new ArrayList<>();
    try {
      for (int i = 0; i < 48; i++)
        jobs.add(
            pool.submit(
                () -> {
                  try {
                    start.await();
                    repo.execute(() -> new Pending<String>(), new Result<>());
                    accepted.incrementAndGet();
                  } catch (RejectedExecutionException e) {
                    rejected.incrementAndGet();
                  } catch (InterruptedException e) {
                    throw new AssertionError(e);
                  }
                }));
      start.countDown();
      for (Future<?> job : jobs) job.get(5, TimeUnit.SECONDS);
      assertEquals(16, accepted.get());
      assertEquals(32, rejected.get());
      assertEquals(16, repo.inFlight());
      repo.close();
      drain();
      assertEquals(0, repo.inFlight()); // never-dispatched tasks prove no transport started
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  public void timeoutReportsFailureButHoldsUnknownTransportUntilTerminal() {
    Pending<String> call = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> call, result);
    drain();
    time.set(100);
    runtime.poll();
    drain();
    assertEquals("REQUEST_TIMEOUT", result.error);
    assertEquals(1, result.failures);
    assertEquals(GroupRequestHandle.State.CANCELLATION_REQUESTED, h.state());
    assertEquals(1, repo.inFlight());
    time.set(150);
    runtime.poll();
    runtime.poll();
    assertEquals(
        1,
        repo.diagnostics().snapshot().stream()
            .filter(e -> e.code.equals("SHUTDOWN_DEADLINE_TERMINATION_UNKNOWN"))
            .count());
    call.success("too late");
    drain();
    assertEquals(0, result.successes);
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void repositoryCloseCancelsAllAndRetainsBookkeepingUntilCallbacks() throws Exception {
    List<Pending<String>> calls = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      Pending<String> c = new Pending<>();
      calls.add(c);
      repo.execute(() -> c, new Result<>());
    }
    drain();
    repo.close();
    repo.close();
    drain();
    assertFalse(repo.awaitTermination(1, TimeUnit.MILLISECONDS));
    assertEquals(4, repo.inFlight());
    for (Pending<String> c : calls) {
      assertEquals(1, c.cancels);
      c.failure();
    }
    drain();
    assertTrue(repo.awaitTermination(1, TimeUnit.SECONDS));
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void logoutBetweenTransportCallbackAndResultClaimSuppressesSuccess() {
    Pending<String> c = new Pending<>();
    Result<String> result = new Result<>();
    repo.execute(() -> c, result);
    drain();
    c.success("private");
    repo.close();
    drain();
    assertEquals(0, result.successes + result.failures);
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void boundAccountEpochRejectsLateResultEvenBeforePhysicalRepositoryClose()
      throws Exception {
    AtomicReference<GroupsAccountScope.Access> access =
        new AtomicReference<>(GroupsAccountScope.Access.ACTIVE);
    GroupsAccountScope a =
        new GroupsAccountScope(UUID.randomUUID(), UUID.randomUUID(), access::get, Runnable::run);
    repo.bindScope(a);
    Pending<String> c = new Pending<>();
    Result<String> result = new Result<>();
    GroupRequestHandle h = repo.execute(() -> c, result);
    drain();
    assertEquals(a.userId(), h.userId);
    assertEquals(a.epoch(), h.sessionEpoch);
    access.set(GroupsAccountScope.Access.EXPIRED);
    c.success("old account");
    drain();
    assertEquals(0, result.successes + result.failures);
    assertTrue(a.awaitCleanup(5, TimeUnit.SECONDS));
    drain();
    assertEquals(0, repo.inFlight());
    GroupsAccountScope b =
        new GroupsAccountScope(
            UUID.randomUUID(),
            UUID.randomUUID(),
            () -> GroupsAccountScope.Access.ACTIVE,
            Runnable::run);
    assertThrows(IllegalStateException.class, () -> repo.bindScope(b));
    b.close();
    a.close();
  }

  @Test
  public void synchronousRetrofitCallbackCannotReleaseWhileEnqueueStillRunning() {
    Pending<String> c = new Pending<>();
    Result<String> result = new Result<>();
    c.enqueueHook =
        () -> {
          c.success("inline");
          assertEquals(1, repo.inFlight());
          assertEquals(0, result.successes);
        };
    repo.execute(() -> c, result);
    drain();
    assertEquals(1, result.successes);
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void factoryFailureIsKnownNotStartedAndReportsError() {
    Result<String> result = new Result<>();
    GroupRequestHandle h =
        repo.execute(
            () -> {
              throw new IllegalArgumentException("injected");
            },
            result);
    drain();
    assertEquals("REQUEST_START_FAILED", result.error);
    assertEquals(GroupRequestHandle.State.SETTLED, h.state());
    assertFalse(h.visitedStates().contains(GroupRequestHandle.State.STARTED));
  }

  @Test
  public void failingCancelDoesNotFabricateTermination() {
    Pending<String> c = new Pending<>();
    c.cancelHook =
        () -> {
          throw new IllegalStateException("injected cancel");
        };
    repo.execute(() -> c, new Result<>());
    drain();
    repo.close();
    drain();
    assertEquals(1, repo.inFlight());
    assertTrue(
        repo.diagnostics().snapshot().stream()
            .anyMatch(e -> e.code.equals("REQUEST_CANCEL_FAILURE")));
    c.failure();
    drain();
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void enqueueExceptionRetainsUnknownTransportUntilRealCallback() {
    Pending<String> c = new Pending<>();
    c.enqueueHook =
        () -> {
          throw new IllegalStateException("injected after dispatch");
        };
    Result<String> result = new Result<>();
    repo.execute(() -> c, result);
    drain();
    assertEquals("REQUEST_START_FAILED", result.error);
    assertEquals(1, repo.inFlight());
    assertEquals(1, c.cancels);
    c.failure();
    drain();
    assertEquals(0, repo.inFlight());
    assertEquals(1, result.failures);
  }

  @Test
  public void globalRuntimeBoundPreventsNewAccountsEscapingRetiredCapacity() {
    List<RemoteGroupRepository> repos = new ArrayList<>();
    GroupApi api =
        (GroupApi)
            java.lang.reflect.Proxy.newProxyInstance(
                GroupApi.class.getClassLoader(),
                new Class[] {GroupApi.class},
                (p, m, a) -> {
                  throw new AssertionError();
                });
    for (int n = 0; n < 4; n++) {
      RemoteGroupRepository r = new RemoteGroupRepository(api, runtime, 100, 50);
      repos.add(r);
      for (int i = 0; i < 16; i++) r.execute(() -> new Pending<String>(), new Result<>());
    }
    RemoteGroupRepository overflow = new RemoteGroupRepository(api, runtime, 100, 50);
    assertThrows(
        RejectedExecutionException.class,
        () ->
            overflow.execute(
                () -> {
                  fail("Global overload factory");
                  return null;
                },
                new Result<>()));
    for (RemoteGroupRepository r : repos) r.close();
    drain();
    for (RemoteGroupRepository r : repos) assertEquals(0, r.inFlight());
    GroupRequestHandle recovered = overflow.execute(() -> new Pending<String>(), new Result<>());
    recovered.cancel();
    drain();
    assertEquals(GroupRequestHandle.State.SETTLED, recovered.state());
    overflow.close();
  }

  @Test
  public void resultDeferredDuringSameEpochRefreshCompletesAfterResume() throws Exception {
    AtomicReference<GroupsAccountScope.Access> access =
        new AtomicReference<>(GroupsAccountScope.Access.ACTIVE);
    GroupsAccountScope scope =
        new GroupsAccountScope(UUID.randomUUID(), UUID.randomUUID(), access::get, Runnable::run);
    repo.bindScope(scope);
    Pending<String> c = new Pending<>();
    Result<String> result = new Result<>();
    repo.execute(() -> c, result);
    drain();
    access.set(GroupsAccountScope.Access.SUSPENDED);
    c.success("retained");
    drain();
    assertEquals(0, result.successes);
    assertEquals(1, repo.inFlight());
    access.set(GroupsAccountScope.Access.ACTIVE);
    scope.sessionChanged();
    assertEquals("retained", result.value);
    assertEquals(0, repo.inFlight());
    scope.close();
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    drain();
  }

  @Test
  public void timeoutAndTerminalRaceAlwaysChooseOneOutcome() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      for (int i = 0; i < 48; i++) {
        Pending<String> c = new Pending<>();
        Result<String> result = new Result<>();
        repo.execute(() -> c, result);
        drain();
        time.addAndGet(100);
        CountDownLatch start = new CountDownLatch(1);
        Future<?> timeout =
            pool.submit(
                () -> {
                  try {
                    start.await();
                  } catch (InterruptedException e) {
                    throw new AssertionError(e);
                  }
                  runtime.poll();
                });
        Future<?> response =
            pool.submit(
                () -> {
                  try {
                    start.await();
                  } catch (InterruptedException e) {
                    throw new AssertionError(e);
                  }
                  c.success("ok");
                });
        start.countDown();
        timeout.get(5, TimeUnit.SECONDS);
        response.get(5, TimeUnit.SECONDS);
        drain();
        assertEquals(1, result.successes + result.failures);
        assertEquals(0, repo.inFlight());
        if (result.failures == 1) assertEquals("REQUEST_TIMEOUT", result.error);
        else assertEquals("ok", result.value);
      }
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  public void synchronousTerminalThenEnqueueThrowDoesNotEraseChosenSuccess() {
    Pending<String> c = new Pending<>();
    c.enqueueHook =
        () -> {
          c.success("committed");
          throw new IllegalStateException("injected after terminal");
        };
    Result<String> result = new Result<>();
    repo.execute(() -> c, result);
    drain();
    assertEquals(1, result.successes);
    assertEquals("committed", result.value);
    assertEquals(0, result.failures);
    assertEquals(0, repo.inFlight());
  }

  @Test
  public void callbackFailureStillSettlesAndPreservesDiagnostic() {
    Pending<String> c = new Pending<>();
    repo.execute(
        () -> c,
        new GroupRepository.Result<String>() {
          public void success(String value) {
            throw new IllegalStateException("injected result failure");
          }

          public void failure(String code) {
            fail(code);
          }
        });
    drain();
    c.success("ok");
    drain();
    assertEquals(0, repo.inFlight());
    assertTrue(
        repo.diagnostics().snapshot().stream()
            .anyMatch(e -> e.code.equals("RESULT_CALLBACK_FAILURE")));
  }
}
