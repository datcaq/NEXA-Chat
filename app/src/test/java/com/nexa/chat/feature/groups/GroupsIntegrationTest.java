package com.nexa.chat.feature.groups;

import static org.junit.Assert.*;

import com.nexa.chat.feature.groups.model.GroupModels.*;
import com.nexa.chat.feature.groups.repository.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;

public class GroupsIntegrationTest {

  private GroupsIntegration.Dependencies custom(
      GroupsAccountScope scope, UUID directoryUser, AtomicInteger closed) {
    PeopleDirectory people =
        new PeopleDirectory() {
          public String currentUserId() {
            return directoryUser.toString();
          }

          public void friends(int page, GroupRepository.Result<Page<Person>> callback) {}

          public void searchPhone(
              String phone, int page, GroupRepository.Result<Page<Person>> callback) {}

          public void close() {
            closed.incrementAndGet();
          }
        };
    return new GroupsIntegration.Dependencies(
        new GroupsViewModelTest.TestGroups(), people, null, scope);
  }

  @Test
  public void partialInitializationIdentityFailureClosesAllAdoptedResources() throws Exception {
    UUID user = UUID.randomUUID();
    GroupsAccountScope scope =
        new GroupsAccountScope(
            user, UUID.randomUUID(), () -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
    AtomicInteger peopleClosed = new AtomicInteger();
    assertThrows(
        IllegalArgumentException.class, () -> custom(scope, UUID.randomUUID(), peopleClosed));
    assertFalse(scope.isCurrent());
    assertTrue(scope.awaitCleanup(5, java.util.concurrent.TimeUnit.SECONDS));
    assertEquals(1, peopleClosed.get());
    assertTrue(
        scope.diagnostics().snapshot().stream()
            .anyMatch(e -> e.code.equals("DEPENDENCY_INITIALIZATION_FAILURE")));
  }

  @Test
  public void newerAccountInstallInvalidatesCandidateBlockedInExternalProjection()
      throws Exception {
    java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1),
        release = new java.util.concurrent.CountDownLatch(1);
    UUID user = UUID.randomUUID();
    GroupsAccountScope aScope =
        new GroupsAccountScope(
            user,
            UUID.randomUUID(),
            () -> {
              entered.countDown();
              try {
                assertTrue(release.await(5, java.util.concurrent.TimeUnit.SECONDS));
              } catch (InterruptedException e) {
                throw new AssertionError(e);
              }
              return GroupsAccountScope.Access.ACTIVE;
            },
            Runnable::run);
    var a = custom(aScope, user, new AtomicInteger());
    var b = dependencies(UUID.randomUUID(), new AtomicInteger());
    java.util.concurrent.ExecutorService pool =
        java.util.concurrent.Executors.newSingleThreadExecutor();
    try {
      var first = pool.submit(() -> GroupsIntegration.install(a));
      assertTrue(entered.await(5, java.util.concurrent.TimeUnit.SECONDS));
      GroupsIntegration.install(b);
      assertFalse(a.scope.isCurrent());
      assertSame(b, GroupsIntegration.get());
      release.countDown();
      first.get(5, java.util.concurrent.TimeUnit.SECONDS);
      assertSame(b, GroupsIntegration.get());
      assertTrue(b.scope.isCurrent());
    } finally {
      release.countDown();
      pool.shutdownNow();
      aScope.close();
      assertTrue(aScope.awaitCleanup(5, java.util.concurrent.TimeUnit.SECONDS));
    }
  }

  @Test
  public void logoutDuringPreparationCannotPublishLateCandidate() throws Exception {
    java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1),
        release = new java.util.concurrent.CountDownLatch(1);
    UUID user = UUID.randomUUID();
    GroupsAccountScope scope =
        new GroupsAccountScope(
            user,
            UUID.randomUUID(),
            () -> {
              entered.countDown();
              try {
                assertTrue(release.await(5, java.util.concurrent.TimeUnit.SECONDS));
              } catch (InterruptedException e) {
                throw new AssertionError(e);
              }
              return GroupsAccountScope.Access.ACTIVE;
            },
            Runnable::run);
    var candidate = custom(scope, user, new AtomicInteger());
    java.util.concurrent.ExecutorService pool =
        java.util.concurrent.Executors.newSingleThreadExecutor();
    try {
      var pending = pool.submit(() -> GroupsIntegration.install(candidate));
      assertTrue(entered.await(5, java.util.concurrent.TimeUnit.SECONDS));
      GroupsIntegration.install(null);
      assertFalse(scope.isCurrent());
      assertNull(GroupsIntegration.get());
      release.countDown();
      pending.get(5, java.util.concurrent.TimeUnit.SECONDS);
      assertNull(GroupsIntegration.get());
    } finally {
      release.countDown();
      pool.shutdownNow();
      scope.close();
      assertTrue(scope.awaitCleanup(5, java.util.concurrent.TimeUnit.SECONDS));
    }
  }

  @After
  public void cleanup() throws Exception {
    var old = GroupsIntegration.get();
    GroupsIntegration.install(null);
    if (old != null) assertTrue(old.scope.awaitCleanup(5, java.util.concurrent.TimeUnit.SECONDS));
  }

  private GroupsIntegration.Dependencies dependencies(UUID user, AtomicInteger directoryClosed) {
    GroupsAccountScope scope =
        new GroupsAccountScope(
            user, UUID.randomUUID(), () -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
    PeopleDirectory people =
        new PeopleDirectory() {
          public String currentUserId() {
            return user.toString();
          }

          public void friends(int page, GroupRepository.Result<Page<Person>> result) {}

          public void searchPhone(
              String phone, int page, GroupRepository.Result<Page<Person>> result) {}

          public void close() {
            directoryClosed.incrementAndGet();
          }
        };
    return new GroupsIntegration.Dependencies(
        new GroupsViewModelTest.TestGroups(), people, null, scope);
  }

  @Test
  public void replacementClosesOldResourcesAndLeavesNewAccountCurrent() throws Exception {
    AtomicInteger oldDirectoryClosed = new AtomicInteger(),
        newDirectoryClosed = new AtomicInteger();
    var first = dependencies(UUID.randomUUID(), oldDirectoryClosed);
    var second = dependencies(UUID.randomUUID(), newDirectoryClosed);
    GroupsIntegration.install(first);
    GroupsIntegration.install(second);
    assertTrue(first.scope.awaitCleanup(5, java.util.concurrent.TimeUnit.SECONDS));
    assertFalse(first.scope.isCurrent());
    assertTrue(second.scope.isCurrent());
    assertTrue(((GroupsViewModelTest.TestGroups) first.groups).closed);
    assertEquals(1, oldDirectoryClosed.get());
    assertEquals(0, newDirectoryClosed.get());
    assertSame(second, GroupsIntegration.get());
  }

  @Test
  public void installNullInvalidatesCapturedDependenciesNotJustStaticReference() throws Exception {
    AtomicInteger directoryClosed = new AtomicInteger();
    var captured = dependencies(UUID.randomUUID(), directoryClosed);
    GroupsIntegration.install(captured);
    GroupsIntegration.install(null);
    GroupsIntegration.install(null);
    assertNull(GroupsIntegration.get());
    assertTrue(captured.scope.awaitCleanup(5, java.util.concurrent.TimeUnit.SECONDS));
    assertFalse(captured.scope.isCurrent());
    assertEquals(1, directoryClosed.get());
    assertTrue(((GroupsViewModelTest.TestGroups) captured.groups).closed);
  }

  @Test
  public void sameDependenciesAreRetainedDuringSameEpochRefresh() throws Exception {
    AtomicInteger directoryClosed = new AtomicInteger();
    var current = dependencies(UUID.randomUUID(), directoryClosed);
    GroupsIntegration.install(current);
    GroupsIntegration.install(current);
    assertSame(current, GroupsIntegration.get());
    assertTrue(current.scope.isCurrent());
    assertEquals(0, directoryClosed.get());
    assertFalse(((GroupsViewModelTest.TestGroups) current.groups).closed);
  }

  @Test
  public void cleanupFailureIsReportedAndAccountBCanInitialize() throws Exception {
    var old = dependencies(UUID.randomUUID(), new AtomicInteger());
    var next = dependencies(UUID.randomUUID(), new AtomicInteger());
    old.scope.own(
        () -> {
          throw new IllegalStateException("test failure");
        });
    GroupsIntegration.install(old);
    GroupsIntegration.install(next);
    assertTrue(old.scope.awaitCleanup(5, java.util.concurrent.TimeUnit.SECONDS));
    assertSame(next, GroupsIntegration.get());
    assertTrue(
        old.scope.diagnostics().snapshot().stream()
            .anyMatch(e -> e.code.equals("CLEANUP_FAILURE")));
    assertFalse(old.scope.isCurrent());
    assertTrue(next.scope.isCurrent());
  }
}
