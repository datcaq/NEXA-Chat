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
    public final GroupsAccountScope scope;

    public Dependencies(
        GroupRepository groups,
        PeopleDirectory people,
        ChatNavigator chat,
        GroupsAccountScope scope) {
      this.groups = java.util.Objects.requireNonNull(groups);
      this.people = java.util.Objects.requireNonNull(people);
      this.chat = chat;
      this.scope = java.util.Objects.requireNonNull(scope);
      if (!scope.userId().toString().equals(people.currentUserId()))
        throw new IllegalArgumentException(
            "Directory identity must match the Groups account scope");
      scope.own(groups::close);
      scope.own(people::close);
    }
  }

  private static volatile Dependencies dependencies;

  private GroupsIntegration() {}

  public static synchronized void install(Dependencies value) {
    Dependencies previous = dependencies;
    if (previous == value) return;
    if (previous != null && value != null && previous.scope == value.scope)
      throw new IllegalArgumentException(
          "Reuse existing dependencies while refreshing the same scope");
    dependencies = null;
    if (previous != null) {
      try {
        previous.scope.close();
      } catch (RuntimeException e) {
        if (value != null) value.scope.close();
        throw e;
      }
    }
    if (value != null && value.scope.isCurrent()) dependencies = value;
    else if (value != null) value.scope.close();
  }

  public static Dependencies get() {
    return dependencies;
  }

  /** TV1's existing Nhóm tab calls this; shared navigation is not replaced. */
  public static void open(Activity activity) {
    activity.startActivity(new Intent(activity, GroupsActivity.class));
  }
}
