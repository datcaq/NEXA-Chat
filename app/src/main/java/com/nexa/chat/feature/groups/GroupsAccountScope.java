package com.nexa.chat.feature.groups;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Feature scope, not authentication. Host supplies prompt latest-session predicate and UI handoff.
 */
public final class GroupsAccountScope implements AutoCloseable {
  public enum Access {
    ACTIVE,
    SUSPENDED,
    EXPIRED
  }

  public interface Registration extends AutoCloseable {
    @Override
    void close();
  }

  public static final int CALLBACK_CAPACITY = 32, RESOURCE_CAPACITY = 8, LISTENER_CAPACITY = 16;
  private final UUID userId, epoch;
  private final Supplier<Access> currentSession;
  private final Executor uiExecutor;
  private final GroupsDiagnostics diagnostics = new GroupsDiagnostics();
  private final Object lock = new Object();
  private final ArrayDeque<Runnable> pending = new ArrayDeque<>();
  private final List<GroupsCleanup.Job> resources = new ArrayList<>(),
      cleanupHistory = new ArrayList<>();
  private final IdentityHashMap<Object, GroupsCleanup.Job> owned = new IdentityHashMap<>();
  private final Set<Listener> listeners = new LinkedHashSet<>(),
      accessListeners = new LinkedHashSet<>();
  private boolean closed, running, scheduled, accessDirty;
  private int closedRegistrations;

  public GroupsAccountScope(
      UUID userId, UUID epoch, Supplier<Access> currentSession, Executor uiExecutor) {
    this.userId = Objects.requireNonNull(userId);
    this.epoch = Objects.requireNonNull(epoch);
    this.currentSession = Objects.requireNonNull(currentSession);
    this.uiExecutor = Objects.requireNonNull(uiExecutor);
  }

  public UUID userId() {
    return userId;
  }

  public UUID epoch() {
    return epoch;
  }

  public GroupsDiagnostics diagnostics() {
    return diagnostics;
  }

  private Access access() {
    synchronized (lock) {
      if (closed) return Access.EXPIRED;
    }
    Access value;
    try {
      value = Objects.requireNonNull(currentSession.get());
    } catch (RuntimeException e) {
      diagnostics.record("SESSION_PROJECTION_FAILURE", e);
      close();
      return Access.EXPIRED;
    }
    if (value == Access.EXPIRED) close();
    synchronized (lock) {
      return closed ? Access.EXPIRED : value;
    }
  }

  public boolean isCurrent() {
    return access() != Access.EXPIRED;
  }

  public boolean hasAccess() {
    return access() == Access.ACTIVE;
  }

  /** Nonwaiting immediate admission. An already claimed action may finish after close. */
  public boolean runIfCurrent(Runnable action) {
    Objects.requireNonNull(action);
    if (access() != Access.ACTIVE) return false;
    synchronized (lock) {
      if (closed || running || !pending.isEmpty()) return false;
      running = true;
    }
    try {
      invoke(action);
    } finally {
      synchronized (lock) {
        running = false;
      }
      schedule();
    }
    return true;
  }

  /** Existing void callers cannot handle overload: fail this feature closed, never host Auth. */
  public void dispatch(Runnable action) {
    if (!tryDispatch(action)) close();
  }

  public boolean tryDispatch(Runnable action) {
    Objects.requireNonNull(action);
    boolean rejected;
    synchronized (lock) {
      if (closed) return false;
      rejected = pending.size() == CALLBACK_CAPACITY;
      if (!rejected) pending.addLast(action);
    }
    if (rejected) {
      diagnostics.record("CALLBACK_CAPACITY", null);
      return false;
    }
    schedule();
    return true;
  }

  private void schedule() {
    synchronized (lock) {
      if (closed || scheduled || running || (pending.isEmpty() && !accessDirty)) return;
      scheduled = true;
    }
    try {
      uiExecutor.execute(this::drain);
    } catch (RuntimeException e) {
      synchronized (lock) {
        scheduled = false;
      }
      diagnostics.record("UI_HANDOFF_REJECTED", e);
      close();
    }
  }

  private void drain() {
    synchronized (lock) {
      scheduled = false;
      if (closed || running) return;
      running = true;
    }
    boolean paused = false;
    try {
      for (; ; ) {
        Access latest = access(); // NEVER call the external supplier under state lock.
        paused = latest != Access.ACTIVE;
        Runnable action;
        List<Listener> controls = Collections.emptyList();
        synchronized (lock) {
          if (closed) return;
          if (accessDirty) {
            accessDirty = false;
            controls = new ArrayList<>(accessListeners);
          }
          action = latest == Access.ACTIVE ? pending.pollFirst() : null;
        }
        for (Listener listener : controls) listener.deliver();
        if (action == null) return;
        synchronized (lock) {
          if (closed) return;
        } // controls may have invalidated us
        invoke(action); // admission claim above is serialized with invalidation
      }
    } finally {
      boolean wake;
      synchronized (lock) {
        running = false;
        wake = !closed && (accessDirty || (!paused && !pending.isEmpty()));
      }
      if (wake) schedule();
    }
  }

  private void invoke(Runnable action) {
    try {
      action.run();
    } catch (RuntimeException e) {
      diagnostics.record("CALLBACK_FAILURE", e);
    }
  }

  public void sessionChanged() {
    if (access() == Access.EXPIRED) return;
    synchronized (lock) {
      if (closed) return;
      accessDirty = true;
    }
    schedule();
  }

  public Registration onAccessChanged(Runnable action) {
    return listen(action, false);
  }

  public Registration onClosed(Runnable action) {
    return listen(action, true);
  }

  private Registration listen(Runnable action, boolean once) {
    Objects.requireNonNull(action);
    Listener listener = new Listener(action, once);
    synchronized (lock) {
      Set<Listener> target = once ? listeners : accessListeners;
      if (target.size() == LISTENER_CAPACITY)
        throw new RejectedExecutionException("Groups listener capacity");
      if (once) {
        if (closed && closedRegistrations == LISTENER_CAPACITY)
          throw new RejectedExecutionException("Groups closed notification capacity");
        listener.job =
            GroupsCleanup.reserveNotification(
                () -> uiExecutor.execute(listener::deliver), diagnostics);
        if (!closed) target.add(listener);
        else {
          closedRegistrations++;
          cleanupHistory.add(listener.job);
          listener.job.submit();
        }
      } else if (!closed) target.add(listener);
      else listener.active = false;
    }
    return listener;
  }

  /**
   * Reserve before ownership transfer. Rejected factories remain responsible for unadopted objects.
   */
  public void own(Runnable cleanup) {
    own(cleanup, cleanup);
  }

  /** Same concrete resource is adopted/closed once, including registration after scope closure. */
  public void own(Object resource, Runnable cleanup) {
    Objects.requireNonNull(resource);
    Objects.requireNonNull(cleanup);
    synchronized (lock) {
      if (owned.containsKey(resource)) return;
      if (owned.size() == RESOURCE_CAPACITY)
        throw new RejectedExecutionException("Groups resource capacity");
      GroupsCleanup.Job job = GroupsCleanup.reserve(cleanup, diagnostics);
      owned.put(resource, job);
      cleanupHistory.add(job);
      if (closed) job.submit();
      else resources.add(job);
    }
  }

  private final class Listener implements Registration {
    private final Runnable action;
    private final boolean once;
    private boolean active = true;
    private GroupsCleanup.Job job;

    Listener(Runnable action, boolean once) {
      this.action = action;
      this.once = once;
    }

    public void close() {
      synchronized (lock) {
        active = false;
        listeners.remove(this);
        accessListeners.remove(this);
      }
      if (job != null) job.cancelBeforeSubmission();
    }

    void deliver() {
      synchronized (lock) {
        if (!active || (!once && closed)) return;
        if (once) active = false;
      }
      invoke(action);
    }
  }

  @Override
  public void close() {
    List<GroupsCleanup.Job> cleanup;
    List<Listener> notifications;
    synchronized (lock) {
      if (closed) return;
      closed = true;
      pending.clear();
      accessDirty = false;
      for (Listener listener : accessListeners) listener.active = false;
      accessListeners.clear();
      cleanup = new ArrayList<>(resources);
      resources.clear();
      notifications = new ArrayList<>(listeners);
      listeners.clear();
      closedRegistrations = notifications.size();
      for (Listener listener : notifications) cleanupHistory.add(listener.job);
    }
    // No arbitrary external cleanup, observer invocation or join on the caller/UI.
    for (Listener listener : notifications) listener.job.submit();
    for (GroupsCleanup.Job resource : cleanup) resource.submit();
  }

  /** Background/test wait only: completed UI handoff is NOT UI-idle or transport termination. */
  public boolean awaitCleanup(long timeout, TimeUnit unit) throws InterruptedException {
    List<GroupsCleanup.Job> copy;
    synchronized (lock) {
      copy = new ArrayList<>(cleanupHistory);
    }
    long deadline = System.nanoTime() + unit.toNanos(timeout);
    for (GroupsCleanup.Job job : copy) if (!job.await(deadline - System.nanoTime())) return false;
    return true;
  }
}
