package com.nexa.chat.feature.groups.data;

import com.nexa.chat.feature.groups.*;
import com.nexa.chat.feature.groups.model.GroupModels.*;
import com.nexa.chat.feature.groups.repository.GroupRepository;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import retrofit2.Call;

/** Feature-owned requests; never owns the shared OkHttp client/dispatcher or credentials. */
public final class RemoteGroupRepository implements GroupRepository {
  public static final int REQUEST_CAPACITY = 16;
  public static final long REQUEST_TIMEOUT_SECONDS = 30, SHUTDOWN_DEADLINE_SECONDS = 5;
  private final GroupApi api;
  private final GroupRequestRuntime runtime;
  private final long timeoutNanos, shutdownNanos;
  private final GroupsDiagnostics diagnostics = new GroupsDiagnostics();
  private final Set<GroupRequestHandle> requests = new LinkedHashSet<>();
  private final boolean requiresScope;
  private GroupsAccountScope scope;
  private boolean closed;

  /** Compatibility constructor: composition root must bindScope before production use. */
  public RemoteGroupRepository(GroupApi api) {
    this(
        api,
        GroupRequestRuntime.SHARED,
        TimeUnit.SECONDS.toNanos(REQUEST_TIMEOUT_SECONDS),
        TimeUnit.SECONDS.toNanos(SHUTDOWN_DEADLINE_SECONDS),
        true);
  }

  public RemoteGroupRepository(GroupApi api, GroupsAccountScope scope) {
    this(api);
    bindScope(scope);
  }

  RemoteGroupRepository(
      GroupApi api, GroupRequestRuntime runtime, long timeoutNanos, long shutdownNanos) {
    this(
        api,
        runtime,
        timeoutNanos,
        shutdownNanos,
        false); // package-local deterministic transport tests
  }

  private RemoteGroupRepository(
      GroupApi api,
      GroupRequestRuntime runtime,
      long timeoutNanos,
      long shutdownNanos,
      boolean requiresScope) {
    this.api = Objects.requireNonNull(api);
    this.runtime = runtime;
    this.requiresScope = requiresScope;
    if (timeoutNanos <= 0 || shutdownNanos <= 0)
      throw new IllegalArgumentException("Positive deadlines required");
    this.timeoutNanos = timeoutNanos;
    this.shutdownNanos = shutdownNanos;
  }

  public synchronized void bindScope(GroupsAccountScope value) {
    Objects.requireNonNull(value);
    if (!closed && scope == value) return;
    if (closed || !requests.isEmpty() || (scope != null && scope != value))
      throw new IllegalStateException("Repository cannot cross account scopes or bind after use");
    scope = value;
    scope.own(this, this::close);
  }

  public GroupsDiagnostics diagnostics() {
    return diagnostics;
  }

  public synchronized List<GroupRequestHandle> requests() {
    return new ArrayList<>(requests);
  }

  public synchronized int inFlight() {
    return requests.size();
  }

  boolean canStart() {
    GroupsAccountScope captured;
    synchronized (this) {
      if (closed) return false;
      captured = scope;
    }
    return captured == null ? !requiresScope : captured.hasAccess();
  }

  boolean canDeliver() {
    GroupsAccountScope captured;
    synchronized (this) {
      if (closed) return false;
      captured = scope;
    }
    return captured == null ? !requiresScope : captured.isCurrent();
  }

  void handoffResult(Runnable action) {
    GroupsAccountScope captured;
    synchronized (this) {
      captured = scope;
    }
    if (captured == null) action.run();
    else if (!captured.tryDispatch(action)) {
      captured.close();
      throw new RejectedExecutionException("Groups result handoff unavailable");
    }
  }

  /** Concrete TV3 API for a cancellable request. GroupRepository signatures stay unchanged. */
  public <T> GroupRequestHandle execute(Supplier<Call<T>> factory, Result<T> callback) {
    Objects.requireNonNull(factory);
    Objects.requireNonNull(callback);
    if (!canStart()) throw new RejectedExecutionException("Groups scope unavailable");
    GroupRequestHandle handle;
    synchronized (this) {
      if (closed || requests.size() == REQUEST_CAPACITY) {
        diagnostics.record("REQUEST_ADMISSION_REJECTED", null);
        throw new RejectedExecutionException("Groups request capacity or closed scope");
      }
      handle =
          new GroupRequestHandle(
              this,
              scope == null ? null : scope.userId(),
              scope == null ? null : scope.epoch(),
              runtime,
              timeoutNanos,
              shutdownNanos,
              callback);
      if (!runtime.admit(handle)) {
        diagnostics.record("GLOBAL_REQUEST_CAPACITY", null);
        throw new RejectedExecutionException("Groups runtime capacity");
      }
      requests.add(handle);
    }
    handle.start(factory, callback);
    return handle;
  }

  synchronized void settled(GroupRequestHandle handle) {
    requests.remove(handle);
    notifyAll();
  }

  @Override
  public void close() {
    List<GroupRequestHandle> copy;
    synchronized (this) {
      if (closed) return;
      closed = true;
      copy = new ArrayList<>(requests);
    }
    for (GroupRequestHandle request : copy) request.cancel();
  }

  /** Background/test wait only. Timeout leaves unknown physical work charged to capacity. */
  public synchronized boolean awaitTermination(long timeout, TimeUnit unit)
      throws InterruptedException {
    long deadline = System.nanoTime() + unit.toNanos(timeout);
    while (!requests.isEmpty()) {
      long left = deadline - System.nanoTime();
      if (left <= 0) return false;
      TimeUnit.NANOSECONDS.timedWait(this, left);
    }
    return true;
  }

  public void groups(int p, Result<Page<Summary>> c) {
    execute(() -> api.groups(p), c);
  }

  public void create(String k, Create r, Result<Created> c) {
    execute(() -> api.create(k, r), c);
  }

  public void details(String g, Result<Summary> c) {
    execute(() -> api.details(g), c);
  }

  public void members(String g, int p, Result<Page<Member>> c) {
    execute(() -> api.members(g, p), c);
  }

  public void outgoing(String g, int p, Result<Page<Invitation>> c) {
    execute(() -> api.outgoing(g, p), c);
  }

  public void invitations(int p, Result<Page<Invitation>> c) {
    execute(() -> api.invitations(p), c);
  }

  public void respond(String id, boolean accept, Result<Invitation> c) {
    execute(() -> accept ? api.accept(id) : api.reject(id), c);
  }
}
