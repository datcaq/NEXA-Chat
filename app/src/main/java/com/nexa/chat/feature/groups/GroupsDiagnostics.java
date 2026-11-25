package com.nexa.chat.feature.groups;

import java.util.*;

/** Feature-local bounded diagnostics; no request/token/payload logging. */
public final class GroupsDiagnostics {
  public static final int CAPACITY = 32;

  public static final class Event {
    public final String code;
    public final RuntimeException cause;

    Event(String code, RuntimeException cause) {
      this.code = code;
      this.cause = cause;
    }
  }

  private final ArrayDeque<Event> events = new ArrayDeque<>();
  private long total;

  public synchronized void record(String code, RuntimeException cause) {
    total++;
    if (events.size() == CAPACITY) events.removeFirst();
    events.addLast(new Event(code, cause));
  }

  public synchronized long total() {
    return total;
  }

  public synchronized List<Event> snapshot() {
    return new ArrayList<>(events);
  }
}
