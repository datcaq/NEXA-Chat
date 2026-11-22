package com.nexa.chat.backend.groups;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_invitations")
public class GroupInvitation {
  @Id public UUID id;
  public UUID conversationId;
  public UUID inviterUserId;
  public UUID inviteeUserId;
  public String status;
  public Instant createdAt;
  public Instant respondedAt;

  protected GroupInvitation() {}

  public GroupInvitation(UUID group, UUID inviter, UUID invitee) {
    id = UUID.randomUUID();
    conversationId = group;
    inviterUserId = inviter;
    inviteeUserId = invitee;
    status = "PENDING";
    createdAt = Instant.now();
  }
}
