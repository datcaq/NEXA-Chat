package com.nexa.chat.backend.groups;

import static org.junit.jupiter.api.Assertions.*;

import com.nexa.chat.backend.conversation.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class GroupPostgresTest {
  @Container static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", db::getJdbcUrl);
    r.add("spring.datasource.username", db::getUsername);
    r.add("spring.datasource.password", db::getPassword);
  }

  @TestConfiguration
  static class Adapters {
    @Bean
    @Primary
    GroupDirectory directory(JdbcTemplate jdbc) {
      return new GroupDirectory() {
        public void requireExistingUser(UUID u) {
          if (jdbc.queryForObject("select count(*) from users where id=?", Integer.class, u) == 0)
            throw new GroupProblem(422, "INELIGIBLE", "Unknown user");
        }

        public void requireEligibleInvitation(UUID a, UUID b) {
          requireExistingUser(a);
          requireExistingUser(b);
        }
      };
    }

    // Test-only decoder. No such token format exists in production.
    @Bean
    @Primary
    JwtDecoder decoder() {
      return token ->
          Jwt.withTokenValue(token)
              .header("alg", "test")
              .subject(token)
              .issuedAt(Instant.now())
              .expiresAt(Instant.now().plusSeconds(60))
              .build();
    }
  }

  @Autowired GroupService service;
  @Autowired JdbcTemplate jdbc;
  @Autowired TestRestTemplate http;
  UUID owner, invitee;

  @BeforeEach
  void seed() {
    jdbc.execute("TRUNCATE conversations,users CASCADE");
    owner = UUID.randomUUID();
    invitee = UUID.randomUUID();
    jdbc.update("insert into users(id) values (?),(?)", owner, invitee);
  }

  GroupContracts.Created create() {
    return service.create(
        owner, "integration-key-001", new GroupContracts.Create("Study", List.of(invitee)));
  }

  @Test
  void persistsAndRetryIsIdempotent() {
    var first = create();
    var second = create();
    assertEquals(first.group().conversation().id, second.group().conversation().id);
    assertEquals(1, jdbc.queryForObject("select count(*) from conversations", Integer.class));
    assertEquals(
        1, jdbc.queryForObject("select count(*) from conversation_members", Integer.class));
    assertEquals(1, service.list(owner, 0, 1).items().size());
    assertTrue(service.list(invitee, 0, 1).items().isEmpty());
  }

  @Test
  void acceptAndRejectPersistAtomically() {
    var created = create();
    UUID id = created.invitations().getFirst().id;
    service.respond(invitee, id, true);
    assertEquals(2, service.detail(invitee, created.group().conversation().id).memberCount());
    assertThrows(GroupProblem.class, () -> service.respond(invitee, id, true));
  }

  @Test
  void invalidInviteeRollsBackKeyAndConversation() {
    assertThrows(
        GroupProblem.class,
        () ->
            service.create(
                owner,
                "rollback-test-key",
                new GroupContracts.Create("Study", List.of(UUID.randomUUID()))));
    assertEquals(0, jdbc.queryForObject("select count(*) from group_creation_keys", Integer.class));
    assertEquals(0, jdbc.queryForObject("select count(*) from conversations", Integer.class));
  }

  @Test
  void duplicatePendingConstraint() {
    var i = create().invitations().getFirst();
    assertThrows(
        org.springframework.dao.DataIntegrityViolationException.class,
        () ->
            jdbc.update(
                "insert into"
                    + " group_invitations(id,conversation_id,inviter_user_id,invitee_user_id,status,created_at)"
                    + " values (?,?,?,?,'PENDING',now())",
                UUID.randomUUID(),
                i.conversationId,
                owner,
                invitee));
  }

  @Test
  void memberInsertFailureRollsBackAcceptance() {
    var i = create().invitations().getFirst();
    jdbc.execute(
        "CREATE FUNCTION fail_member() RETURNS trigger AS $$ BEGIN IF NEW.role='MEMBER' THEN RAISE"
            + " EXCEPTION 'test failure'; END IF; RETURN NEW; END; $$ LANGUAGE plpgsql");
    jdbc.execute(
        "CREATE TRIGGER test_fail BEFORE INSERT ON conversation_members FOR EACH ROW EXECUTE"
            + " FUNCTION fail_member()");
    try {
      assertThrows(RuntimeException.class, () -> service.respond(invitee, i.id, true));
      assertEquals(
          "PENDING",
          jdbc.queryForObject(
              "select status from group_invitations where id=?", String.class, i.id));
      assertEquals(
          1, jdbc.queryForObject("select count(*) from conversation_members", Integer.class));
    } finally {
      jdbc.execute("DROP TRIGGER test_fail ON conversation_members");
      jdbc.execute("DROP FUNCTION fail_member()");
    }
  }

  @Test
  void restAuthPaginationAndValidation() {
    assertEquals(401, http.getForEntity("/api/v1/groups", String.class).getStatusCode().value());
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(owner.toString());
    headers.set("Idempotency-Key", "rest-create-key-001");
    var result =
        http.exchange(
            "/api/v1/groups",
            HttpMethod.POST,
            new HttpEntity<>(new GroupContracts.Create("REST", List.of(invitee)), headers),
            GroupContracts.Created.class);
    assertEquals(201, result.getStatusCode().value());
    assertEquals(
        200,
        http.exchange(
                "/api/v1/groups?size=1", HttpMethod.GET, new HttpEntity<>(headers), String.class)
            .getStatusCode()
            .value());
    headers.setBearerAuth(invitee.toString());
    assertEquals(
        404,
        http.exchange(
                "/api/v1/groups/" + result.getBody().group().conversation().id,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class)
            .getStatusCode()
            .value());
  }

  @Test
  void concurrentCreationWithSameKeyCreatesOneGroup() throws Exception {
    try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
      var gate = new java.util.concurrent.CountDownLatch(1);
      java.util.concurrent.Callable<UUID> task =
          () -> {
            gate.await();
            return create().group().conversation().id;
          };
      var a = pool.submit(task);
      var b = pool.submit(task);
      gate.countDown();
      assertEquals(
          a.get(15, java.util.concurrent.TimeUnit.SECONDS),
          b.get(15, java.util.concurrent.TimeUnit.SECONDS));
      assertEquals(1, jdbc.queryForObject("select count(*) from conversations", Integer.class));
    }
  }

  @Test
  void concurrentAcceptanceCreatesOneMembership() throws Exception {
    UUID id = create().invitations().getFirst().id;
    try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
      var gate = new java.util.concurrent.CountDownLatch(1);
      java.util.concurrent.Callable<String> task =
          () -> {
            gate.await();
            try {
              return service.respond(invitee, id, true).status;
            } catch (GroupProblem e) {
              return e.code;
            }
          };
      var a = pool.submit(task);
      var b = pool.submit(task);
      gate.countDown();
      assertEquals(
          Set.of("ACCEPTED", "INVITATION_PROCESSED"),
          Set.of(
              a.get(15, java.util.concurrent.TimeUnit.SECONDS),
              b.get(15, java.util.concurrent.TimeUnit.SECONDS)));
      assertEquals(
          2, jdbc.queryForObject("select count(*) from conversation_members", Integer.class));
    }
  }

  @Test
  void moreThanOneBatchWithoutGroupMemberCap() {
    var group = create().group().conversation().id;
    List<UUID> people = new ArrayList<>();
    for (int n = 0; n < 101; n++) {
      UUID id = UUID.randomUUID();
      jdbc.update("insert into users(id) values (?)", id);
      people.add(id);
    }
    service.invite(owner, group, new GroupContracts.Invite(people.subList(0, 100)));
    service.invite(owner, group, new GroupContracts.Invite(people.subList(100, 101)));
    assertEquals(102, jdbc.queryForObject("select count(*) from group_invitations", Integer.class));
    assertTrue(service.outgoing(owner, group, 0, 30).hasNext());
    assertEquals(12, service.outgoing(owner, group, 3, 30).items().size());
    assertThrows(
        GroupProblem.class,
        () -> service.invite(owner, group, new GroupContracts.Invite(List.of(invitee))));
  }

  @Test
  void rejectionPersistsWithoutMembership() {
    var i = create().invitations().getFirst();
    service.respond(invitee, i.id, false);
    assertEquals(
        "REJECTED",
        jdbc.queryForObject("select status from group_invitations where id=?", String.class, i.id));
    assertEquals(
        1, jdbc.queryForObject("select count(*) from conversation_members", Integer.class));
  }

  @Test
  void onlyOwnerCanInspectAndSendInvitations() {
    var created = create();
    var group = created.group().conversation().id;
    service.respond(invitee, created.invitations().getFirst().id, true);
    assertEquals(
        403,
        assertThrows(GroupProblem.class, () -> service.outgoing(invitee, group, 0, 30)).status);
    assertEquals(
        403,
        assertThrows(
                GroupProblem.class,
                () -> service.invite(invitee, group, new GroupContracts.Invite(List.of(owner))))
            .status);
  }

  @Test
  void rateLimitIsTransactionalAndShared() {
    for (int n = 0; n < 30; n++)
      service.create(owner, "rate-limit-test-" + n, new GroupContracts.Create("Budget", List.of()));
    assertEquals(
        429,
        assertThrows(
                GroupProblem.class,
                () ->
                    service.create(
                        owner,
                        "rate-limit-test-31",
                        new GroupContracts.Create("Budget", List.of())))
            .status);
    assertEquals(30, jdbc.queryForObject("select count(*) from conversations", Integer.class));
  }

  @Test
  void oversizedJsonBodyRejected() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(owner.toString());
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Idempotency-Key", "oversized-body-key");
    assertEquals(
        413,
        http.exchange(
                "/api/v1/groups",
                HttpMethod.POST,
                new HttpEntity<>("{\"name\":\"" + "x".repeat(70000) + "\"}", headers),
                String.class)
            .getStatusCode()
            .value());
  }
}
