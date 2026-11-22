package com.nexa.chat.backend.groups;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "group_creation_keys")
public class GroupCreationKey {
  @Id public UUID id;
  public UUID userId;
  public String key;
  public String requestHash;
  public UUID conversationId;
}
