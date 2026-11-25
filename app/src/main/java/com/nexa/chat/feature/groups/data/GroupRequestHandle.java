package com.nexa.chat.feature.groups.data;

import com.nexa.chat.feature.groups.repository.GroupRepository;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import retrofit2.*;

/**
 * A per-account request lease. TERMINATED means Retrofit terminal callback observed, or proven
 * never enqueued. SETTLED additionally requires every owned invocation/result task to return. A
 * cancellation request/deadline NEVER fabricates transport termination.
 */
public final class GroupRequestHandle {
  public enum State {
    ADMITTED,
    DISPATCHED,
    STARTED,
    CANCELLATION_REQUESTED,
    TERMINATED,
    SETTLED
  }

  public enum Delivery {
    PENDING,
    CLAIMED,
    RESULT_DELIVERED,
    SUPPRESSED
  }

  public final UUID requestId = UUID.randomUUID();
  public final UUID userId, sessionEpoch;
  private final RemoteGroupRepository owner;
  private final GroupRequestRuntime runtime;
  private final EnumSet<State> visited = EnumSet.of(State.ADMITTED);
  private State state = State.ADMITTED;
  private Delivery delivery = Delivery.PENDING;
  private Call<?> call;
  private int tasks;
  private boolean cancelled, terminal, released, resultScheduled, cancelScheduled, deadlineReported;
  private long cancelAt;
  private final long admittedAt;
  private final long timeoutNanos, shutdownNanos;
  private Runnable timeoutResult;

  GroupRequestHandle(
      RemoteGroupRepository owner,
      UUID user,
      UUID epoch,
      GroupRequestRuntime runtime,
      long timeoutNanos,
      long shutdownNanos) {
    this.owner = owner;
    this.userId = user;
    this.sessionEpoch = epoch;
    this.runtime = runtime;
    this.timeoutNanos = timeoutNanos;
    this.shutdownNanos = shutdownNanos;
    admittedAt = runtime.clock.getAsLong();
  }

  public synchronized State state() {
    return state;
  }

  public synchronized Delivery delivery() {
    return delivery;
  }

  public synchronized Set<State> visitedStates() {
    return EnumSet.copyOf(visited);
  }

  public synchronized boolean cancellationRequested() {
    return cancelled;
  }

  private void transition(State next) {
    state = next;
    visited.add(next);
  }

  <T> void start(Supplier<Call<T>> factory, GroupRepository.Result<T> callback) {
    synchronized (this) {
      timeoutResult = () -> callback.failure("REQUEST_TIMEOUT");
    }
    task(() -> dispatch(factory, callback));
  }

  private <T> void dispatch(Supplier<Call<T>> factory, GroupRepository.Result<T> callback) {
    synchronized (this) {
      if (cancelled) {
        markNeverStarted();
        return;
      }
      transition(State.DISPATCHED);
    }
    if (!owner.canStart()) {
      cancel();
      synchronized (this) {
        markNeverStarted();
      }
      return;
    }
    Call<T> created;
    try {
      created = Objects.requireNonNull(factory.get());
    } catch (RuntimeException e) {
      owner.diagnostics().record("REQUEST_FACTORY_FAILURE", e);
      synchronized (this) {
        terminal = true;
        transition(State.TERMINATED);
      }
      result(() -> callback.failure("REQUEST_START_FAILED"), false);
      return;
    }
    synchronized (this) {
      call = created;
    }
    boolean skip;
    synchronized (this) {
      skip = cancelled;
    }
    if (!skip && !owner.canStart()) {
      cancel();
      skip = true;
    }
    if (skip) {
      scheduleCancel();
      synchronized (this) {
        markNeverStarted();
      }
      return;
    }
    try {
      created.timeout().timeout(timeoutNanos, TimeUnit.NANOSECONDS);
      synchronized (this) {
        skip = cancelled;
        if (!skip)
          transition(State.STARTED); // enqueue admission, not an invented socket-start event
      }
      if (skip) {
        scheduleCancel();
        synchronized (this) {
          markNeverStarted();
        }
        return;
      }
      created.enqueue(
          new Callback<T>() {
            public void onResponse(Call<T> ignored, Response<T> response) {
              completed(
                  () -> {
                    if (response.isSuccessful() && response.body() != null)
                      callback.success(response.body());
                    else callback.failure("HTTP_" + response.code());
                  });
            }

            public void onFailure(Call<T> ignored, Throwable failure) {
              completed(() -> callback.failure("NETWORK_ERROR"));
            }
          });
    } catch (RuntimeException e) {
      owner.diagnostics().record("ENQUEUE_FAILURE_TERMINATION_UNKNOWN", e);
      Runnable failure;
      long now = runtime.clock.getAsLong();
      synchronized (this) {
        if (terminal) return; // a synchronous terminal callback already chose the result
        cancelled = true;
        cancelAt = now;
        if (visited.contains(State.STARTED)) transition(State.CANCELLATION_REQUESTED);
        else {
          terminal = true;
          transition(State.TERMINATED);
        }
        failure = prepareResult(() -> callback.failure("REQUEST_START_FAILED"), true);
      }
      scheduleCancel();
      if (failure != null) submitRegistered(failure);
    }
  }

  private void markNeverStarted() {
    terminal = true;
    transition(State.TERMINATED);
    if (!resultScheduled) delivery = Delivery.SUPPRESSED;
  }

  private void completed(Runnable callback) {
    Runnable resultWork;
    synchronized (this) {
      if (terminal) {
        owner.diagnostics().record("DUPLICATE_TERMINAL", null);
        return;
      }
      tasks++; // keep the terminal callback invocation charged through its final accounting
      terminal = true;
      transition(State.TERMINATED);
      resultWork = prepareResult(callback, false);
    }
    try {
      if (resultWork != null) submitRegistered(resultWork);
    } finally {
      synchronized (this) {
        tasks--;
      }
      releaseIfDone();
    }
  }

  private void result(Runnable callback, boolean cancellationFailure) {
    Runnable resultWork;
    synchronized (this) {
      resultWork = prepareResult(callback, cancellationFailure);
    }
    if (resultWork != null) submitRegistered(resultWork);
  }

  /** Select one result AND charge its task atomically with timeout/transport terminal state. */
  private Runnable prepareResult(Runnable callback, boolean cancellationFailure) {
    if (resultScheduled || delivery != Delivery.PENDING) return null;
    if (cancelled && !cancellationFailure) {
      delivery = Delivery.SUPPRESSED;
      return null;
    }
    resultScheduled = true;
    tasks++;
    return () -> {
      try {
        owner.handoffResult(
            () -> {
              boolean valid = owner.canDeliver();
              synchronized (this) {
                if (!valid || delivery != Delivery.PENDING || (cancelled && !cancellationFailure)) {
                  if (delivery == Delivery.PENDING) delivery = Delivery.SUPPRESSED;
                } else delivery = Delivery.CLAIMED;
              }
              try {
                synchronized (this) {
                  if (delivery != Delivery.CLAIMED) return;
                }
                callback.run();
              } catch (RuntimeException e) {
                owner.diagnostics().record("RESULT_CALLBACK_FAILURE", e);
              } finally {
                synchronized (this) {
                  if (delivery == Delivery.CLAIMED) delivery = Delivery.RESULT_DELIVERED;
                }
                releaseIfDone();
              }
            });
      } catch (RuntimeException e) {
        owner.diagnostics().record("RESULT_HANDOFF_REJECTED", e);
        synchronized (this) {
          if (delivery == Delivery.PENDING) delivery = Delivery.SUPPRESSED;
        }
      }
    };
  }

  public void cancel() {
    requestCancel();
  }

  private void requestCancel() {
    long now = runtime.clock.getAsLong();
    synchronized (this) {
      if (released || cancelled) return;
      cancelled = true;
      cancelAt = now;
      if (!terminal) transition(State.CANCELLATION_REQUESTED);
      if (delivery == Delivery.PENDING) delivery = Delivery.SUPPRESSED;
    }
    scheduleCancel();
    releaseIfDone();
  }

  private void scheduleCancel() {
    Call<?> captured;
    synchronized (this) {
      if (cancelScheduled || call == null) return;
      cancelScheduled = true;
      captured = call;
      tasks++;
    }
    submitRegistered(
        () -> {
          try {
            captured.cancel();
          } catch (RuntimeException e) {
            owner.diagnostics().record("REQUEST_CANCEL_FAILURE", e);
          }
          // Returning from cancel is NOT terminal transport evidence.
        });
  }

  void checkDeadline(long now) {
    Runnable failure = null;
    boolean timeout;
    synchronized (this) {
      timeout = !released && !terminal && !cancelled && now - admittedAt >= timeoutNanos;
      if (timeout) {
        cancelled = true;
        cancelAt = now;
        transition(State.CANCELLATION_REQUESTED);
        if (timeoutResult != null) failure = prepareResult(timeoutResult, true);
      }
    }
    if (timeout) {
      owner.diagnostics().record("REQUEST_TIMEOUT", null);
      scheduleCancel();
      if (failure != null) submitRegistered(failure);
    }
    synchronized (this) {
      if (!released
          && cancelled
          && !terminal
          && !deadlineReported
          && now - cancelAt >= shutdownNanos) {
        deadlineReported = true;
        owner.diagnostics().record("SHUTDOWN_DEADLINE_TERMINATION_UNKNOWN", null);
      }
    }
  }

  private void task(Runnable work) {
    synchronized (this) {
      tasks++;
    }
    submitRegistered(work);
  }

  private void submitRegistered(Runnable work) {
    AtomicBoolean began = new AtomicBoolean();
    try {
      runtime.executor.execute(
          () -> {
            began.set(true);
            try {
              work.run();
            } finally {
              synchronized (this) {
                tasks--;
              }
              releaseIfDone();
            }
          });
    } catch (RuntimeException e) {
      owner.diagnostics().record("REQUEST_WORK_REJECTED", e);
      if (began.get()) return; // inline executor already ran the task's finally/accounting
      synchronized (this) {
        tasks--;
        if (state == State.ADMITTED) {
          cancelled = true;
          markNeverStarted();
        }
        if (delivery == Delivery.PENDING) delivery = Delivery.SUPPRESSED;
      }
      // Retain started/unknown work until actual terminal evidence. No inline external fallback.
      releaseIfDone();
    }
  }

  private void releaseIfDone() {
    synchronized (this) {
      if (released
          || !terminal
          || tasks != 0
          || (delivery != Delivery.SUPPRESSED && delivery != Delivery.RESULT_DELIVERED)) return;
      released = true;
      call = null;
      timeoutResult = null;
      transition(State.SETTLED);
    }
    owner.settled(this);
    runtime.release(this);
  }
}
