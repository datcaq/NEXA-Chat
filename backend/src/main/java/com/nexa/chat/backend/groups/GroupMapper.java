package com.nexa.chat.backend.groups;

import com.nexa.chat.backend.conversation.*;
import java.time.Instant;
import java.util.UUID;

/** Explicit wire projections keep future persistence fields out of the public API. */
public final class GroupMapper {
  private GroupMapper() {}

  public static final class ConversationView {
    public UUID id;
    public String type, title, avatarUrl;
    public Instant createdAt, updatedAt;

    public ConversationView() {}

    private ConversationView(Conversation c) {
      id = c.id;
      type = c.type;
      title = c.title;
      avatarUrl = c.avatarUrl;
      createdAt = c.createdAt;
      updatedAt = c.updatedAt;
    }
  }

  public static final class MemberView {
    public UUID conversationId, userId, lastReadMessageId;
    public String role, status;
    public Instant joinedAt, leftAt;

    public MemberView() {}

    private MemberView(ConversationMember m) {
      conversationId = m.conversationId;
      userId = m.userId;
      lastReadMessageId = m.lastReadMessageId;
      role = m.role;
      status = m.status;
      joinedAt = m.joinedAt;
      leftAt = m.leftAt;
    }
  }

  public static final class InvitationView {
    public UUID id, conversationId, inviterUserId, inviteeUserId;
    public String status;
    public Instant createdAt, respondedAt;

    public InvitationView() {}

    private InvitationView(GroupInvitation i) {
      id = i.id;
      conversationId = i.conversationId;
      inviterUserId = i.inviterUserId;
      inviteeUserId = i.inviteeUserId;
      status = i.status;
      createdAt = i.createdAt;
      respondedAt = i.respondedAt;
    }
  }

  public static ConversationView conversation(Conversation value) {
    return new ConversationView(value);
  }

  public static MemberView member(ConversationMember value) {
    return new MemberView(value);
  }

  public static InvitationView invitation(GroupInvitation value) {
    return new InvitationView(value);
  }
}
