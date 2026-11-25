# Phase 2A — Backend baseline preparation manifest
Ngày25/11/2026. **Preparation only; extraction NOT performed.** D01 one deployable/domain packages, D08 immutable V1/forward changes and D09 TV1 common security direction are authorized. No permission inferred to change shared schema or TV1 code.

## Verified source
PR6 start HEAD d2141e2560d8d6ba198942c944a69a963c133923. Backend remains byte-for-byte unchanged by Phase2A.
- backend/pom.xml: SpringBoot3.5.16/Java21; one Maven project, JPA/Validation/Resource Server/Flyway/Postgres/Testcontainers.
- application.yml: env datasource/password; Hibernate validate/open-in-view=false; multipart disabled.
- compose.yaml: one PostgreSQL16-alpine localhost5432, healthcheck, persistent named volume.
- NexaBackend bootstrap; conversation entities/repositories and Groups domain layers.
- GroupConfiguration currently contains global SecurityFilterChain/JwtDecoder; move requires TV1-owned implementation/review.
- One production V1; no additional migration files introduced.

V1 path: backend/src/main/resources/db/migration/V1__shared_conversations_and_group_invitations.sql.
Unchanged Git blob: `03e3057ee5c6bbaaf0696ed271767991c205eb8c`.
Unchanged SHA256: `BDE9DCD689CBC86D4966A6C0E1327950AE89D67440E6986C0B054BAB19018E60`.

## Migration state
Verified only: PostgreSQL16 Testcontainers in GroupPostgresTest; Maven verify25 PASS/0 skipped. Flyway log confirms validate1 migration and apply V1 on ephemeral test DB. No DATABASE_* /NEXA_AUTH_* configuration available in this execution environment. **Shared/production-like DB state cannot be verified.** Compose declaration alone does not establish whether its volume or any shared environment applied V1.

Do not edit/rename/delete/split V1; do not repair/clean/reset DB or create second backend. Shared migration changes remain a separate approved task after owners provide sanitized Flyway history/version/checksum evidence.

## Scoped extraction manifest for a later task
| Category | Candidate paths | Owner/review | Gate |
|---|---|---|---|
| Infrastructure baseline | backend/pom.xml; NexaBackend.java; application.yml; compose.yaml; .env.example/.gitignore | TV3, TV1 security/deps +TV2 runtime review | File/hunk manifest approved; no embedded secrets |
| Infra documentation/tests | New backend runbook and dedicated bootstrap/datasource/Flyway smoke tests | TV3 | Tests independent of GroupService; test-only migration location if production schema intentionally deferred |
| Shared identity/security | Current global beans in groups/GroupConfiguration.java; users identity/provisioning policy | TV1 implementation; TV2/TV3 review | No TV3 move or permitAll here; trusted UUID/auth scheme decided by TV1 |
| Conversation/Member schema | V1 shared tables, conversation entity/repo package | TV2 Conversation +TV3 membership +TV1 users review | One registry; preserve V1 ancestry; approved forward migration and compatibility/backfill tests |
| Groups domain stays separate | GroupService/Controller/Contracts/Mapper; Invitation/CreationKey/WriteBudget/Directory and tests | TV3 | Never copy whole PR6 to get bootstrap |
| Global errors/request infrastructure | GroupErrors/current request limits vscommon error conventions | TV1 security +TV2 wire conventions; TV3 coordinates | Affected domain contracts approved; no broad advice/filter regression |

Future PR ordering: infrastructure-only bootstrap →approved shared schema/security lineage →Groups follow-up, with one runtime/history. Keep mixed historical V1 in canonical lineage under preserved strategy rather than falsely relabeling Groups tables as generic infrastructure. No automatic rebase/merge-main needed in Phase2A.

Required owner packet: environment aliases/owners/app SHA +sanitized flyway_schema_history and schema/checksum facts; public ports and schema review; fresh/upgrade/restart/restore tests and seed2users/1group deliverable. This manifest does not assert a baseline PR exists or has been approved/merged.
