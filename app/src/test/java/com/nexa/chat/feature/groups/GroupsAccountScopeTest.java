package com.nexa.chat.feature.groups;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;

public class GroupsAccountScopeTest {
  @Test
  public void queuedSuccessIsSuppressedAfterClose() throws Exception {
    Queue<Runnable> tasks = new ArrayDeque<>();
    GroupsAccountScope scope =
        new GroupsAccountScope(
            UUID.randomUUID(),
            UUID.randomUUID(),
            () -> GroupsAccountScope.Access.ACTIVE,
            tasks::add);
    AtomicInteger writes = new AtomicInteger();
    scope.dispatch(writes::incrementAndGet);
    scope.close();
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    tasks.remove().run();
    assertEquals(0, writes.get());
  }

  @Test
  public void latestSnapshotIsCheckedAtCallbackExecution() throws Exception {
    Queue<Runnable> tasks = new ArrayDeque<>();
    AtomicReference<UUID> epoch = new AtomicReference<>(UUID.randomUUID());
    UUID captured = epoch.get();
    GroupsAccountScope scope =
        new GroupsAccountScope(
            UUID.randomUUID(),
            captured,
            () ->
                epoch.get().equals(captured)
                    ? GroupsAccountScope.Access.ACTIVE
                    : GroupsAccountScope.Access.EXPIRED,
            tasks::add);
    AtomicInteger writes = new AtomicInteger(), cancelled = new AtomicInteger();
    scope.own(cancelled::incrementAndGet);
    scope.dispatch(writes::incrementAndGet);
    epoch.set(UUID.randomUUID());
    tasks.remove().run();
    assertEquals(0, writes.get());
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    assertEquals(1, cancelled.get());
    assertFalse(scope.isCurrent());
  }

  @Test
  public void cleanupAndCloseNotificationAreIdempotent() throws Exception {
    GroupsAccountScope scope =
        new GroupsAccountScope(
            UUID.randomUUID(),
            UUID.randomUUID(),
            () -> GroupsAccountScope.Access.ACTIVE,
            Runnable::run);
    AtomicInteger cleanup = new AtomicInteger(), notifications = new AtomicInteger();
    scope.own(cleanup::incrementAndGet);
    scope.onClosed(notifications::incrementAndGet);
    scope.close();
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    scope.close();
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    assertEquals(1, cleanup.get());
    assertEquals(1, notifications.get());
  }

  @Test
  public void detachedListenerDoesNotRetainOldActivity() throws Exception {
    GroupsAccountScope scope =
        new GroupsAccountScope(
            UUID.randomUUID(),
            UUID.randomUUID(),
            () -> GroupsAccountScope.Access.ACTIVE,
            Runnable::run);
    AtomicInteger notifications = new AtomicInteger();
    var registration = scope.onClosed(notifications::incrementAndGet);
    registration.close();
    registration.close();
    scope.close();
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    assertEquals(0, notifications.get());
  }

  @Test
  public void queuedClosureNotificationCanBeCancelledOnDestroy() throws Exception {
    Queue<Runnable> tasks = new ArrayDeque<>();
    GroupsAccountScope scope =
        new GroupsAccountScope(
            UUID.randomUUID(),
            UUID.randomUUID(),
            () -> GroupsAccountScope.Access.ACTIVE,
            tasks::add);
    AtomicInteger notifications = new AtomicInteger();
    var registration = scope.onClosed(notifications::incrementAndGet);
    scope.close();
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    registration.close();
    tasks.remove().run();
    assertEquals(0, notifications.get());
  }

  @Test
  public void logoutDiscardsResponsesDeferredDuringRefresh() throws Exception {
    AtomicReference<GroupsAccountScope.Access> access =
        new AtomicReference<>(GroupsAccountScope.Access.SUSPENDED);
    GroupsAccountScope scope =
        new GroupsAccountScope(UUID.randomUUID(), UUID.randomUUID(), access::get, Runnable::run);
    AtomicInteger writes = new AtomicInteger();
    scope.dispatch(writes::incrementAndGet);
    assertEquals(0, writes.get());
    access.set(GroupsAccountScope.Access.EXPIRED);
    scope.sessionChanged();
    access.set(GroupsAccountScope.Access.ACTIVE);
    scope.sessionChanged();
    assertEquals(0, writes.get());
    assertFalse(scope.isCurrent());
  }

  @Test
  public void lateResourcesAreClosedAsynchronously() throws Exception {
    GroupsAccountScope scope =
        new GroupsAccountScope(
            UUID.randomUUID(),
            UUID.randomUUID(),
            () -> GroupsAccountScope.Access.ACTIVE,
            Runnable::run);
    scope.close();
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    AtomicInteger cleanup = new AtomicInteger();
    scope.own(cleanup::incrementAndGet);
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    assertEquals(1, cleanup.get());
  }

  @Test
  public void oneCleanupFailureDoesNotSkipOtherResourcesOrScreenClosure() throws Exception {
    GroupsAccountScope scope =
        new GroupsAccountScope(
            UUID.randomUUID(),
            UUID.randomUUID(),
            () -> GroupsAccountScope.Access.ACTIVE,
            Runnable::run);
    AtomicInteger cleanup = new AtomicInteger(), notifications = new AtomicInteger();
    scope.own(
        () -> {
          throw new IllegalStateException("test cleanup failure");
        });
    scope.own(cleanup::incrementAndGet);
    scope.onClosed(notifications::incrementAndGet);
    scope.close();
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    assertTrue(
        scope.diagnostics().snapshot().stream()
            .anyMatch(
                e -> e.code.equals("CLEANUP_FAILURE") && e.cause instanceof IllegalStateException));
    assertFalse(scope.isCurrent());
    assertEquals(1, cleanup.get());
    assertEquals(1, notifications.get());
  }

  @Test
  public void closeDoesNotWaitForAnAlreadyAdmittedAction() throws Exception {
    GroupsAccountScope scope =
        new GroupsAccountScope(
            UUID.randomUUID(),
            UUID.randomUUID(),
            () -> GroupsAccountScope.Access.ACTIVE,
            Runnable::run);
    CountDownLatch entered = new CountDownLatch(1),
        release = new CountDownLatch(1),
        closing = new CountDownLatch(1);
    AtomicInteger mutations = new AtomicInteger();
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      Future<?> callback =
          pool.submit(
              () ->
                  scope.runIfCurrent(
                      () -> {
                        entered.countDown();
                        try {
                          if (!release.await(5, TimeUnit.SECONDS))
                            throw new AssertionError("callback timeout");
                        } catch (InterruptedException e) {
                          throw new AssertionError(e);
                        }
                        mutations.incrementAndGet();
                      }));
      assertTrue(entered.await(5, TimeUnit.SECONDS));
      Future<?> close =
          pool.submit(
              () -> {
                closing.countDown();
                scope.close();
              });
      assertTrue(closing.await(5, TimeUnit.SECONDS));
      close.get(5, TimeUnit.SECONDS);
      assertFalse(scope.isCurrent());
      assertFalse(scope.runIfCurrent(mutations::incrementAndGet));
      release.countDown();
      callback.get(5, TimeUnit.SECONDS);
      assertFalse(scope.runIfCurrent(mutations::incrementAndGet));
      assertEquals(1, mutations.get());
    } finally {
      release.countDown();
      pool.shutdownNow();
    }
  }
}
