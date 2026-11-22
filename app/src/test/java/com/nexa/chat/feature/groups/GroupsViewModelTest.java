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
  PeopleDirectory directory =
      new PeopleDirectory() {
        public String currentUserId() {
          return "me";
        }

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
  public void setup() {
    repository = new TestGroups();
    saved = new SavedStateHandle();
    vm = new GroupsViewModel(saved, repository, directory);
  }

  @Test
  public void blankNameDoesNotCallServer() {
    vm.name("  ");
    vm.create();
    assertEquals("INVALID_NAME", vm.error);
    assertEquals(0, repository.submissions);
  }

  @Test
  public void selectionIsUniqueAndRemovable() {
    Person p = new Person("friend", "Friend", true);
    assertTrue(vm.toggle(p));
    assertEquals(1, vm.selected().size());
    vm.toggle(new Person("friend", "Same user", true));
    assertTrue(vm.selected().isEmpty());
    assertFalse(vm.toggle(new Person("me", "Me", true)));
    assertFalse(vm.toggle(new Person("blocked", "Blocked", false)));
  }

  @Test
  public void networkRetryKeepsPayloadAndKey() {
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
  public void changedRequestGetsNewKey() {
    vm.name("First");
    vm.create();
    String first = repository.key;
    repository.pending.failure("NETWORK_ERROR");
    vm.name("Second");
    vm.create();
    assertNotEquals(first, repository.key);
  }

  @Test
  public void successfulCreateOpensSharedConversationIdentity() {
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
  public void savedDraftAndSelectionSurviveNewViewModel() {
    vm.name("Study");
    vm.toggle(new Person("friend", "Friend", true));
    vm.create();
    String key = repository.key;
    GroupsViewModel restored = new GroupsViewModel(saved, repository, directory);
    restored.create();
    assertEquals("Study", restored.name());
    assertEquals(1, restored.selected().size());
    assertEquals(key, repository.key);
  }

  @Test
  public void friendsAndSearchFailureRemainHonest() {
    vm.show("create");
    assertEquals(1, vm.people.size());
    vm.search("+84912345678");
    assertEquals("HTTP_503", vm.error);
    assertFalse(vm.busy);
  }

  static class TestGroups implements GroupRepository {
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
