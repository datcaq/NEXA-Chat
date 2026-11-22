package com.nexa.chat.backend.groups;

import static com.nexa.chat.backend.groups.GroupContracts.*;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class GroupController {
  private final GroupService service;

  public GroupController(GroupService service) {
    this.service = service;
  }

  private UUID user(Jwt jwt) {
    try {
      return UUID.fromString(jwt.getSubject());
    } catch (RuntimeException e) {
      throw new GroupProblem(401, "UNAUTHENTICATED", "Invalid user identity");
    }
  }

  @PostMapping("/groups")
  public ResponseEntity<Created> create(
      @AuthenticationPrincipal Jwt jwt,
      @RequestHeader("Idempotency-Key") String key,
      @Valid @RequestBody Create request) {
    Created created = service.create(user(jwt), key, request);
    return ResponseEntity.created(
            java.net.URI.create("/api/v1/groups/" + created.group().conversation().id))
        .body(created);
  }

  @GetMapping("/groups")
  public Page<Summary> list(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "30") int size) {
    return service.list(user(jwt), page, size);
  }

  @GetMapping("/groups/{id}")
  public Summary detail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    return service.detail(user(jwt), id);
  }

  @GetMapping("/groups/{id}/members")
  public Page<GroupMapper.MemberView> members(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "30") int size) {
    return service.members(user(jwt), id, page, size);
  }

  @PostMapping("/groups/{id}/invitations")
  public java.util.List<GroupMapper.InvitationView> invite(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody Invite request) {
    return service.invite(user(jwt), id, request);
  }

  @GetMapping("/groups/{id}/invitations")
  public Page<GroupMapper.InvitationView> outgoing(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "30") int size) {
    return service.outgoing(user(jwt), id, page, size);
  }

  @GetMapping("/group-invitations")
  public Page<GroupMapper.InvitationView> incoming(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(defaultValue = "PENDING") String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "30") int size) {
    return service.incoming(user(jwt), status, page, size);
  }

  @PostMapping("/group-invitations/{id}/accept")
  public GroupMapper.InvitationView accept(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    return service.respond(user(jwt), id, true);
  }

  @PostMapping("/group-invitations/{id}/reject")
  public GroupMapper.InvitationView reject(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    return service.respond(user(jwt), id, false);
  }
}
