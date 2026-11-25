package com.nexa.chat.feature.groups.data;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.LongSupplier;

/** Independent Groups runtime: 64 live/retiring requests, two workers, one bounded watchdog. */
final class GroupRequestRuntime {
  static final int GLOBAL_CAPACITY = 64;
  static final GroupRequestRuntime SHARED = production();
  final Executor executor;
  final LongSupplier clock;
  private final Set<GroupRequestHandle> live = new HashSet<>();

  GroupRequestRuntime(Executor executor, LongSupplier clock) {
    this.executor = executor;
    this.clock = clock;
  }

  private static ThreadFactory threads(String name) {
    return task -> {
      Thread t = new Thread(task, name);
      t.setDaemon(true);
      return t;
    };
  }

  private static GroupRequestRuntime production() {
    // At most dispatch+cancel+result per live handle: <=192 queued/running tasks, not unbounded.
    ThreadPoolExecutor work =
        new ThreadPoolExecutor(
            2, 2, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(256), threads("groups-request"));
    GroupRequestRuntime runtime = new GroupRequestRuntime(work, System::nanoTime);
    ScheduledExecutorService timer =
        Executors.newSingleThreadScheduledExecutor(threads("groups-watchdog"));
    timer.scheduleWithFixedDelay(runtime::poll, 100, 100, TimeUnit.MILLISECONDS);
    return runtime;
  }

  synchronized boolean admit(GroupRequestHandle request) {
    if (live.size() == GLOBAL_CAPACITY) return false;
    live.add(request);
    return true;
  }

  synchronized void release(GroupRequestHandle request) {
    live.remove(request);
  }

  void poll() {
    List<GroupRequestHandle> copy;
    synchronized (this) {
      copy = new ArrayList<>(live);
    }
    for (GroupRequestHandle request : copy) request.checkDeadline(clock.getAsLong());
  }
}
