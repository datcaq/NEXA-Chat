# Flyway verification plan

26/11/2026. Preparation only; no database connection, migration execution, clean/repair/reset or migration file modification in this task. [Audit/source S10/S16/S18/S19](FOUNDATION_AUDIT.md), [manifest](BACKEND_FOUNDATION_PREPARATION.md), [D08 authority](DECISION_REGISTER.md).

## 1. Verified V1 integrity

Path: backend/src/main/resources/db/migration/V1__shared_conversations_and_group_invitations.sql.

- Current #6 commit: `2e804c033631049683cffd1d7500bb7406658bfa`.
- Historical inspected commit: `d2141e2560d8d6ba198942c944a69a963c133923`.
- Both resolve to Git blob **03e3057ee5c6bbaaf0696ed271767991c205eb8c**.
- SHA-256 of raw Git blob bytes at both refs: **BDE9DCD689CBC86D4966A6C0E1327950AE89D67440E6986C0B054BAB19018E60**.
- Computed using git rev-parse ref:path then git cat-file blob and Python hashlib.sha256 on bytes, avoiding PowerShell text/EOL conversion. Both match the historical user record. Main has no backend/V1; do not claim main applied it.

Name has version V1, double underscore, SQL extension and description. One production migration exists in the candidate tree. Content: users UUID skeleton; conversations DIRECT/GROUP; conversation_members role/status/timestamps/UUID last_read_message_id and unique pair; partial at-most-one active OWNER; group_invitations statuses and response consistency/pending uniqueness/indexes; group_creation_keys user/key/hash; transactional group_write_budgets. No Message, periods, delivered cursor, expiry or canonical DIRECT pair. The owner index does not ensure at least one OWNER or define DIRECT roles.

backend/pom.xml includes Flyway core+PostgreSQL module and JDBC driver. application.yml sets Hibernate validate/open-in-view=false, external password, no custom Flyway location/history/baseline settings; candidate uses Boot integration and default migration discovery. No second history is proposed. SHA-256/Git blob IDs are artifact integrity identifiers; they are **not Flyway's recorded SQL checksum**.

## 2. Schema history evidence requirements

For every authorized target, record sanitized environment alias/owner, app commit/artifact, PostgreSQL/Flyway versions, configured schema/search path and migration locations/history table. Obtain installed_rank/version/description/type/script/checksum/success and validation output, with timestamps redacted only where needed. Compare Flyway checksum using the actual configured Flyway version, not SHA-256. Unexpected scripts, failed records, checksum drift, missing history with existing tables or manually changed schema blocks migration pending investigation. Do not repair to silence it.

## 3. Environment identification

| Environment | Evidence actually available | Status / next evidence |
| --- | --- | --- |
| Isolated PostgreSQL16 Testcontainers | S16 creates container; external25Nov log validates1/appliesV1;13 integration tests0 skipped | VERIFIED historical ephemeral test context only; test-only decoder/directory |
| Local compose volume | Declarative postgres16-alpine, loopback5432 and named persistent volume | UNKNOWN actual existence/schema/history; do not infer unused from declaration |
| Shared/demo/staging/production | No authorized connection/history packet provided | UNKNOWN; explicit target-owner authorization before read/connect or execution |

No environment variables, credentials, .env contents or database URLs with credentials were printed. The sanitized template is an evidence request, not a statement that any target is known clean.

## 4–7. Verification matrix for separately authorized work

| Stage | Setup and actions | Required evidence / acceptance | Current status |
| --- | --- | --- | --- |
| Fresh installation | New isolated database, exact approved runtime/config, default canonical migrations; start app and validate entities | Complete startup/migrate/validate logs, expected tables/constraints/indexes/history,0 skipped tests; no fake prod identity | Historical V1 migrate passes in Groups integration test; standalone infra acceptance NOT RUN |
| Existing database upgrade | Authorized clone or restored sanitized backup with exact original V1 and representative users/group/invites/keys; apply only approved forward migration when one exists | Original V1 checksum/history preserved, row counts/FKs/unique invariants/backfill bounds, old/new client compatibility, lock/duration metrics | NOT RUN; no V2 authored; target state UNKNOWN |
| Restart | Restart same isolated app/database after migrate and after representative writes | No reapply/duplicate seed/history, validate success, durable data and readiness recover | NOT RUN as dedicated acceptance |
| Connection failure | Isolated unreachable/denied DB and restore availability | Bounded startup failure, readiness unavailable, sanitized diagnostics; no reset/auto-create or fallback history | NOT RUN |
| Backup/restore | Authorized disposable restore rehearsal using owner's backup procedure; verify backup checksum/storage/access | Document RPO/RTO, restore command/log, schema+history+representative rows/invariants, boot+validate; retain recovery artifact and decision | NOT RUN; no shared backup touched |

Fresh isolated databases may be provisioned later; do not use a command such as Flyway clean to make an existing target look fresh. Existing test source contains truncate/drop of test fixtures inside disposable container scope; never copy those actions into shared verification.

## 8–10. Ownership and forward migration process

TV3 coordinates single version registry/history; TV1 owns users/auth/session semantics, TV2 Conversation/Message/sequence, TV3 membership/invitations. Shared SQL needs affected owners' exact-head approval plus target operator authorization. D08 full ADR remains insufficiently established; today's immutable-V1 constraint remains mandatory.

Future process: inventory target → reconcile actual history and schema → publish reviewed forward-only proposal with compatibility/backfill/locking/rollback-vs-restore plan → allocate one version atomically → pass fresh/populated upgrade/restart/failure/restore on isolated target → obtain owner/environment approval → controlled rollout with validation and stop criteria. No editing/renaming/deleting/splitting V1, no new V2 in this milestone, no second application/history, no unapproved baseline-on-migrate or repair.

Before production migration require exact SQL/app artifacts and hashes, complete owner approval ledger, environment identity and sanitized history, tested backups/restores, access credentials via secret injection, deployment window/operator, measured lock/backfill duration, monitoring/abort plan and post-run history/constraint/data checks. None is implied by unit tests or an open PR.
