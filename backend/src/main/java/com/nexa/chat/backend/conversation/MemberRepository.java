package com.nexa.chat.backend.conversation;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<ConversationMember, UUID> {
  boolean existsByConversationIdAndUserIdAndStatus(UUID group, UUID user, String status);

  Optional<ConversationMember> findByConversationIdAndUserId(UUID group, UUID user);

  long countByConversationIdAndStatus(UUID group, String status);

  Slice<ConversationMember> findByConversationIdAndStatus(UUID group, String status, Pageable page);
}
