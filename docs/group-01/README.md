# GROUP-01 Group creation and invitations

## Status and scope

This branch implements group persistence, invitation acceptance/rejection, authorization checks, pagination and Android feature screens.
It is not a completed production end-to-end feature: main now has Auth UI preview and Home foundation, but no TV1 production authentication/directory implementation or TV2 chat module.
There is no production fake data or authentication bypass. Missing identity is rejected; missing directory integration returns 503.
The PR should remain draft until the adapters and shared contracts have been reviewed.

GROUP-01 implements invitation-based membership, not automatic addition. The creator is OWNER/ACTIVE; invitees are PENDING invitations only.
Acceptance creates MEMBER/ACTIVE and changes the invitation to ACCEPTED in one transaction. Rejection creates no membership.
ADMIN management, leaving/rejoining, history policy, messaging, realtime events, media and translation are outside this change.

## Architecture

- Android: Java/XML, GroupsActivity as feature navigation/rendering host, GroupsViewModel with LiveData and SavedStateHandle, repository interfaces, Retrofit adapter, RecyclerView ListAdapter.
- Backend: Java 21, Spring Boot 3.5.16, Maven, Spring MVC, JPA, Bean Validation, Flyway, PostgreSQL 16. Java 21 was found locally. The 3.5 maintenance line avoids a framework-major migration for the team and supports Java 21 ([Spring requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)).
- Controller derives UUID identity from the validated JWT subject. Service owns business rules and transactions; repositories own persistence; GroupMapper projects database entities to public DTOs.
- JWT requires trusted issuer and expected audience. With no issuer configured, the decoder rejects every token. TV1 may replace the decoder with its verified authentication integration; no JWT is issued by this feature.
- GroupDirectory is a TV1 integration port. Its default implementation rejects operations with DIRECTORY_UNAVAILABLE. It must check user existence, discoverability and blocking in both directions, including at acceptance time.

## Schema and transaction rules

`V1__shared_conversations_and_group_invitations.sql` is the first migration in this previously backend-free repository.

| Table | Responsibility / constraints |
|---|---|
| users | Shared identity table containing UUID PK only; TV1 provisions users and extends it in new migrations |
| conversations | Shared DIRECT/GROUP aggregate; group creation uses GROUP |
| conversation_members | Unique conversation/user; ACTIVE/LEFT/REMOVED, OWNER/ADMIN/MEMBER; at most one active owner |
| group_invitations | FK conversation/inviter/invitee; PENDING/ACCEPTED/REJECTED/CANCELLED; partial unique index for pending group/invitee |
| group_creation_keys | Unique authenticated user/key, request hash and resulting conversation |
| group_write_budgets | Transactional shared per-user write budget across backend instances |

Creation atomically reserves/locks the idempotency key, validates eligibility, inserts conversation, owner and invitations, and stores the result ID.
Concurrent requests with the same user/key wait for the first transaction and return the same conversation. Different payload with that key returns 409.
Names are stripped; invitee order is canonicalized for hashing. The replay response reflects the current group and first invitation page, not a historical immutable response snapshot.
Use GET detail/invitation pages for subsequent state. Keys are durable for this release; a future retention job must preserve the promised retry period.

Acceptance pessimistically locks the invitation, checks its recipient and PENDING state, revalidates policy and atomically inserts the membership and response.
Another acceptance/rejection after processing returns 409. Database uniqueness additionally prevents duplicate membership/pending invitations.

No maximum group size is defined. Creation and additional invitations accept at most 100 invitees per request. Owners may send further batches via the API.
List sizes are 1–100 (default 30); Android replaces the current page instead of retaining all pages in memory.
JSON POST bodies are bounded to 64 KiB, including chunked input. Writes allow 30 successful operations per user per minute.
Failed transactions do not consume that write budget. HTTP-level rate limiting for abusive failed/read traffic remains a deployment gateway responsibility.

`last_read_message_id` is reserved for TV2; no FK to a nonexistent message table is created. TV2 must add the message relation in a later migration.
There is one conversation system and one user identity table, not separate group identities or contact tables.
Do not edit applied migrations. Use a new migration when integrating TV1/TV2 schemas.

## REST contract

All paths use /api/v1 and require Authorization: Bearer with a verified JWT whose subject is the current user's UUID.
Errors contain `{code,message,requestId}`. HTTP 400/401/403/404/409/413/422/429/500/503 are handled according to validation, policy and dependency state.
Unauthorized group/invitation IDs are concealed with 404.

| Method | Path | Result / permission |
|---|---|---|
| POST | /groups | Create group; Idempotency-Key required |
| GET | /groups?page=0&size=30 | Current user's ACTIVE groups only |
| GET | /groups/{id} | Summary; ACTIVE membership required |
| GET | /groups/{id}/members?page=0&size=30 | ACTIVE members; ACTIVE membership required |
| GET | /groups/{id}/invitations?page=0&size=30 | Invitation states; OWNER only |
| POST | /groups/{id}/invitations | Additional batch; OWNER only; duplicate pending is 409 |
| GET | /group-invitations?status=PENDING&page=0&size=30 | Authenticated recipient's invitations |
| POST | /group-invitations/{id}/accept | Recipient only; atomic membership activation |
| POST | /group-invitations/{id}/reject | Recipient only; no membership |

Creation request:

```http
POST /api/v1/groups
Authorization: Bearer <TV1-issued-token>
Idempotency-Key: 8bbf595e-87f2-4d85-a635-62a21cd02855
Content-Type: application/json

{"name":"NEXA Study","inviteeUserIds":["7a6f751c-e07f-4f76-8d6e-c36d1a5d88c8"]}
```

Creation returns 201 with `group: {conversation,memberCount}`, `creatorMembership` and `invitations`.
A group with no invitees is valid. Repeated IDs or the creator as invitee are rejected.
Key syntax: 16–128 ASCII letters, digits, underscores or hyphens. Keys are scoped to authenticated user.
Pages use `{items,hasNext,page}`, ordered by creation timestamp + UUID; member pages use joinedAt + UUID.
Page-based pagination is not a snapshot under concurrent writes; clients can refresh page zero.
Additional invitation JSON: `{"inviteeUserIds":["<UUID>"]}`. Accept/reject take no body.

## TV1 Android integration

Provide a real `PeopleDirectory` adapter:

- `currentUserId()`: current authenticated user UUID; return null without a valid session.
- `friends(page,callback)`: existing contacts module, paginated, public display name/avatar/identifier only.
- `searchPhone(phone,page,callback)`: existing user search flow; TV1 normalizes E.164, handles country selection, rate limiting and privacy.
- `Person.eligible`: UI hint only. Server always rechecks policy. Do not return private phone/email merely to fill the identifier.
- Callbacks must run on Android's main thread, as Retrofit's Android callback executor does.

Construct `RemoteGroupRepository` using `authenticatedRetrofit.create(GroupApi.class)`.
The shared Retrofit base URL must end with / and represent the server root; GroupApi includes api/v1.
Use HTTPS and TV1's token refresh/revoke logic. No tokens or server URL are hardcoded by the group feature.

Install `GroupsIntegration.Dependencies(repository,peopleDirectory,chatNavigator,scope)` from the Application composition root.
Scope requires a validated user UUID, local session epoch and latest-session access supplier supplied by TV1.
Call `GroupsIntegration.open(activity)` from home_panel_groups after authentication; shared Auth/Home sources are retained from main.
Calling install(null) or replacing dependencies invalidates the old scope, cancels owned requests, clears account state and finishes old Groups screens.
Refresh in the same epoch suspends protected UI while validation is unavailable and retains draft/retry state; it does not invalidate the scope.
Reinstall real adapters after process recreation before opening the activity. SavedStateHandle restores drafts/keys only for the same user and epoch.
See [Session V2 integration](session-v2-integration.md) for the required latest-snapshot mapping, cancellation and coordinator responsibilities.

## TV1 backend integration

- Implement GroupDirectory as a bean backed by authoritative users/privacy/block policy, preferably participating in the same database transaction.
- Require a real existing user row for the caller and every invitee. Do not provision users from group requests.
- Configure NEXA_AUTH_ISSUER and NEXA_AUTH_AUDIENCE or integrate TV1's verified JwtDecoder and shared security configuration.
- Agree on the initial users identity table and evolve it through additional migrations. No phone/profile search endpoints were invented here.
- Review the shared Gradle additions, manifest entry/Internet permission, dedicated feature theme and proposed Home mount. No skeleton launch button remains.

## TV2 integration

- Reuse conversation IDs and the shared conversations/conversation_members schema for group messaging.
- Supply ChatNavigator.open(activity,conversationId). Until then the UI reports that chat is unavailable.
- Enforce ACTIVE membership for REST reads/writes and WebSocket subscription/delivery; pending invitees must not read group history.
- Membership events/history/rejoin policy need a later agreed contract. This change does not claim realtime invitation notifications.

## Running locally

1. Install Java 21 and Maven, and run Docker.
2. Set DATABASE_PASSWORD locally (never commit it). In backend/, run `docker compose up -d postgres`.
3. Set DATABASE_URL, DATABASE_USER and DATABASE_PASSWORD for Spring; defaults for URL/user are localhost:5432/nexa and nexa.
4. Install the TV1 GroupDirectory adapter and configure trusted authentication.
5. Run `mvn spring-boot:run`. Application starts without a directory adapter, but group writes return 503; missing issuer rejects tokens.

The .env.example lists variable names only. Docker Compose reads .env; Spring Boot requires process environment variables and does not automatically load .env.
For backend automated verification use `mvn verify`; tests start an isolated PostgreSQL Testcontainer with ephemeral credentials and test-only adapters.
Without Docker the PostgreSQL test class is explicitly skipped; inspect skipped counts. Unit-only command: `mvn -Dtest=GroupServiceTest test`.

Android: install SDK 37 and the wrapper's requested JDK 25 toolchain; JAVA_HOME 21 can launch the wrapper, which resolves its configured daemon toolchain.
Run `gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.
For UI checks boot an emulator, then run `gradlew.bat :app:connectedDebugAndroidTest`.
UI fixtures exist only under androidTest and unit tests. They are not production search/auth.
