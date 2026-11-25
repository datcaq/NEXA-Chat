package com.nexa.chat.backend.groups;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface CreationKeyRepository extends JpaRepository<GroupCreationKey, UUID> {
  @Modifying
  @Query(
      value =
          "insert into group_creation_keys(id,user_id,key,request_hash) values"
              + " (:id,:user,:key,:hash) on conflict(user_id,key) do nothing",
      nativeQuery = true)
  int reserve(UUID id, UUID user, String key, String hash);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<GroupCreationKey> findByUserIdAndKey(UUID user, String key);
}
