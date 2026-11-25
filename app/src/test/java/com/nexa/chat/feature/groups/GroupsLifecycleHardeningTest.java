package com.nexa.chat.feature.groups;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;

public class GroupsLifecycleHardeningTest {
  private final List<GroupsAccountScope> scopes = new ArrayList<>();

  private GroupsAccountScope scope(
      java.util.function.Supplier<GroupsAccountScope.Access> access, Executor ui) {
    GroupsAccountScope scope =
        new GroupsAccountScope(UUID.randomUUID(), UUID.randomUUID(), access, ui);
    scopes.add(scope);
    return scope;
  }

  @After
  public void cleanup() throws Exception {
    for (GroupsAccountScope s : scopes) {
      s.close();
      assertTrue(s.awaitCleanup(5, TimeUnit.SECONDS));
    }
  }

  @Test
  public void closedNotificationDoesNotWaitBehindBlockedPhysicalCleanup() throws Exception {
    CountDownLatch occupied = new CountDownLatch(2), release = new CountDownLatch(1);
    GroupsAccountScope retiring = scope(() -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
    for (int i = 0; i < 2; i++)
      retiring.own(
          () -> {
            occupied.countDown();
            try {
              assertTrue(release.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
              throw new AssertionError(e);
            }
          });
    retiring.close();
    try {
      assertTrue(occupied.await(5, TimeUnit.SECONDS));
      CountDownLatch notification = new CountDownLatch(1);
      GroupsAccountScope current = scope(() -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
      current.onClosed(notification::countDown);
      current.close();
      assertTrue(
          "Terminal UI handoff must not queue behind physical resource cleanup",
          notification.await(1, TimeUnit.SECONDS));
    } finally {
      release.countDown();
    }
  }

  @Test
  public void resourceIdentityIsAdoptedOnceBeforeAndAfterClosure() throws Exception {
    GroupsAccountScope s = scope(() -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
    Object resource = new Object();
    AtomicInteger closes = new AtomicInteger();
    for (int i = 0; i < 12; i++) s.own(resource, closes::incrementAndGet);
    for (int i = 0; i < 7; i++) s.own(new Object(), closes::incrementAndGet);
    assertThrows(
        RejectedExecutionException.class, () -> s.own(new Object(), closes::incrementAndGet));
    s.close();
    assertTrue(s.awaitCleanup(5, TimeUnit.SECONDS));
    assertEquals(8, closes.get());
    s.own(resource, () -> fail("same adopted resource must not close twice"));
    assertTrue(s.awaitCleanup(5, TimeUnit.SECONDS));
    assertEquals(8, closes.get());
  }

  @Test
  public void newResourceAfterClosureIsDisposedAndDeduplicated() throws Exception {
    GroupsAccountScope s = scope(() -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
    s.close();
    Object resource = new Object();
    AtomicInteger closes = new AtomicInteger();
    s.own(resource, closes::incrementAndGet);
    s.own(resource, closes::incrementAndGet);
    assertTrue(s.awaitCleanup(5, TimeUnit.SECONDS));
    assertEquals(1, closes.get());
  }

  @Test
  public void reentryQueuesWithoutHoldingStateLockOrRecursing() {
    GroupsAccountScope s = scope(() -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
    List<Integer> order = new ArrayList<>();
    assertTrue(
        s.runIfCurrent(
            () -> {
              order.add(1);
              assertFalse(
                  s.runIfCurrent(
                      () -> fail("Nested immediate action must not bypass serialization")));
              s.dispatch(() -> order.add(3));
              order.add(2);
            }));
    assertEquals(List.of(1, 2, 3), order);
  }

  @Test
  public void callbackExceptionIsRecordedAndDoesNotStrandNextCallback() {
    Queue<Runnable> ui = new ArrayDeque<>();
    GroupsAccountScope s = scope(() -> GroupsAccountScope.Access.ACTIVE, ui::add);
    AtomicInteger delivered = new AtomicInteger();
    s.dispatch(
        () -> {
          throw new IllegalArgumentException("injected");
        });
    s.dispatch(delivered::incrementAndGet);
    assertEquals(1, ui.size());
    ui.remove().run();
    assertEquals(1, delivered.get());
    assertEquals("CALLBACK_FAILURE", s.diagnostics().snapshot().get(0).code);
    assertTrue(s.diagnostics().snapshot().get(0).cause instanceof IllegalArgumentException);
  }

  @Test
  public void suspendedCapacityRejectsObservablyAndRecoversAfterRefresh() {
    AtomicReference<GroupsAccountScope.Access> access =
        new AtomicReference<>(GroupsAccountScope.Access.SUSPENDED);
    GroupsAccountScope s = scope(access::get, Runnable::run);
    AtomicInteger delivered = new AtomicInteger();
    for (int i = 0; i < GroupsAccountScope.CALLBACK_CAPACITY; i++)
      assertTrue(s.tryDispatch(delivered::incrementAndGet));
    assertFalse(s.tryDispatch(() -> fail("Rejected callback")));
    assertEquals(0, delivered.get());
    assertEquals("CALLBACK_CAPACITY", s.diagnostics().snapshot().get(0).code);
    access.set(GroupsAccountScope.Access.ACTIVE);
    s.sessionChanged();
    assertEquals(GroupsAccountScope.CALLBACK_CAPACITY, delivered.get());
    assertTrue(s.tryDispatch(delivered::incrementAndGet));
    assertEquals(GroupsAccountScope.CALLBACK_CAPACITY + 1, delivered.get());
  }

  @Test
  public void voidDispatchOverloadClosesFeatureButStillRunsOwnedCleanup() throws Exception {
    GroupsAccountScope s = scope(() -> GroupsAccountScope.Access.SUSPENDED, Runnable::run);
    AtomicInteger cleanup = new AtomicInteger();
    s.own(cleanup::incrementAndGet);
    for (int i = 0; i <= GroupsAccountScope.CALLBACK_CAPACITY; i++)
      s.dispatch(() -> fail("Suspended"));
    assertFalse(s.isCurrent());
    assertTrue(s.awaitCleanup(5, TimeUnit.SECONDS));
    assertEquals(1, cleanup.get());
  }

  @Test
  public void accountSwitchDiscardsAQueuedWorkAndCannotReactivateA() {
    Queue<Runnable> ui = new ArrayDeque<>();
    AtomicReference<GroupsAccountScope.Access> access =
        new AtomicReference<>(GroupsAccountScope.Access.ACTIVE);
    GroupsAccountScope a = scope(access::get, ui::add);
    GroupsAccountScope b = scope(() -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
    AtomicInteger aWrites = new AtomicInteger(), bWrites = new AtomicInteger();
    a.dispatch(aWrites::incrementAndGet);
    a.close();
    b.dispatch(bWrites::incrementAndGet);
    access.set(GroupsAccountScope.Access.ACTIVE);
    a.sessionChanged();
    while (!ui.isEmpty()) ui.remove().run();
    assertEquals(0, aWrites.get());
    assertEquals(1, bWrites.get());
    assertFalse(a.runIfCurrent(aWrites::incrementAndGet));
    assertNotEquals(a.epoch(), b.epoch());
  }

  @Test
  public void concurrentDispatchIsSerializedEvenWithParallelHostExecutor() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      GroupsAccountScope s = scope(() -> GroupsAccountScope.Access.ACTIVE, pool);
      AtomicInteger running = new AtomicInteger(), max = new AtomicInteger();
      CountDownLatch done = new CountDownLatch(24);
      for (int i = 0; i < 24; i++)
        s.dispatch(
            () -> {
              int n = running.incrementAndGet();
              max.accumulateAndGet(n, Math::max);
              Thread.yield();
              running.decrementAndGet();
              done.countDown();
            });
      assertTrue(done.await(5, TimeUnit.SECONDS));
      assertEquals(1, max.get());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  public void blockedSessionSupplierCannotHoldInvalidationLock() throws Exception {
    CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
    GroupsAccountScope s =
        scope(
            () -> {
              entered.countDown();
              try {
                assertTrue(release.await(5, TimeUnit.SECONDS));
              } catch (InterruptedException e) {
                throw new AssertionError(e);
              }
              return GroupsAccountScope.Access.ACTIVE;
            },
            Runnable::run);
    ExecutorService pool = Executors.newSingleThreadExecutor();
    try {
      Future<Boolean> access = pool.submit(s::hasAccess);
      assertTrue(entered.await(5, TimeUnit.SECONDS));
      s.close(); // must complete while supplier remains blocked
      release.countDown();
      assertFalse(access.get(5, TimeUnit.SECONDS));
    } finally {
      release.countDown();
      pool.shutdownNow();
    }
  }

  @Test
  public void blockingCleanupDoesNotBlockLogicalCloseOrAccountB() throws Exception {
    CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
    GroupsAccountScope a = scope(() -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
    a.own(
        () -> {
          entered.countDown();
          try {
            assertTrue(release.await(5, TimeUnit.SECONDS));
          } catch (InterruptedException e) {
            throw new AssertionError(e);
          }
        });
    try {
      a.close();
      assertTrue(entered.await(5, TimeUnit.SECONDS));
      assertFalse(a.isCurrent());
      assertFalse(a.awaitCleanup(1, TimeUnit.MILLISECONDS));
      GroupsAccountScope b = scope(() -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
      assertTrue(b.hasAccess());
    } finally {
      release.countDown();
    }
  }

  @Test
  public void multipleFailingResourcesAllAttemptedExactlyOnceAndDiagnosed() throws Exception {
    GroupsAccountScope s = scope(() -> GroupsAccountScope.Access.SUSPENDED, Runnable::run);
    AtomicInteger attempts = new AtomicInteger();
    for (int i = 0; i < 3; i++)
      s.own(
          () -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("injected resource failure");
          });
    s.close();
    s.close();
    assertTrue(s.awaitCleanup(5, TimeUnit.SECONDS));
    assertEquals(3, attempts.get());
    assertEquals(
        3,
        s.diagnostics().snapshot().stream().filter(e -> e.code.equals("CLEANUP_FAILURE")).count());
  }

  @Test
  public void executorRejectionFailsClosedWithDiagnosticRatherThanEscapingObserver() {
    GroupsAccountScope s =
        scope(
            () -> GroupsAccountScope.Access.ACTIVE,
            r -> {
              throw new RejectedExecutionException("injected UI rejection");
            });
    s.dispatch(() -> fail("Rejected"));
    assertFalse(s.isCurrent());
    assertEquals("UI_HANDOFF_REJECTED", s.diagnostics().snapshot().get(0).code);
  }

  @Test
  public void diagnosticsRetainBoundedTailAndTotalFailureCount() {
    GroupsDiagnostics d = new GroupsDiagnostics();
    for (int i = 0; i < 100; i++) d.record("INJECTED", new IllegalStateException());
    assertEquals(100, d.total());
    assertEquals(GroupsDiagnostics.CAPACITY, d.snapshot().size());
  }
}
