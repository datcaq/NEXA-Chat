package com.nexa.chat.backend.groups;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nexa.chat.backend.conversation.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {
  @Mock ConversationRepository conversations;
  @Mock MemberRepository members;
  @Mock InvitationRepository invitations;
  @Mock CreationKeyRepository keys;
  @Mock GroupDirectory directory;
  @Mock WriteBudget budget;
  @InjectMocks GroupService service;
  UUID owner = UUID.randomUUID(), invitee = UUID.randomUUID();
  String key = "test-request-key-123";

  GroupContracts.Create request(List<UUID> ids) {
    return new GroupContracts.Create("  Study  ", ids);
  }

  void freshKey() {
    when(keys.reserve(any(), eq(owner), eq(key), any()))
        .thenAnswer(
            invocation -> {
              GroupCreationKey operation = new GroupCreationKey();
              operation.requestHash = invocation.getArgument(3);
              when(keys.findByUserIdAndKey(owner, key)).thenReturn(Optional.of(operation));
              return 1;
            });
  }

  @Test
  void createsOwnerAndPendingInvitations() {
    freshKey();
    when(conversations.save(any())).thenAnswer(i -> i.getArgument(0));
    when(members.save(any())).thenAnswer(i -> i.getArgument(0));
    var result = service.create(owner, key, request(List.of(invitee)));
    assertEquals("Study", result.group().conversation().title);
    assertEquals("GROUP", result.group().conversation().type);
    assertEquals("OWNER", result.creatorMembership().role);
    assertEquals("ACTIVE", result.creatorMembership().status);
    assertEquals("PENDING", result.invitations().getFirst().status);
    verify(members, times(1)).save(any());
    verify(directory).requireEligibleInvitation(owner, invitee);
  }

  @Test
  void invalidNames() {
    assertEquals(
        400,
        assertThrows(
                GroupProblem.class,
                () -> service.create(owner, key, new GroupContracts.Create("  ", List.of())))
            .status);
    verifyNoInteractions(conversations);
  }

  @Test
  void duplicateInvitees() {
    assertEquals(
        "DUPLICATE_INVITEES",
        assertThrows(
                GroupProblem.class,
                () -> service.create(owner, key, request(List.of(invitee, invitee))))
            .code);
  }

  @Test
  void cannotInviteSelf() {
    assertEquals(
        "SELF_INVITATION",
        assertThrows(GroupProblem.class, () -> service.create(owner, key, request(List.of(owner))))
            .code);
  }

  @Test
  void unknownAndBlockedInviteesPreventWrites() {
    freshKey();
    doThrow(new GroupProblem(422, "INELIGIBLE", "Unavailable"))
        .when(directory)
        .requireEligibleInvitation(owner, invitee);
    assertThrows(GroupProblem.class, () -> service.create(owner, key, request(List.of(invitee))));
    verifyNoInteractions(conversations, members, invitations);
  }

  GroupInvitation pending() {
    var i = new GroupInvitation(UUID.randomUUID(), owner, invitee);
    when(invitations.lock(i.id)).thenReturn(Optional.of(i));
    return i;
  }

  @Test
  void acceptanceCreatesMember() {
    var i = pending();
    when(members.existsByConversationIdAndUserIdAndStatus(i.conversationId, owner, "ACTIVE"))
        .thenReturn(true);
    when(invitations.save(any())).thenAnswer(v -> v.getArgument(0));
    assertEquals("ACCEPTED", service.respond(invitee, i.id, true).status);
    ArgumentCaptor<ConversationMember> member = ArgumentCaptor.forClass(ConversationMember.class);
    verify(members).save(member.capture());
    assertEquals("MEMBER", member.getValue().role);
    assertNotNull(i.respondedAt);
  }

  @Test
  void rejectionDoesNotCreateMember() {
    var i = pending();
    when(invitations.save(any())).thenAnswer(v -> v.getArgument(0));
    assertEquals("REJECTED", service.respond(invitee, i.id, false).status);
    verifyNoInteractions(members);
  }

  @Test
  void alreadyProcessedIsConflict() {
    var i = pending();
    i.status = "REJECTED";
    assertEquals(
        409, assertThrows(GroupProblem.class, () -> service.respond(invitee, i.id, true)).status);
    verifyNoInteractions(members);
  }

  @Test
  void anotherUserCannotRespond() {
    var i = pending();
    assertEquals(
        404, assertThrows(GroupProblem.class, () -> service.respond(owner, i.id, true)).status);
  }

  @Test
  void nonmemberCannotReadGroup() {
    assertEquals(
        404,
        assertThrows(GroupProblem.class, () -> service.detail(invitee, UUID.randomUUID())).status);
    verifyNoInteractions(conversations);
  }

  @Test
  void invalidPageRejected() {
    assertThrows(GroupProblem.class, () -> service.list(owner, 0, 1000));
  }

  @Test
  void keyCannotBeReusedForDifferentRequest() {
    GroupCreationKey operation = new GroupCreationKey();
    operation.requestHash = "different";
    when(keys.findByUserIdAndKey(owner, key)).thenReturn(Optional.of(operation));
    assertEquals(
        409,
        assertThrows(GroupProblem.class, () -> service.create(owner, key, request(List.of())))
            .status);
    verifyNoInteractions(conversations);
  }
}
