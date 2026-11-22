package com.nexa.chat.feature.groups.viewmodel;

import androidx.lifecycle.*;
import com.nexa.chat.feature.groups.model.GroupModels.*;
import com.nexa.chat.feature.groups.repository.*;
import java.util.*;

public final class GroupsViewModel extends ViewModel {
  public final MutableLiveData<Integer> changed = new MutableLiveData<>(0);
  private final SavedStateHandle saved;
  private final GroupRepository repository;
  private final PeopleDirectory directory;
  public boolean busy, hasNext;
  public String error = "";
  public int page;
  public List<Summary> groups = Collections.emptyList();
  public List<Person> people = Collections.emptyList();
  public List<Invitation> invitations = Collections.emptyList();
  public List<Member> members = Collections.emptyList();
  public Summary detail;
  private int generation;
  private boolean createFailed;

  public GroupsViewModel(
      SavedStateHandle saved, GroupRepository repository, PeopleDirectory directory) {
    this.saved = saved;
    this.repository = repository;
    this.directory = directory;
  }

  public String screen() {
    return saved.get("screen") == null ? "groups" : saved.get("screen");
  }

  public String name() {
    String v = saved.get("name");
    return v == null ? "" : v;
  }

  public String query() {
    String v = saved.get("query");
    return v == null ? "" : v;
  }

  public String groupId() {
    return saved.get("group");
  }

  public void name(String value) {
    if (!name().equals(value)) {
      saved.set("name", value);
      saved.set("key", null);
    }
  }

  public ArrayList<Person> selected() {
    ArrayList<Person> list = saved.get("selected");
    return list == null ? new ArrayList<>() : new ArrayList<>(list);
  }

  public boolean isSelected(String id) {
    return selected().stream().anyMatch(p -> p.id.equals(id));
  }

  public boolean toggle(Person person) {
    if (busy
        || person == null
        || person.id == null
        || !person.eligible
        || person.id.equals(directory.currentUserId())) return false;
    ArrayList<Person> list = selected();
    if (!list.removeIf(p -> p.id.equals(person.id))) {
      if (list.size() >= 100) {
        error = "BATCH_LIMIT";
        notifyChange();
        return false;
      }
      list.add(person);
    }
    saved.set("selected", list);
    saved.set("key", null);
    notifyChange();
    return true;
  }

  public void show(String screen) {
    if (busy) return;
    saved.set("screen", screen);
    page = 0;
    error = "";
    people = Collections.emptyList();
    invitations = Collections.emptyList();
    members = Collections.emptyList();
    load(0);
  }

  public void search(String phone) {
    if (busy) return;
    people = Collections.emptyList();
    saved.set("query", phone.trim());
    saved.set("screen", "create");
    load(0);
  }

  public void friends() {
    if (busy) return;
    people = Collections.emptyList();
    saved.set("query", "");
    load(0);
  }

  public void retry() {
    if (createFailed) create();
    else load(page);
  }

  public void open(String id) {
    if (busy) return;
    saved.set("group", id);
    saved.set("screen", "detail");
    detail = null;
    load(0);
  }

  public void load(int requestedPage) {
    if (busy || requestedPage < 0) return;
    busy = true;
    error = "";
    createFailed = false;
    int token = ++generation;
    notifyChange();
    switch (screen()) {
      case "create":
        GroupRepository.Result<Page<Person>> callback =
            result(
                token,
                v -> {
                  people = v.items;
                  page = v.page;
                  hasNext = v.hasNext;
                });
        if (query().isEmpty()) directory.friends(requestedPage, callback);
        else directory.searchPhone(query(), requestedPage, callback);
        break;
      case "incoming":
        repository.invitations(
            requestedPage,
            result(
                token,
                v -> {
                  invitations = v.items;
                  page = v.page;
                  hasNext = v.hasNext;
                }));
        break;
      case "members":
        repository.members(
            groupId(),
            requestedPage,
            result(
                token,
                v -> {
                  members = v.items;
                  page = v.page;
                  hasNext = v.hasNext;
                }));
        break;
      case "outgoing":
        repository.outgoing(
            groupId(),
            requestedPage,
            result(
                token,
                v -> {
                  invitations = v.items;
                  page = v.page;
                  hasNext = v.hasNext;
                }));
        break;
      case "detail":
        repository.details(
            groupId(),
            result(
                token,
                v -> {
                  detail = v;
                  hasNext = false;
                }));
        break;
      default:
        repository.groups(
            requestedPage,
            result(
                token,
                v -> {
                  groups = v.items;
                  page = v.page;
                  hasNext = v.hasNext;
                }));
    }
  }

  public void create() {
    if (busy) return;
    String title = name().trim();
    if (title.isEmpty() || title.length() > 120) {
      error = "INVALID_NAME";
      notifyChange();
      return;
    }
    String key = saved.get("key");
    if (key == null) {
      key = UUID.randomUUID().toString();
      saved.set("key", key);
    }
    busy = true;
    error = "";
    createFailed = true;
    int token = ++generation;
    notifyChange();
    repository.create(
        key,
        new Create(
            title,
            selected().stream().map(p -> p.id).collect(java.util.stream.Collectors.toList())),
        result(
            token,
            v -> {
              createFailed = false;
              detail = v.group;
              saved.set("group", v.group.conversation.id);
              saved.set("screen", "detail");
              saved.set("selected", new ArrayList<Person>());
              saved.set("name", "");
              saved.set("key", null);
              hasNext = false;
            }));
  }

  public void respond(Invitation invitation, boolean accept) {
    if (busy) return;
    busy = true;
    error = "";
    int token = ++generation;
    notifyChange();
    repository.respond(
        invitation.id,
        accept,
        result(
            token,
            v -> {
              invitations = new ArrayList<>(invitations);
              invitations.removeIf(i -> i.id.equals(v.id));
            }));
  }

  private interface Apply<T> {
    void accept(T value);
  }

  private <T> GroupRepository.Result<T> result(int token, Apply<T> apply) {
    return new GroupRepository.Result<T>() {
      public void success(T value) {
        if (token != generation) return;
        apply.accept(value);
        busy = false;
        notifyChange();
      }

      public void failure(String code) {
        if (token != generation) return;
        busy = false;
        error = code;
        notifyChange();
      }
    };
  }

  private void notifyChange() {
    changed.setValue(changed.getValue() == null ? 1 : changed.getValue() + 1);
  }

  @Override
  protected void onCleared() {
    generation++;
  }
}
