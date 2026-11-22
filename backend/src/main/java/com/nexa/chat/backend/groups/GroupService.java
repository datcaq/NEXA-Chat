package com.nexa.chat.backend.groups;

import static com.nexa.chat.backend.groups.GroupContracts.*;

import com.nexa.chat.backend.conversation.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GroupService {
  private final ConversationRepository conversations;
  private final MemberRepository members;
  private final InvitationRepository invitations;
  private final CreationKeyRepository keys;
  private final GroupDirectory directory;
  private final WriteBudget budget;

  public GroupService(
      ConversationRepository c,
      MemberRepository m,
      InvitationRepository i,
      CreationKeyRepository k,
      GroupDirectory d,
      WriteBudget b) {
    conversations = c;
    members = m;
    invitations = i;
    keys = k;
    directory = d;
    budget = b;
  }

  @Transactional
  public Created create(UUID user, String key, Create request) {
    if (key == null || !key.matches("[A-Za-z0-9_-]{16,128}"))
      throw new GroupProblem(
          400, "INVALID_IDEMPOTENCY_KEY", "Supply a 16–128 character Idempotency-Key");
    if (request == null
        || request.name() == null
        || request.name().strip().isEmpty()
        || request.name().strip().length() > 120)
      throw new GroupProblem(
          400, "INVALID_NAME", "Group name is required (maximum 120 characters)");
    if (request.inviteeUserIds() == null
        || request.inviteeUserIds().size() > 100
        || request.inviteeUserIds().stream().anyMatch(Objects::isNull))
      throw new GroupProblem(400, "INVALID_INVITEES", "Use batches of at most 100 invitees");
    Set<UUID> unique = new HashSet<>(request.inviteeUserIds());
    if (unique.size() != request.inviteeUserIds().size())
      throw new GroupProblem(422, "DUPLICATE_INVITEES", "Select each person once");
    if (unique.contains(user))
      throw new GroupProblem(422, "SELF_INVITATION", "The creator is already the owner");
    String name = request.name().strip();
    String hash = fingerprint(name, unique);
    directory.requireExistingUser(user);
    // PostgreSQL unique key waits for concurrent creation to commit, then locks its result.
    keys.reserve(UUID.randomUUID(), user, key, hash);
    GroupCreationKey operation = keys.findByUserIdAndKey(user, key).orElseThrow();
    if (!operation.requestHash.equals(hash))
      throw new GroupProblem(409, "IDEMPOTENCY_CONFLICT", "Key was used for a different request");
    if (operation.conversationId != null) return created(operation.conversationId, user);
    budget.consume(user);
    unique.forEach(invitee -> directory.requireEligibleInvitation(user, invitee));
    Conversation group = conversations.save(new Conversation(name));
    ConversationMember owner = members.save(new ConversationMember(group.id, user, "OWNER"));
    List<GroupInvitation> pending =
        unique.stream().sorted().map(id -> new GroupInvitation(group.id, user, id)).toList();
    invitations.saveAll(pending);
    operation.conversationId = group.id;
    keys.save(operation);
    return new Created(
        new Summary(GroupMapper.conversation(group), 1, "OWNER"),
        GroupMapper.member(owner),
        pending.stream().map(GroupMapper::invitation).toList());
  }

  private Created created(UUID id, UUID user) {
    // Creation batches are bounded; later detail pages are always paginated.
    return new Created(
        detail(user, id),
        GroupMapper.member(members.findByConversationIdAndUserId(id, user).orElseThrow()),
        invitations
            .findByConversationId(id, PageRequest.of(0, 100, Sort.by("createdAt", "id")))
            .stream()
            .map(GroupMapper::invitation)
            .toList());
  }

  public Page<Summary> list(UUID user, int page, int size) {
    var slice = conversations.mine(user, paging(page, size));
    return new Page<>(
        slice.stream()
            .map(
                c ->
                    new Summary(
                        GroupMapper.conversation(c),
                        members.countByConversationIdAndStatus(c.id, "ACTIVE"),
                        members.findByConversationIdAndUserId(c.id, user).orElseThrow().role))
            .toList(),
        slice.hasNext(),
        page);
  }

  public Summary detail(UUID user, UUID group) {
    requireMember(user, group);
    Conversation c =
        conversations
            .findById(group)
            .filter(g -> "GROUP".equals(g.type))
            .orElseThrow(() -> new GroupProblem(404, "NOT_FOUND", "Group not found"));
    return new Summary(
        GroupMapper.conversation(c),
        members.countByConversationIdAndStatus(group, "ACTIVE"),
        members.findByConversationIdAndUserId(group, user).orElseThrow().role);
  }

  public Page<GroupMapper.MemberView> members(UUID user, UUID group, int page, int size) {
    detail(user, group);
    paging(page, size);
    return GroupContracts.page(
        members
            .findByConversationIdAndStatus(
                group, "ACTIVE", PageRequest.of(page, size, Sort.by("joinedAt", "id")))
            .map(GroupMapper::member),
        page);
  }

  public Page<GroupMapper.InvitationView> outgoing(UUID user, UUID group, int page, int size) {
    detail(user, group);
    if (!"OWNER".equals(members.findByConversationIdAndUserId(group, user).orElseThrow().role))
      throw new GroupProblem(403, "FORBIDDEN", "Only the owner can inspect invitations");
    return GroupContracts.page(
        invitations.findByConversationId(group, paging(page, size)).map(GroupMapper::invitation),
        page);
  }

  public Page<GroupMapper.InvitationView> incoming(UUID user, String status, int page, int size) {
    if (!Set.of("PENDING", "ACCEPTED", "REJECTED", "CANCELLED").contains(status))
      throw new GroupProblem(400, "INVALID_STATUS", "Invalid invitation status");
    return GroupContracts.page(
        invitations
            .findByInviteeUserIdAndStatus(user, status, paging(page, size))
            .map(GroupMapper::invitation),
        page);
  }

  @Transactional
  public List<GroupMapper.InvitationView> invite(UUID user, UUID group, Invite request) {
    detail(user, group);
    conversations
        .lock(group)
        .orElseThrow(() -> new GroupProblem(404, "NOT_FOUND", "Group not found"));
    if (!"OWNER".equals(members.findByConversationIdAndUserId(group, user).orElseThrow().role))
      throw new GroupProblem(403, "FORBIDDEN", "Only the owner can invite in GROUP-01");
    var ids = request.inviteeUserIds();
    if (ids == null || ids.isEmpty() || ids.size() > 100 || ids.stream().anyMatch(Objects::isNull))
      throw new GroupProblem(400, "INVALID_INVITEES", "Use batches of 1–100 people");
    if (new HashSet<>(ids).size() != ids.size())
      throw new GroupProblem(422, "DUPLICATE_INVITEES", "Select each person once");
    budget.consume(user);
    for (UUID id : ids) {
      if (id.equals(user) || members.findByConversationIdAndUserId(group, id).isPresent())
        throw new GroupProblem(409, "MEMBERSHIP_EXISTS", "Person already has a membership");
      directory.requireEligibleInvitation(user, id);
      if (invitations.existsByConversationIdAndInviteeUserIdAndStatus(group, id, "PENDING"))
        throw new GroupProblem(409, "INVITATION_EXISTS", "Invitation is already pending");
    }
    return invitations
        .saveAll(ids.stream().map(id -> new GroupInvitation(group, user, id)).toList())
        .stream()
        .map(GroupMapper::invitation)
        .toList();
  }

  @Transactional
  public GroupMapper.InvitationView respond(UUID user, UUID id, boolean accept) {
    GroupInvitation i =
        invitations
            .lock(id)
            .orElseThrow(() -> new GroupProblem(404, "NOT_FOUND", "Invitation not found"));
    if (!i.inviteeUserId.equals(user))
      throw new GroupProblem(404, "NOT_FOUND", "Invitation not found");
    if (!"PENDING".equals(i.status))
      throw new GroupProblem(409, "INVITATION_PROCESSED", "Invitation is no longer pending");
    directory.requireExistingUser(user);
    budget.consume(user);
    if (accept) {
      directory.requireEligibleInvitation(i.inviterUserId, user);
      requireMember(i.inviterUserId, i.conversationId);
      if (members.findByConversationIdAndUserId(i.conversationId, user).isPresent())
        throw new GroupProblem(409, "MEMBERSHIP_EXISTS", "Membership already exists");
      members.save(new ConversationMember(i.conversationId, user, "MEMBER"));
    }
    i.status = accept ? "ACCEPTED" : "REJECTED";
    i.respondedAt = java.time.Instant.now();
    return GroupMapper.invitation(invitations.save(i));
  }

  private void requireMember(UUID user, UUID group) {
    if (!members.existsByConversationIdAndUserIdAndStatus(group, user, "ACTIVE"))
      throw new GroupProblem(404, "NOT_FOUND", "Group not found");
  }

  private Pageable paging(int page, int size) {
    if (page < 0 || page > 100000 || size < 1 || size > 100)
      throw new GroupProblem(400, "INVALID_PAGE", "Use page >= 0 and size 1–100");
    return PageRequest.of(page, size, Sort.by("createdAt", "id"));
  }

  private static String fingerprint(String name, Set<UUID> ids) {
    try {
      String value =
          name.length()
              + ":"
              + name
              + ":"
              + ids.stream()
                  .sorted()
                  .map(UUID::toString)
                  .collect(java.util.stream.Collectors.joining(","));
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
