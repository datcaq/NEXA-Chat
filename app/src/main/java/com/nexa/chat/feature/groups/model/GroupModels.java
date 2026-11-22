package com.nexa.chat.feature.groups.model;

import java.io.Serializable;
import java.util.*;

public final class GroupModels {
  private GroupModels() {}

  public static class Person implements Serializable {
    public String id, displayName, avatarUrl, publicIdentifier;
    public boolean eligible;

    public Person(String id, String name, boolean eligible) {
      this.id = id;
      displayName = name;
      this.eligible = eligible;
    }
  }

  public static class Conversation {
    public String id, type, title, avatarUrl, createdAt, updatedAt;
  }

  public static class Summary {
    public String myRole;
    public Conversation conversation;
    public long memberCount;
  }

  public static class Member {
    public String userId, role, status, joinedAt;
  }

  public static class Invitation {
    public String id, conversationId, inviteeUserId, status;
  }

  public static class Page<T> {
    public List<T> items = Collections.emptyList();
    public boolean hasNext;
    public int page;
  }

  public static class Created {
    public Summary group;
    public Member creatorMembership;
    public List<Invitation> invitations;
  }

  public static class Create {
    public final String name;
    public final List<String> inviteeUserIds;

    public Create(String name, List<String> ids) {
      this.name = name;
      inviteeUserIds = ids;
    }
  }
}
