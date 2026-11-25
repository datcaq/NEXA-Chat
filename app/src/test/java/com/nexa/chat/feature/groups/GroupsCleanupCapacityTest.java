package com.nexa.chat.feature.groups;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;

public class GroupsCleanupCapacityTest {
  @Test
  public void resourceAndNotificationLanesBoundAdmissionAndRecoverExactlyOnce() throws Exception {
    for (boolean notification : new boolean[] {false, true}) {
      GroupsDiagnostics diagnostics = new GroupsDiagnostics();
      AtomicInteger closed = new AtomicInteger();
      List<GroupsCleanup.Job> jobs = new ArrayList<>();
      try {
        for (int i = 0; i < 128; i++)
          jobs.add(
              notification
                  ? GroupsCleanup.reserveNotification(closed::incrementAndGet, diagnostics)
                  : GroupsCleanup.reserve(closed::incrementAndGet, diagnostics));
        assertThrows(
            RejectedExecutionException.class,
            () -> {
              if (notification)
                GroupsCleanup.reserveNotification(() -> fail("unadopted"), diagnostics);
              else GroupsCleanup.reserve(() -> fail("unadopted"), diagnostics);
            });
        assertTrue(
            diagnostics.snapshot().stream()
                .anyMatch(e -> e.code.equals("CLEANUP_ADMISSION_REJECTED")));
      } finally {
        for (GroupsCleanup.Job job : jobs) {
          job.submit();
          job.submit();
        }
        for (GroupsCleanup.Job job : jobs) assertTrue(job.await(TimeUnit.SECONDS.toNanos(5)));
      }
      assertEquals(128, closed.get());
      GroupsCleanup.Job recovered =
          notification
              ? GroupsCleanup.reserveNotification(closed::incrementAndGet, diagnostics)
              : GroupsCleanup.reserve(closed::incrementAndGet, diagnostics);
      recovered.submit();
      assertTrue(recovered.await(TimeUnit.SECONDS.toNanos(5)));
      assertEquals(129, closed.get());
    }
  }

  @Test
  public void cancelledUnsubmittedOwnershipReleasesSlotWithoutRunningAction() throws Exception {
    GroupsDiagnostics diagnostics = new GroupsDiagnostics();
    GroupsCleanup.Job job = GroupsCleanup.reserve(() -> fail("withdrawn ownership"), diagnostics);
    job.cancelBeforeSubmission();
    job.cancelBeforeSubmission();
    job.submit();
    assertTrue(job.await(TimeUnit.SECONDS.toNanos(5)));
    assertEquals(0, diagnostics.total());
  }
}
