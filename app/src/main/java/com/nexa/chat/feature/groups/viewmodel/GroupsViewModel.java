package com.nexa.chat.feature.groups.viewmodel;

import androidx.lifecycle.*;
import com.nexa.chat.feature.groups.GroupsAccountScope;
import com.nexa.chat.feature.groups.model.GroupModels.*;
import com.nexa.chat.feature.groups.repository.*;
import java.util.*;

public final class GroupsViewModel extends ViewModel {
  public final MutableLiveData<Integer> changed = new MutableLiveData<>(0);
  private final SavedStateHandle saved;
  private final GroupRepository repository;
  private final PeopleDirectory directory;
  private final GroupsAccountScope scope;
  private final GroupsAccountScope.Registration closure;
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
  private boolean disposed;
  private boolean initialLoadStarted;

  public GroupsViewModel(
      SavedStateHandle saved,
      GroupRepository repository,
      PeopleDirectory directory,
      GroupsAccountScope scope) {
    this.saved = saved;
    this.repository = repository;
    this.directory = directory;
    this.scope = scope;
    if (!scope.userId().toString().equals(saved.get("scope.user"))
        || !scope.epoch().toString().equals(saved.get("scope.epoch"))) clearSavedAccount();
    saved.set("scope.user", scope.userId().toString());
    saved.set("scope.epoch", scope.epoch().toString());
    closure = scope.onClosed(this::invalidate);
  }

  public String screen() {
    if (!ownsSavedAccount()) return "groups";
    return saved.get("screen") == null ? "groups" : saved.get("screen");
  }

  public String name() {
    if (!ownsSavedAccount()) return "";
    String v = saved.get("name");
    return v == null ? "" : v;
  }

  public String query() {
    if (!ownsSavedAccount()) return "";
    String v = saved.get("query");
    return v == null ? "" : v;
  }

  public String groupId() {
    if (!ownsSavedAccount()) return null;
    return saved.get("group");
  }

  public void name(String value) {
    if (!ensureScope()) return;
    if (!name().equals(value)) {
      saved.set("name", value);
      saved.set("key", null);
    }
  }

  public ArrayList<Person> selected() {
    if (!ownsSavedAccount()) return new ArrayList<>();
    ArrayList<Person> list = saved.get("selected");
    return list == null ? new ArrayList<>() : new ArrayList<>(list);
  }

  public boolean isSelected(String id) {
    return selected().stream().anyMatch(p -> p.id.equals(id));
  }

  public boolean toggle(Person person) {
    if (!ensureScope()
        || busy
        || person == null
        || person.id == null
        || !person.eligible
        || person.id.equals(scope.userId().toString())) return false;
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
    if (!ensureScope() || busy) return;
    saved.set("screen", screen);
    page = 0;
    error = "";
    people = Collections.emptyList();
    invitations = Collections.emptyList();
    members = Collections.emptyList();
    load(0);
  }

  public void search(String phone) {
    if (!ensureScope() || busy) return;
    people = Collections.emptyList();
    saved.set("query", phone.trim());
    saved.set("screen", "create");
    load(0);
  }

  public void friends() {
    if (!ensureScope() || busy) return;
    people = Collections.emptyList();
    saved.set("query", "");
    load(0);
  }

  public void retry() {
    if (!ensureScope()) return;
    if (createFailed) create();
    else load(page);
  }

  public void open(String id) {
    if (!ensureScope() || busy) return;
    saved.set("group", id);
    saved.set("screen", "detail");
    detail = null;
    load(0);
  }

  public void load(int requestedPage) {
    if (!ensureScope() || busy || requestedPage < 0) return;
    initialLoadStarted = true;
    busy = true;
    error = "";
    createFailed = false;
    int token = ++generation;
    notifyChange();
    try {
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
    } catch (java.util.concurrent.RejectedExecutionException rejected) {
      admissionRejected();
    }
  }

  /** Opening while CHECKING must defer, not permanently lose, the first authorized load. */
  public void ensureInitialLoad() {
    if (!initialLoadStarted) load(0);
  }

  public void create() {
    if (!ensureScope() || busy) return;
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
    try {
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
    } catch (java.util.concurrent.RejectedExecutionException rejected) {
      admissionRejected();
    }
  }

  public void respond(Invitation invitation, boolean accept) {
    if (!ensureScope() || busy) return;
    busy = true;
    error = "";
    int token = ++generation;
    notifyChange();
    try {
      repository.respond(
          invitation.id,
          accept,
          result(
              token,
              v -> {
                invitations = new ArrayList<>(invitations);
                invitations.removeIf(i -> i.id.equals(v.id));
              }));
    } catch (java.util.concurrent.RejectedExecutionException rejected) {
      admissionRejected();
    }
  }

  private void admissionRejected() {
    scope.diagnostics().record("VIEWMODEL_REQUEST_REJECTED", null);
    if (!ensureScope()) return;
    busy = false;
    error = "REQUEST_CAPACITY";
    notifyChange();
  }

  private interface Apply<T> {
    void accept(T value);
  }

  private <T> GroupRepository.Result<T> result(int token, Apply<T> apply) {
    return new GroupRepository.Result<T>() {
      public void success(T value) {
        scope.dispatch(
            () -> {
              if (token != generation || !ownsSavedAccount()) return;
              apply.accept(value);
              busy = false;
              notifyChange();
            });
      }

      public void failure(String code) {
        scope.dispatch(
            () -> {
              if (token != generation || !ownsSavedAccount()) return;
              busy = false;
              error = code;
              notifyChange();
            });
      }
    };
  }

  private void notifyChange() {
    changed.setValue(changed.getValue() == null ? 1 : changed.getValue() + 1);
  }

  private boolean ensureScope() {
    if (disposed) return false;
    if (scope.hasAccess()) return ownsSavedAccount();
    if (!scope.isCurrent()) scope.close();
    return false;
  }

  private boolean ownsSavedAccount() {
    return scope.isCurrent() && matchesSavedOwner();
  }

  private boolean matchesSavedOwner() {
    return scope.userId().toString().equals(saved.get("scope.user"))
        && scope.epoch().toString().equals(saved.get("scope.epoch"));
  }

  private void clearSavedAccount() {
    for (String key :
        Arrays.asList(
            "scope.user", "scope.epoch", "screen", "name", "query", "selected", "key", "group"))
      saved.remove(key);
  }

  private void invalidate() {
    generation++;
    if (matchesSavedOwner()) clearSavedAccount();
    busy = false;
    hasNext = false;
    page = 0;
    createFailed = false;
    groups = Collections.emptyList();
    people = Collections.emptyList();
    invitations = Collections.emptyList();
    members = Collections.emptyList();
    detail = null;
    error = "SCOPE_EXPIRED";
    notifyChange();
  }

  @Override
  protected void onCleared() {
    disposed = true;
    generation++;
    closure.close();
  }
}
