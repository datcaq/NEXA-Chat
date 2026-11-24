package com.nexa.chat.feature.groups;

import static org.junit.Assert.*;

import com.nexa.chat.feature.groups.model.GroupModels.*;
import com.nexa.chat.feature.groups.repository.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;

public class GroupsIntegrationTest {
  @After
  public void cleanup() {
    GroupsIntegration.install(null);
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
  public void replacementClosesOldResourcesAndLeavesNewAccountCurrent() {
    AtomicInteger oldDirectoryClosed = new AtomicInteger(),
        newDirectoryClosed = new AtomicInteger();
    var first = dependencies(UUID.randomUUID(), oldDirectoryClosed);
    var second = dependencies(UUID.randomUUID(), newDirectoryClosed);
    GroupsIntegration.install(first);
    GroupsIntegration.install(second);
    assertFalse(first.scope.isCurrent());
    assertTrue(second.scope.isCurrent());
    assertTrue(((GroupsViewModelTest.TestGroups) first.groups).closed);
    assertEquals(1, oldDirectoryClosed.get());
    assertEquals(0, newDirectoryClosed.get());
    assertSame(second, GroupsIntegration.get());
  }

  @Test
  public void installNullInvalidatesCapturedDependenciesNotJustStaticReference() {
    AtomicInteger directoryClosed = new AtomicInteger();
    var captured = dependencies(UUID.randomUUID(), directoryClosed);
    GroupsIntegration.install(captured);
    GroupsIntegration.install(null);
    GroupsIntegration.install(null);
    assertNull(GroupsIntegration.get());
    assertFalse(captured.scope.isCurrent());
    assertEquals(1, directoryClosed.get());
    assertTrue(((GroupsViewModelTest.TestGroups) captured.groups).closed);
  }

  @Test
  public void sameDependenciesAreRetainedDuringSameEpochRefresh() {
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
  public void cleanupFailureFailsClosedAndDisposesIncomingDependencies() {
    var old = dependencies(UUID.randomUUID(), new AtomicInteger());
    var next = dependencies(UUID.randomUUID(), new AtomicInteger());
    old.scope.own(
        () -> {
          throw new IllegalStateException("test failure");
        });
    GroupsIntegration.install(old);
    assertThrows(IllegalStateException.class, () -> GroupsIntegration.install(next));
    assertNull(GroupsIntegration.get());
    assertFalse(old.scope.isCurrent());
    assertFalse(next.scope.isCurrent());
  }
}
