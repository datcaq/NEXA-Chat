package com.nexa.chat.backend.groups;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface InvitationRepository extends JpaRepository<GroupInvitation, UUID> {
  boolean existsByConversationIdAndInviteeUserIdAndStatus(UUID group, UUID invitee, String status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from GroupInvitation i where i.id=:id")
  Optional<GroupInvitation> lock(UUID id);

  Slice<GroupInvitation> findByInviteeUserIdAndStatus(UUID user, String status, Pageable page);

  Slice<GroupInvitation> findByConversationId(UUID group, Pageable page);
}
