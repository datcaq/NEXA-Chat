package com.nexa.chat.backend.conversation;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_members")
public class ConversationMember {
  @Id public UUID id;
  public UUID conversationId;
  public UUID userId;
  public String role;
  public String status;
  public Instant joinedAt;
  public Instant leftAt;
  public UUID lastReadMessageId;

  protected ConversationMember() {}

  public ConversationMember(UUID conversation, UUID user, String role) {
    id = UUID.randomUUID();
    conversationId = conversation;
    userId = user;
    this.role = role;
    status = "ACTIVE";
    joinedAt = Instant.now();
  }
}
