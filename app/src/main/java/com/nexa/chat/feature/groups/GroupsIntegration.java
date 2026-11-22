package com.nexa.chat.feature.groups;

import android.app.Activity;
import android.content.Intent;
import com.nexa.chat.feature.groups.repository.*;
import com.nexa.chat.feature.groups.ui.GroupsActivity;

/** Install once from the Application composition root; all dependencies are real adapters. */
public final class GroupsIntegration {
  public interface ChatNavigator {
    void open(Activity activity, String conversationId);
  }

  public static final class Dependencies {
    public final GroupRepository groups;
    public final PeopleDirectory people;
    public final ChatNavigator chat;

    public Dependencies(GroupRepository groups, PeopleDirectory people, ChatNavigator chat) {
      this.groups = groups;
      this.people = people;
      this.chat = chat;
    }
  }

  private static volatile Dependencies dependencies;

  private GroupsIntegration() {}

  public static void install(Dependencies value) {
    dependencies = value;
  }

  public static Dependencies get() {
    return dependencies;
  }

  /** TV1's existing Nhóm tab calls this; shared navigation is not replaced. */
  public static void open(Activity activity) {
    activity.startActivity(new Intent(activity, GroupsActivity.class));
  }
}
