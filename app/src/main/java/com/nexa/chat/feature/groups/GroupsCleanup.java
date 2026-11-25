package com.nexa.chat.feature.groups;

import java.util.ArrayDeque;
import java.util.concurrent.*;

/** Two Groups-only daemon workers; reservations bound queued AND running cleanup to 128. */
final class GroupsCleanup {
  private static final Semaphore slots = new Semaphore(128);
  private static final ArrayDeque<Job> queue = new ArrayDeque<>();

  static {
    for (int i = 0; i < 2; i++) {
      Thread worker = new Thread(GroupsCleanup::work, "groups-cleanup-" + i);
      worker.setDaemon(true);
      worker.start();
    }
  }

  static Job reserve(Runnable action, GroupsDiagnostics diagnostics) {
    if (!slots.tryAcquire()) {
      RejectedExecutionException e =
          new RejectedExecutionException("Groups cleanup ownership capacity");
      diagnostics.record("CLEANUP_ADMISSION_REJECTED", e);
      throw e; // Ownership has NOT transferred; factory must release unadopted allocations.
    }
    return new Job(action, diagnostics);
  }

  private static void work() {
    for (; ; ) {
      Job job;
      synchronized (queue) {
        while (queue.isEmpty()) {
          try {
            queue.wait();
          } catch (InterruptedException ignored) {
            /* process lifetime */
          }
        }
        job = queue.removeFirst();
      }
      job.run();
    }
  }

  static final class Job {
    private Runnable action;
    private final GroupsDiagnostics diagnostics;
    private boolean submitted, done;
    private final CountDownLatch finished = new CountDownLatch(1);

    Job(Runnable action, GroupsDiagnostics diagnostics) {
      this.action = action;
      this.diagnostics = diagnostics;
    }

    synchronized void submit() {
      if (done || submitted) return;
      submitted = true;
      synchronized (queue) {
        queue.addLast(this);
        queue.notifyAll();
      }
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
      slots.release();
      finished.countDown();
    }

    boolean await(long nanos) throws InterruptedException {
      return finished.await(Math.max(0, nanos), TimeUnit.NANOSECONDS);
    }
  }
}
