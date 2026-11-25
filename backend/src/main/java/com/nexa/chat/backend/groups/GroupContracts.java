package com.nexa.chat.backend.groups;

import com.nexa.chat.backend.conversation.*;
import jakarta.validation.constraints.*;
import java.util.*;

public final class GroupContracts {
  private GroupContracts() {}

  // A batch limit protects request resources; it is not a maximum group size.
  public record Create(
      @NotBlank @Size(max = 120) String name,
      @NotNull @Size(max = 100) List<@NotNull UUID> inviteeUserIds) {}

  public record Invite(@NotNull @Size(min = 1, max = 100) List<@NotNull UUID> inviteeUserIds) {}

  public record Page<T>(List<T> items, boolean hasNext, int page) {}

  public record Summary(
      GroupMapper.ConversationView conversation, long memberCount, String myRole) {}

  public record Created(
      Summary group,
      GroupMapper.MemberView creatorMembership,
      List<GroupMapper.InvitationView> invitations) {}

  public static <T> Page<T> page(org.springframework.data.domain.Slice<T> slice, int page) {
    return new Page<>(slice.getContent(), slice.hasNext(), page);
  }
}
