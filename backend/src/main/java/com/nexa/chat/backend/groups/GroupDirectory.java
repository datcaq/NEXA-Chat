package com.nexa.chat.backend.groups;

import java.util.UUID;

/**
 * TV1 integration: must check existence, discoverability and both block directions. Run inside the
 * caller transaction; never authorize from Android-provided eligibility.
 */
public interface GroupDirectory {
  void requireExistingUser(UUID user);

  void requireEligibleInvitation(UUID inviter, UUID invitee);
}
