package com.nexa.chat.backend.conversation;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Shared conversation aggregate. Messaging must reuse this identity. */
@Entity
@Table(name = "conversations")
public class Conversation {
  @Id public UUID id;
  public String type;
  public String title;
  public String avatarUrl;
  public Instant createdAt;
  public Instant updatedAt;

  protected Conversation() {}

  public Conversation(String title) {
    id = UUID.randomUUID();
    type = "GROUP";
    this.title = title;
    createdAt = updatedAt = Instant.now();
  }
}
