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
      this.scope = java.util.Objects.requireNonNull(scope);
      try {
        this.groups = java.util.Objects.requireNonNull(groups);
        scope.own(groups::close);
        this.people = java.util.Objects.requireNonNull(people);
        scope.own(people::close);
        if (groups instanceof com.nexa.chat.feature.groups.data.RemoteGroupRepository)
          ((com.nexa.chat.feature.groups.data.RemoteGroupRepository) groups).bindScope(scope);
        if (!scope.userId().toString().equals(people.currentUserId()))
          throw new IllegalArgumentException(
              "Directory identity must match the Groups account scope");
      } catch (RuntimeException failure) {
        scope.diagnostics().record("DEPENDENCY_INITIALIZATION_FAILURE", failure);
        scope.close();
        throw failure; // factory preparation failure, not a session-observer cleanup throw
      }
      this.chat = chat;
    }
  }

  private static volatile Dependencies dependencies;
  private static Dependencies preparing;
  private static long revision;

  private GroupsIntegration() {}

  public static void install(Dependencies value) {
    Dependencies previous;
    long ticket;
    synchronized (GroupsIntegration.class) {
      previous = dependencies;
      if ((previous == value && preparing == null) || (value != null && preparing == value)) return;
      if (previous != null && value != null && previous.scope == value.scope)
        throw new IllegalArgumentException(
            "Reuse existing dependencies while refreshing the same scope");
      dependencies = null;
      // close() is now a bounded logical transition + reserved job handoff, never external cleanup.
      // Invalidate both published and in-preparation scopes before another candidate can publish.
      if (previous != null) previous.scope.close();
      if (preparing != null && preparing != previous) preparing.scope.close();
      preparing = value;
      ticket = ++revision;
    }
    boolean valid =
        value != null && value.scope.isCurrent(); // external supplier outside static lock
    boolean installed = false;
    synchronized (GroupsIntegration.class) {
      if (ticket == revision) {
        preparing = null;
        if (valid) {
          dependencies = value;
          installed = true;
        }
      }
    }
    if (value != null && !installed) value.scope.close();
  }

  public static Dependencies get() {
    return dependencies;
  }

  /** TV1's existing Nhóm tab calls this; shared navigation is not replaced. */
  public static void open(Activity activity) {
    activity.startActivity(new Intent(activity, GroupsActivity.class));
  }
}
