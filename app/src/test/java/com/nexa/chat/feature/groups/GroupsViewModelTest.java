package com.nexa.chat.feature.groups;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.SavedStateHandle;
import com.nexa.chat.feature.groups.model.GroupModels.*;
import com.nexa.chat.feature.groups.repository.*;
import com.nexa.chat.feature.groups.viewmodel.GroupsViewModel;
import java.util.*;
import org.junit.*;

public class GroupsViewModelTest {
  @Rule public InstantTaskExecutorRule executor = new InstantTaskExecutorRule();
  TestGroups repository;
  GroupsViewModel vm;
  SavedStateHandle saved;
  final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
  GroupsAccountScope scope;
  PeopleDirectory directory =
      new PeopleDirectory() {
        public String currentUserId() {
          return userId.toString();
        }

        public void close() {}

        public void friends(int p, GroupRepository.Result<Page<Person>> c) {
          Page<Person> page = new Page<>();
          page.items = List.of(new Person("friend", "Friend", true));
          c.success(page);
        }

        public void searchPhone(String q, int p, GroupRepository.Result<Page<Person>> c) {
          c.failure("HTTP_503");
        }
      };

  @Before
  public void setup() throws Exception {
    repository = new TestGroups();
    saved = new SavedStateHandle();
    scope =
        new GroupsAccountScope(
            userId, UUID.randomUUID(), () -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
    vm = new GroupsViewModel(saved, repository, directory, scope);
  }

  @Test
  public void boundedRepositoryRejectionResetsBusyAndKeepsCreateRetryKey() {
    TestGroups rejected =
        new TestGroups() {
          @Override
          public void create(String key, Create request, GroupRepository.Result<Created> callback) {
            throw new java.util.concurrent.RejectedExecutionException("injected capacity");
          }
        };
    GroupsViewModel target =
        new GroupsViewModel(new SavedStateHandle(), rejected, directory, scope);
    target.name("Keep draft");
    target.create();
    assertFalse(target.busy);
    assertEquals("REQUEST_CAPACITY", target.error);
    assertEquals("Keep draft", target.name());
    assertTrue(
        scope.diagnostics().snapshot().stream()
            .anyMatch(e -> e.code.equals("VIEWMODEL_REQUEST_REJECTED")));
  }

  @Test
  public void blankNameDoesNotCallServer() throws Exception {
    vm.name("  ");
    vm.create();
    assertEquals("INVALID_NAME", vm.error);
    assertEquals(0, repository.submissions);
  }

  @Test
  public void selectionIsUniqueAndRemovable() throws Exception {
    Person p = new Person("friend", "Friend", true);
    assertTrue(vm.toggle(p));
    assertEquals(1, vm.selected().size());
    vm.toggle(new Person("friend", "Same user", true));
    assertTrue(vm.selected().isEmpty());
    assertFalse(vm.toggle(new Person(userId.toString(), "Me", true)));
    assertFalse(vm.toggle(new Person("blocked", "Blocked", false)));
  }

  @Test
  public void networkRetryKeepsPayloadAndKey() throws Exception {
    vm.name(" Study ");
    vm.toggle(new Person("friend", "Friend", true));
    vm.create();
    String key = repository.key;
    vm.create();
    assertEquals(1, repository.submissions);
    repository.pending.failure("NETWORK_ERROR");
    assertFalse(vm.busy);
    assertEquals(1, vm.selected().size());
    assertEquals(" Study ", vm.name());
    vm.create();
    assertEquals(key, repository.key);
    assertEquals("Study", repository.request.name);
    assertEquals(List.of("friend"), repository.request.inviteeUserIds);
  }

  @Test
  public void changedRequestGetsNewKey() throws Exception {
    vm.name("First");
    vm.create();
    String first = repository.key;
    repository.pending.failure("NETWORK_ERROR");
    vm.name("Second");
    vm.create();
    assertNotEquals(first, repository.key);
  }

  @Test
  public void successfulCreateOpensSharedConversationIdentity() throws Exception {
    vm.name("Study");
    vm.create();
    Created created = new Created();
    created.group = new Summary();
    created.group.conversation = new Conversation();
    created.group.conversation.id = "group-1";
    repository.pending.success(created);
    assertFalse(vm.busy);
    assertEquals("detail", vm.screen());
    assertEquals("group-1", vm.groupId());
    assertTrue(vm.selected().isEmpty());
  }

  @Test
  public void savedDraftAndSelectionSurviveNewViewModel() throws Exception {
    vm.name("Study");
    vm.toggle(new Person("friend", "Friend", true));
    vm.create();
    String key = repository.key;
    GroupsViewModel restored = new GroupsViewModel(saved, repository, directory, scope);
    restored.create();
    assertEquals("Study", restored.name());
    assertEquals(1, restored.selected().size());
    assertEquals(key, repository.key);
  }

  @Test
  public void friendsAndSearchFailureRemainHonest() throws Exception {
    vm.show("create");
    assertEquals(1, vm.people.size());
    vm.search("+84912345678");
    assertEquals("HTTP_503", vm.error);
    assertFalse(vm.busy);
  }

  @Test
  public void logoutClearsDraftSelectionAndRejectsLateCreateSuccess() throws Exception {
    vm.name("Private draft");
    vm.toggle(new Person("friend", "Friend", true));
    vm.create();
    GroupRepository.Result<Created> oldCallback = repository.pending;
    scope.close();
    assertTrue(scope.awaitCleanup(5, java.util.concurrent.TimeUnit.SECONDS));
    assertEquals("", vm.name());
    assertTrue(vm.selected().isEmpty());
    assertNull(vm.groupId());
    assertFalse(vm.busy);
    assertTrue(vm.groups.isEmpty());
    assertEquals("SCOPE_EXPIRED", vm.error);
    int version = vm.changed.getValue();
    Created stale = new Created();
    stale.group = new Summary();
    stale.group.conversation = new Conversation();
    stale.group.conversation.id = "old-group";
    oldCallback.success(stale);
    oldCallback.failure("NETWORK_ERROR");
    assertEquals(version, (int) vm.changed.getValue());
    assertNull(vm.groupId());
    vm.name("should not persist");
    vm.create();
    assertEquals("", vm.name());
    assertEquals(1, repository.submissions);
  }

  @Test
  public void newEpochCannotRestoreOldAccountDraftOrRetryKey() throws Exception {
    vm.name("Private draft");
    vm.toggle(new Person("friend", "Friend", true));
    vm.create();
    GroupsAccountScope replacement =
        new GroupsAccountScope(
            userId, UUID.randomUUID(), () -> GroupsAccountScope.Access.ACTIVE, Runnable::run);
    GroupsViewModel restored = new GroupsViewModel(saved, new TestGroups(), directory, replacement);
    assertEquals("", restored.name());
    assertTrue(restored.selected().isEmpty());
    assertNull(saved.get("key"));
    restored.name("New private draft");
    scope.close();
    assertTrue(scope.awaitCleanup(5, java.util.concurrent.TimeUnit.SECONDS));
    assertEquals("New private draft", restored.name());
    assertEquals("", vm.name());
  }

  @Test
  public void directoryResponseQueuedBeforeLogoutCannotRepopulatePeople() throws Exception {
    final GroupRepository.Result<Page<Person>>[] pending = new GroupRepository.Result[1];
    PeopleDirectory delayed =
        new PeopleDirectory() {
          public String currentUserId() {
            return userId.toString();
          }

          public void friends(int page, GroupRepository.Result<Page<Person>> callback) {
            pending[0] = callback;
          }

          public void searchPhone(
              String query, int page, GroupRepository.Result<Page<Person>> callback) {
            pending[0] = callback;
          }

          public void close() {}
        };
    GroupsViewModel target =
        new GroupsViewModel(new SavedStateHandle(), repository, delayed, scope);
    target.show("create");
    scope.close();
    assertTrue(scope.awaitCleanup(5, java.util.concurrent.TimeUnit.SECONDS));
    Page<Person> response = new Page<>();
    response.items = List.of(new Person("old-user", "Private profile", true));
    pending[0].success(response);
    assertTrue(target.people.isEmpty());
    assertEquals("SCOPE_EXPIRED", target.error);
  }

  @Test
  public void clearingViewModelRejectsLateCallbackWithinSameAccount() throws Exception {
    vm.name("Keep draft");
    vm.create();
    androidx.lifecycle.ViewModelStore store = new androidx.lifecycle.ViewModelStore();
    store.put("groups", vm);
    store.clear();
    int version = vm.changed.getValue();
    repository.pending.failure("NETWORK_ERROR");
    assertEquals(version, (int) vm.changed.getValue());
    assertEquals("Keep draft", vm.name());
    vm.name("late edit");
    assertEquals("Keep draft", vm.name());
  }

  @Test
  public void transientRefreshSuspendsDeliveryWithoutLosingDraftOrRetryKey() throws Exception {
    java.util.concurrent.atomic.AtomicReference<GroupsAccountScope.Access> access =
        new java.util.concurrent.atomic.AtomicReference<>(GroupsAccountScope.Access.ACTIVE);
    GroupsAccountScope refreshing =
        new GroupsAccountScope(userId, UUID.randomUUID(), access::get, Runnable::run);
    TestGroups remote = new TestGroups();
    GroupsViewModel target =
        new GroupsViewModel(new SavedStateHandle(), remote, directory, refreshing);
    target.name("Keep on refresh");
    target.create();
    String key = remote.key;
    access.set(GroupsAccountScope.Access.SUSPENDED);
    refreshing.sessionChanged();
    remote.pending.failure("NETWORK_ERROR");
    assertEquals("Keep on refresh", target.name());
    assertTrue(target.busy);
    assertEquals("", target.error);
    assertTrue(refreshing.isCurrent());
    assertFalse(refreshing.hasAccess());
    target.create();
    assertEquals(1, remote.submissions);
    access.set(GroupsAccountScope.Access.ACTIVE);
    refreshing.sessionChanged();
    assertFalse(target.busy);
    assertEquals("NETWORK_ERROR", target.error);
    target.retry();
    assertEquals(key, remote.key);
    assertEquals(2, remote.submissions);
  }

  static class TestGroups implements GroupRepository {
    boolean closed;

    public void close() {
      closed = true;
    }

    int submissions;
    String key;
    Create request;
    Result<Created> pending;

    public void create(String k, Create r, Result<Created> c) {
      submissions++;
      key = k;
      request = r;
      pending = c;
    }

    public void groups(int p, Result<Page<Summary>> c) {
      c.success(new Page<>());
    }

    public void details(String id, Result<Summary> c) {
      c.failure("HTTP_404");
    }

    public void members(String g, int p, Result<Page<Member>> c) {
      c.success(new Page<>());
    }

    public void outgoing(String g, int p, Result<Page<Invitation>> c) {
      c.success(new Page<>());
    }

    public void invitations(int p, Result<Page<Invitation>> c) {
      c.success(new Page<>());
    }

    public void respond(String id, boolean a, Result<Invitation> c) {
      c.failure("HTTP_409");
    }
  }
}
