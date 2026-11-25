package com.nexa.chat.feature.groups;

import java.util.ArrayDeque;
import java.util.concurrent.*;

/** Independent bounded physical cleanup and terminal-UI handoff lanes, never Messaging workers. */
final class GroupsCleanup {
  private static final Lane resources = new Lane("groups-cleanup", 2);
  private static final Lane notifications = new Lane("groups-terminal-ui", 1);

  static Job reserve(Runnable action, GroupsDiagnostics diagnostics) {
    return resources.reserve(action, diagnostics);
  }

  static Job reserveNotification(Runnable action, GroupsDiagnostics diagnostics) {
    return notifications.reserve(action, diagnostics);
  }

  /** Each lane reserves at most128 queued/running jobs BEFORE ownership transfer. */
  private static final class Lane {
    private final Semaphore slots = new Semaphore(128);
    private final ArrayDeque<Job> queue = new ArrayDeque<>();

    Lane(String name, int workers) {
      for (int i = 0; i < workers; i++) {
        Thread worker = new Thread(this::work, name + "-" + i);
        worker.setDaemon(true);
        worker.start();
      }
    }

    Job reserve(Runnable action, GroupsDiagnostics diagnostics) {
      if (!slots.tryAcquire()) {
        RejectedExecutionException e =
            new RejectedExecutionException("Groups cleanup ownership capacity");
        diagnostics.record("CLEANUP_ADMISSION_REJECTED", e);
        throw e; // Ownership has NOT transferred. Factory retains unadopted allocations.
      }
      return new Job(this, action, diagnostics);
    }

    void enqueue(Job job) {
      synchronized (queue) {
        queue.addLast(job);
        queue.notifyAll();
      }
    }

    private void work() {
      for (; ; ) {
        Job job;
        synchronized (queue) {
          while (queue.isEmpty()) {
            try {
              queue.wait();
            } catch (InterruptedException ignored) {
              /* process lifetime, no user work under lock */
            }
          }
          job = queue.removeFirst();
        }
        job.run();
      }
    }
  }

  static final class Job {
    private final Lane lane;
    private Runnable action;
    private final GroupsDiagnostics diagnostics;
    private boolean submitted, done;
    private final CountDownLatch finished = new CountDownLatch(1);

    Job(Lane lane, Runnable action, GroupsDiagnostics diagnostics) {
      this.lane = lane;
      this.action = action;
      this.diagnostics = diagnostics;
    }

    synchronized void submit() {
      if (done || submitted) return;
      submitted = true;
      lane.enqueue(this);
    }

    synchronized void cancelBeforeSubmission() {
      if (done || submitted) return;
      finish();
    }

    private void run() {
      Runnable task;
      synchronized (this) {
        task = action;
      }
      try {
        task.run();
      } catch (RuntimeException e) {
        diagnostics.record("CLEANUP_FAILURE", e);
      } finally {
        synchronized (this) {
          finish();
        }
      }
    }

    private void finish() {
      if (done) return;
      done = true;
      action = null;
      lane.slots.release();
      finished.countDown();
    }

    boolean await(long nanos) throws InterruptedException {
      return finished.await(Math.max(0, nanos), TimeUnit.NANOSECONDS);
    }
  }
}
