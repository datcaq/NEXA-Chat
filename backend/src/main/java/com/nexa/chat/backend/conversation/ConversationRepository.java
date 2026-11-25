package com.nexa.chat.backend.conversation;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Conversation c where c.id=:id")
  Optional<Conversation> lock(UUID id);

  @Query(
      "select c from Conversation c where c.type='GROUP' and exists (select m.id from"
          + " ConversationMember m where m.conversationId=c.id and m.userId=:user and"
          + " m.status='ACTIVE')")
  Slice<Conversation> mine(UUID user, Pageable pageable);
}
