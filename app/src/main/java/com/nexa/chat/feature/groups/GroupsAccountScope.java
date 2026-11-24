package com.nexa.chat.feature.groups;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Groups resources for one already authenticated (user, epoch). This is not authentication. TV1
 * supplies a latest-snapshot predicate and the coordinator's serial UI executor. Invalidate this
 * scope before publishing a replacement account scope.
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

  private final UUID userId;
  private final UUID epoch;
  private final Supplier<Access> currentSession;
  private final Executor uiExecutor;
  private final Object lock = new Object();
  private final List<Runnable> resources = new ArrayList<>();
  private final Set<Listener> listeners = new LinkedHashSet<>();
  private final Set<Listener> accessListeners = new LinkedHashSet<>();
  private final List<Runnable> deferred = new ArrayList<>();
  private boolean closed;

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

  public boolean isCurrent() {
    synchronized (lock) {
      return !closed && currentSession.get() != Access.EXPIRED;
    }
  }

  public boolean hasAccess() {
    synchronized (lock) {
      return !closed && currentSession.get() == Access.ACTIVE;
    }
  }

  /** Check and mutation are serialized against close, including already queued callbacks. */
  public boolean runIfCurrent(Runnable action) {
    synchronized (lock) {
      if (closed || currentSession.get() != Access.ACTIVE) return false;
      action.run();
      return true;
    }
  }

  public void dispatch(Runnable action) {
    uiExecutor.execute(() -> deliver(action));
  }

  private void deliver(Runnable action) {
    synchronized (lock) {
      if (closed) return;
      Access access = Objects.requireNonNull(currentSession.get());
      if (access == Access.SUSPENDED) {
        deferred.add(action);
        return;
      }
      if (access == Access.ACTIVE) {
        action.run();
        return;
      }
    }
    close();
  }

  /** TV1 calls this on each latest session snapshot; suspension is not logout. */
  public void sessionChanged() {
    uiExecutor.execute(
        () -> {
          List<Runnable> pending;
          List<Listener> observers;
          Access access;
          synchronized (lock) {
            if (closed) return;
            access = Objects.requireNonNull(currentSession.get());
            pending = access == Access.ACTIVE ? new ArrayList<>(deferred) : Collections.emptyList();
            if (access == Access.ACTIVE) deferred.clear();
            observers = new ArrayList<>(accessListeners);
          }
          if (access == Access.EXPIRED) {
            close();
            return;
          }
          for (Runnable action : pending) deliver(action);
          for (Listener observer : observers) observer.deliver();
        });
  }

  public Registration onAccessChanged(Runnable action) {
    Listener listener = new Listener(Objects.requireNonNull(action), false);
    synchronized (lock) {
      if (!closed) accessListeners.add(listener);
      else listener.active = false;
    }
    return listener;
  }

  /** Own only feature-specific resources, never TV1's shared HTTP client or contacts module. */
  public void own(Runnable cleanup) {
    Objects.requireNonNull(cleanup);
    synchronized (lock) {
      if (!closed) {
        resources.add(cleanup);
        return;
      }
    }
    cleanup.run();
  }

  public Registration onClosed(Runnable listener) {
    Objects.requireNonNull(listener);
    Listener registration = new Listener(listener, true);
    synchronized (lock) {
      if (!closed) {
        listeners.add(registration);
        return registration;
      }
    }
    uiExecutor.execute(registration::deliver);
    return registration;
  }

  private final class Listener implements Registration {
    private final Runnable action;
    private boolean active = true;
    private final boolean once;

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
    }

    void deliver() {
      synchronized (lock) {
        if (!active) return;
        if (once) active = false;
      }
      action.run();
    }
  }

  @Override
  public void close() {
    List<Runnable> cleanup;
    List<Listener> notifications;
    synchronized (lock) {
      if (closed) return;
      closed = true;
      deferred.clear();
      for (Listener listener : accessListeners) listener.active = false;
      accessListeners.clear();
      cleanup = new ArrayList<>(resources);
      resources.clear();
      notifications = new ArrayList<>(listeners);
      listeners.clear();
    }
    RuntimeException failure = null;
    for (Runnable resource : cleanup) {
      try {
        resource.run();
      } catch (RuntimeException e) {
        if (failure == null) failure = new IllegalStateException("Groups scope cleanup failed");
        failure.addSuppressed(e);
      }
    }
    for (Listener listener : notifications) {
      try {
        uiExecutor.execute(listener::deliver);
      } catch (RuntimeException e) {
        if (failure == null)
          failure = new IllegalStateException("Groups scope notification failed");
        failure.addSuppressed(e);
      }
    }
    if (failure != null) throw failure;
  }
}
