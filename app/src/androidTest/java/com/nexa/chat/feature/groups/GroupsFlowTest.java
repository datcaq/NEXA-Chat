package com.nexa.chat.feature.groups;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.graphics.Bitmap;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.nexa.chat.R;
import com.nexa.chat.feature.groups.model.GroupModels.*;
import com.nexa.chat.feature.groups.repository.*;
import com.nexa.chat.feature.groups.ui.GroupsActivity;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;

/** Isolated UI fixture only: production adapters are not replaced or shipped in main. */
@RunWith(AndroidJUnit4.class)
public class GroupsFlowTest {
  Fixture groups;
  PeopleDirectory people;

  @Before
  public void setup() {
    groups = new Fixture();
    people =
        new PeopleDirectory() {
          public String currentUserId() {
            return "owner";
          }

          public void friends(int p, GroupRepository.Result<Page<Person>> c) {
            Page<Person> page = new Page<>();
            page.items = List.of(new Person("friend", "An", true));
            c.success(page);
          }

          public void searchPhone(String q, int p, GroupRepository.Result<Page<Person>> c) {
            Page<Person> page = new Page<>();
            page.items = List.of(new Person("phone-user", "Bình", true));
            c.success(page);
          }
        };
    GroupsIntegration.install(new GroupsIntegration.Dependencies(groups, people, null));
  }

  @After
  public void cleanup() {
    GroupsIntegration.install(null);
  }

  @Test
  public void createFromFriendsAndPhoneThenInspectPending() throws Exception {
    try (ActivityScenario<GroupsActivity> scenario =
        ActivityScenario.launch(GroupsActivity.class)) {
      capture("01-groups-empty");
      onView(withText(R.string.groups_create)).perform(click());
      onView(withId(R.id.groups_name_input))
          .perform(replaceText("NEXA Study"), closeSoftKeyboard());
      onView(withText(R.string.groups_select)).perform(click());
      onView(withId(R.id.groups_phone_input))
          .perform(replaceText("+84912345678"), closeSoftKeyboard());
      onView(withText(R.string.groups_search)).perform(click());
      onView(withText(R.string.groups_select)).perform(click());
      onView(withText("Xem người đã chọn (2)")).check(matches(isDisplayed()));
      scenario.recreate();
      onView(withId(R.id.groups_name_input)).check(matches(withText("NEXA Study")));
      onView(withText("Xem người đã chọn (2)")).check(matches(isDisplayed()));
      capture("02-create-selected");
      onView(withText(R.string.groups_submit)).perform(click());
      onView(withId(android.R.id.button1)).perform(click());
      onView(withText("NEXA Study")).check(matches(isDisplayed()));
      capture("03-group-details");
      onView(withText(R.string.groups_pending)).perform(click());
      onView(withSubstring("Đang chờ")).check(matches(isDisplayed()));
      capture("04-pending-invitations");
      Assert.assertEquals(List.of("friend", "phone-user"), groups.request.inviteeUserIds);
    }
  }

  @Test
  public void incomingAcceptAndReject() {
    try (ActivityScenario<GroupsActivity> ignored = ActivityScenario.launch(GroupsActivity.class)) {
      onView(withText(R.string.groups_incoming)).perform(click());
      onView(withText(R.string.groups_accept)).perform(click());
      onView(withText(R.string.groups_empty)).check(matches(isDisplayed()));
      onView(withText(R.string.groups_cancel)).perform(click());
      onView(withText(R.string.groups_incoming)).perform(click());
      onView(withText(R.string.groups_reject)).perform(click());
      onView(withText(R.string.groups_empty)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void skeletonOpensGroupsWithoutInventingAuthentication() {
    GroupsIntegration.install(null);
    try (ActivityScenario<com.nexa.chat.MainActivity> ignored =
        ActivityScenario.launch(com.nexa.chat.MainActivity.class)) {
      onView(withId(R.id.groups_entry)).perform(click());
      onView(withText(R.string.groups_setup)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void failedCreateKeepsDraftAndCanRetry() {
    groups.fail = true;
    try (ActivityScenario<GroupsActivity> ignored = ActivityScenario.launch(GroupsActivity.class)) {
      onView(withText(R.string.groups_create)).perform(click());
      onView(withId(R.id.groups_name_input))
          .perform(replaceText("Retry group"), closeSoftKeyboard());
      onView(withText(R.string.groups_submit)).perform(click());
      onView(withId(android.R.id.button1)).perform(click());
      onView(withId(R.id.groups_name_input)).check(matches(withText("Retry group")));
      String key = groups.key;
      groups.fail = false;
      onView(withText(R.string.groups_retry)).perform(click());
      Assert.assertEquals(key, groups.key);
      onView(withText("Retry group")).check(matches(isDisplayed()));
    }
  }

  private void capture(String name) throws Exception {
    var instrumentation = InstrumentationRegistry.getInstrumentation();
    instrumentation.waitForIdleSync();
    android.os.SystemClock.sleep(500);
    Bitmap bitmap = instrumentation.getUiAutomation().takeScreenshot();
    File dir =
        new File(
            instrumentation.getTargetContext().getExternalFilesDir(null), "group01-screenshots");
    dir.mkdirs();
    try (FileOutputStream out = new FileOutputStream(new File(dir, name + ".png"))) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    }
    bitmap.recycle();
  }

  static class Fixture implements GroupRepository {
    Create request;
    String key;
    boolean fail;

    public void groups(int p, Result<Page<Summary>> c) {
      c.success(new Page<>());
    }

    public void create(String k, Create r, Result<Created> c) {
      key = k;
      request = r;
      if (fail) {
        c.failure("NETWORK_ERROR");
        return;
      }
      Created v = new Created();
      v.group = new Summary();
      v.group.conversation = new Conversation();
      v.group.conversation.id = "group-1";
      v.group.conversation.title = r.name;
      v.group.memberCount = 1;
      v.group.myRole = "OWNER";
      c.success(v);
    }

    public void details(String id, Result<Summary> c) {
      c.failure("HTTP_404");
    }

    public void members(String id, int p, Result<Page<Member>> c) {
      c.success(new Page<>());
    }

    public void outgoing(String id, int p, Result<Page<Invitation>> c) {
      c.success(invitationPage());
    }

    public void invitations(int p, Result<Page<Invitation>> c) {
      c.success(invitationPage());
    }

    public void respond(String id, boolean accept, Result<Invitation> c) {
      Invitation i = new Invitation();
      i.id = id;
      i.status = accept ? "ACCEPTED" : "REJECTED";
      c.success(i);
    }

    Page<Invitation> invitationPage() {
      Page<Invitation> page = new Page<>();
      Invitation i = new Invitation();
      i.id = "invite-1";
      i.conversationId = "group-1";
      i.inviteeUserId = "friend";
      i.status = "PENDING";
      page.items = List.of(i);
      return page;
    }
  }
}
