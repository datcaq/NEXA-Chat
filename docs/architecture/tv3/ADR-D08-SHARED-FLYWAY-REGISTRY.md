# ADR D-08 — Shared Flyway registry and transaction coordination

26/11/2026, Asia/Saigon. **PROPOSED — AWAITING OWNER APPROVAL.**

Full ADR adoption, physical transaction strategy and every cross-domain schema choice: **PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION.** TV1/TV2 alignment does not replace Phúc or affected-owner signatures. D-08 is the architecture ADR identifier; Messaging D08 is the related decision in the unchanged FIX3 register.

## Context and evidence

Three modules need one backend database evolution path, but registry administration, domain policy and environment execution are different responsibilities. Source inspected at Groups `2e804c033631049683cffd1d7500bb7406658bfa` contains one Spring Boot/PostgreSQL/Flyway foundation, separate GroupInvitation, stable member-pair uniqueness and initial atomic accept. It has no membership periods, message allocator, receipt ACL, shared authority fence or transactional publication architecture.

[GroupService](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupService.java) currently locks the invitation first in respond; invite locks the conversation. This is not evidence of a universal lock order. [Postgres tests](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/test/java/com/nexa/chat/backend/groups/GroupPostgresTest.java) cover concurrent initial accept and insert-failure rollback, not join/send/remove/revoke integration. The test directory adapter checks existence only; its JWT decoder is explicitly test-only. Historical backend evidence records 25 tests (12 service + 13 PostgreSQL), not a new execution in this task.

Evidence pins, source hashes and availability limits are in the [owner adoption ledger](TV3_CONTRACT_01E2B_OWNER_ADOPTION_DRAFT.md). No shared/production database was contacted. Actual schema/history/configuration and deployment lineage are **UNKNOWN**.

## Proposed decision and authority matrix

Use one shared migration registry and one approved Flyway history for the shared database. TV3 administers inventory, proposed reservations, ordering, checksum records, CI/readiness documentation and owner routing. **No version is allocated by this task.**

| Responsibility | Owner / authority | Approval boundary |
| --- | --- | --- |
| Auth/User/Friend/Security policy and migrations | TV1 | TV1 controls domain; other affected owners review cross-domain effects |
| Conversation/Message/Sequence | TV2 | TV2 controls domain; Groups shared Conversation/member effects require TV3 concurrence |
| Groups/Membership/GroupInvitation | TV3 | Phúc controls domain; TV1 privacy and TV2 message/sequence effects reviewed |
| Backend foundation / registry administration | TV3 coordinator | Coordinates, documents, detects collision; cannot approve TV1/TV2 schema unilaterally |
| Cross-domain migration | Named author + every affected domain owner | Explicit scoped approvals and accepted evidence before implementation/rollout |
| Shared transaction contract | TV1/TV2/TV3 | All participating write/read/revoke paths must implement one approved protocol |
| Deployment / restore / database operation | Named authorized environment custodian | Registry coordination grants no production deployment authority; custodian currently unrecorded |

Conversation ownership does not make TV2 sole owner of GROUP history; foundation coordination does not transfer Auth or Message ownership to TV3.

Alternatives: independent histories per module risk ordering/checksum divergence in one shared schema; single unilateral schema owner erases domain accountability; unmanaged per-branch numbering causes collisions. A shared registry with affected-owner review adds coordination cost but keeps one verifiable lineage. Isolated databases would require an explicitly redesigned distributed consistency model; no such split is selected here.

## Migration inventory and lifecycle

| Inventory item | Recorded fact |
| --- | --- |
| Existing script | `backend/src/main/resources/db/migration/V1__shared_conversations_and_group_invitations.sql` at Groups pin |
| Git blob | `03e3057ee5c6bbaaf0696ed271767991c205eb8c` |
| Raw SHA256 | `BDE9DCD689CBC86D4966A6C0E1327950AE89D67440E6986C0B054BAB19018E60` (recomputed from Git bytes) |
| Existing shape | Six foundation/group tables; unique conversation/user membership; partial pending-invite uniqueness; no period/message/replay schema |
| Installed version / Flyway checksum / target lineage | UNKNOWN; raw SHA256 is not Flyway's installed checksum |
| New migration version / script | NONE proposed or reserved here; no V2 created |

Preserve V1 bytes. Never edit a possibly installed script to reconcile branches. Do not clean, repair, baseline, reset or introduce a second history to conceal divergence. Keep the default shared-history configuration unless owners and custodian explicitly approve a different single-history location; current target location is unknown.

Proposed workflow: domain author submits dependency/compatibility plan → coordinator inventories candidate after current remote refresh → affected owners approve shape/order/backfill → coordinator records a future reservation under an agreed allocation rule → CI validates → custodian evaluates target lineage/backup/readiness → authorized rollout → append actual evidence. These are future stages, not actions performed here.

Proposed registry fields: candidate ID, domain/author, affected owners, dependencies, future version once separately allocated, `V<version>__<descriptive_snake_case>.sql` name, commit/blob/raw digest, tool-produced installed checksum, compatibility/backfill/lock estimate, fresh/upgrade/restart/restore evidence, approvals with date/source and authorized environment operator. Allocate monotonically after checking merged and reserved versions; no guessed next number, branch-local collision or out-of-order setting used to bypass review. Changed candidate content invalidates its reviews/checksum inventory.

### CI and deployment readiness proposal

- Validate unique version/name/order and one migration location/history; compare V1 blob/raw hash and prior applied scripts; run Flyway validate against a disposable PostgreSQL matching the agreed tool/server versions. Record versions/config; successful Hibernate validation alone is insufficient.
- Fresh install: empty disposable DB, apply canonical chain, inspect schema/constraints/history/checksum/success, verify application start; no skipped container tests accepted as execution evidence.
- Populated upgrade: sanctioned snapshot with representative members/invitations and any later agreed messages; prove referential/uniqueness invariants, conservative legacy period mapping, bounded backfill/locks and old/new application compatibility. Empty-install success does not prove upgrade safety.
- Restart: stop/restart after success and after injected failure; verify no duplicate migration/events and no partial supported state. Check concurrent startup behavior and application compatibility before admitting traffic.
- Failure/restore: snapshot/backup and operator-run restore rehearsal in isolation; verify actual data/history after restore and restart. Forward fix versus restore is an explicit operational choice, never delete history rows or silently mutate V1. Some DDL/backfills may require a separately reviewed resumable plan.
- Target evidence, supplied later by custodian: sanitized environment identity; PostgreSQL/Flyway versions; schemas/search path/history location/config; installed_rank/version/script/checksum/success; drift/validation; backup/restore references; fresh/populated/restart/failure results; deployment/rollback authority. Do not include credentials or connect to obtain them in this task.

## Physical transaction decision — candidate A

**PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION.** Recommend evaluating a single-database, ordered-authority-lock protocol with READ COMMITTED and explicit pessimistic serialization. Candidate B is persisted generation/admission fencing with a proven cross-instance completion barrier; SERIALIZABLE with whole-transaction retry is another isolation alternative. These designs are alternatives, not an implemented hybrid. Eventual fanout alone cannot support immediate revocation claims.

### Candidate A's proposed order and completion boundary

Acquire the complete affected authority set in one deterministic order: (1) stable user/account authority roots sorted by canonical UUID, (2) social/block/consent pair roots sorted by canonical UUID pair, (3) idempotency/create-pair roots sorted by operation identity, (4) conversation roots sorted by UUID, (5) membership and invitation rows sorted by type then identity, (6) message counter/message/version rows, (7) receipt and attachment rows, (8) durable conversation/account-log and outbox append state sorted by stream owner. Exact row design/comparator and rank registry need all owners. This allocates no schema/version.

Do not take a low-ranked lock after a high-ranked lock. Read an invitation locator only as an untrusted hint to discover IDs, then lock authority roots/group/member/invitation in rank order and revalidate the locator/version. If the required lock set changes, rollback and restart from the complete set. Missing-row pair/admission serialization requires a persistent root/unique constraint strategy; a process-local mutex cannot cover multiple backend instances. Create/upsert conflicts must rollback before fresh reread where the transaction is aborted. No HTTP/provider calls, network event sends or long fanout while holding DB locks.

For group-wide sends, complete affected-user authority locking can be expensive. Either bound and lock the full affected set, with a retry when membership changes during set discovery, or select candidate B with a proven strong barrier. Existing batch100 is not a group-size cap. Do not claim candidate A is ready at unbounded roster size. A narrower sender-only lock does not prove account revocation for every recipient projection.

Writes, authority revisions, durable event facts and outbox commit atomically. A revoke that wins serialization prevents later protected admission. A prior send may commit before revoke; subsequent fetch/apply/publication must recheck current recipient authority under the selected strong admission boundary. A socket write already admitted/in flight cannot be recalled: define exactly whether revoke completion waits for admitted work or closes/contains it. This cross-instance final-response/publication barrier remains a required design/evidence gap, not a property conferred by DB row locks or queued cleanup. Same-process UUID/epoch callback fences protect Android apply/render separately.

### Thirteen-topic technical decision table

Each recommendation below is conditional on the common protocol above; none claims implementation. All acceptance evidence is **NOT RUN** in this documentation task.

| # / topic | Recommended approach | Alternatives | Trade-offs | Required evidence | Affected owner | Unresolved decision |
| --- | --- | --- | --- | --- | --- | --- |
| 1. DB transaction boundaries | One application command transaction covers current authority, domain mutation, idempotency result and durable facts; final disclosure separately gated | SERIALIZABLE command; distributed coordinator | Local atomicity simple, strong publication still needs barrier | Commit/rollback/final-admission schedules across instances | TV1/TV2/TV3 | Exact command and response linearization points |
| 2. Membership / sequence | Under same conversation authority lock, read finalized committed J, create fresh period at inclusive J+1; send assigns counter+message atomically | Persisted sequencer; explicit approved backfill interval | Per-group serialization cost; rolled-back reservations must not masquerade as J | Join/send both orders, rollback/gaps, J0/overflow | TV2 allocator + TV3 periods | Allocator design, empty interval wire, legacy mapping |
| 3. Invitation atomicity | Revalidate invitation/inviter/target under full order; consume invite + stable member/new period/floor/revisions + event facts in one commit | Explicit saga with no access until completion | Atomic DB path simpler, requires joint schema | Insert/period/outbox failure rollback; both-order conflicts | TV3, TV1 eligibility, TV2 publication | Rejoin/lifecycle selections in D25 |
| 4. Join/send/remove | Same group serialization point; remove closes period and advances authority; recheck final disclosure | Generation fence + completion barrier | Coarse contention versus complex distributed proof | Both orders plus account/block races on separate instances | All three | Full authority set and precise remove completion semantics |
| 5. Lock order | Stable complete rank order above; discovery hints revalidated | Persisted admission sequencer / SERIALIZABLE conflicts | Needs protocol compliance across modules | Rank inventory and instrumented opposing-command tests | All three | Rank/comparator/missing-root strategy |
| 6. Deadlock avoidance | Complete sorted sets; no external I/O; bounded waits, rollback on rank expansion; detect and retry classified deadlocks | Finer partitions; optimistic retries | Contention/throughput versus complexity | Deterministic deadlock injection, saturation and bounded resource proof | Foundation + each module | Lock timeout/capacity/batch policy, not chosen numeric values |
| 7. Isolation | READ COMMITTED + explicit authority/counter locks and DB constraints | SERIALIZABLE + whole-command retry; REPEATABLE READ with additional proof | Explicit-lock omissions dangerous; serializable adds abort cost | Phantom eligibility/absent-row/upsert races, multi-instance proof | All three | Choose one isolation/protocol, no claim annotation alone suffices |
| 8. Idempotency | Bind authenticated UUID/operation/key to frozen payload/origin; unique DB identity, never auto-rekey unknown result | Separate command ledger with same guarantees | Storage/retention cost; concealment limits recovery | Duplicate/changed-key payload, crash after commit, hidden P1 parity | TV2 messaging + TV3 commands + TV1 privacy | Retention/permanent reservation; accept409 compatibility |
| 9. Outbox/events | Commit domain facts and outbox together; publish after commit with immutable identity and recipient ACL projection; deduplicate | DB log tailing with equivalent atomicity | At-least-once duplicates require consumer idempotency | Crash before/after commit/publish/ack, per-stream order/revocation | TV2 logs + TV3 facts + TV1 privacy | Outbox shape, dispatcher barrier and event registry; no new event invented |
| 10. Retry | Bounded whole-command retry for known rollback/deadlock/serialization only; same frozen key, current ACL each attempt | Caller retry under explicit retryable outcome | Latency/charge bounds; unknown commit cannot be blindly resent | Lost commit response, aborted transaction, timeout saturation | All three | Attempts/backoff/deadlines and recovery disclosure |
| 11. Rollback | Any failure rolls back invite/member/period/counter/event/idempotency outcome together; unknown commit stays uncertain | Reviewed resumable command/saga | Atomic scope may be large; partial external effects unacceptable | Fail after each write, no externally published uncommitted fact | All three | Budget/audit rollback versus nontransactional logging boundaries |
| 12. Cleanup / revocation | Logical authority revision/admission denial first; physical cancellation/cleanup asynchronous, accounted until settled | Synchronous drain with bounded containment | Prompt denial != physical completion; in-flight data cannot be retracted | Revoke/dispatch/result/render races, queue full, stalled cleanup | TV1 session + TV2 transport + TV3 scopes | Strong completion barrier and resource termination proof |
| 13. Multi-module ownership | One named command coordinator owns outer transaction; modules join same datasource/manager, no autonomous protected-write commit | Approved distributed transaction redesign | Avoids split commit, requires jointly reviewed interfaces | Actual proxy/propagation/rollback and cross-module fault tests | TV1/TV2/TV3 | Coordinator interface, propagation and deployment operator approval |

### Acceptance sequence and consequences

Before selecting A, owners must approve exact authoritative roots/revision storage, all affected writers/readers and final admission protocol; demonstrate bounded lock sets, rank compliance and crash/revocation behavior. Choosing B instead requires persisted epochs/generations, cross-instance invalidation acknowledgement and a strong completion barrier, with the same safety/unknown-commit tests. Neither can be inferred from current Groups invitation-first locking or client cleanup lanes.

J+1 specifically means a committed counter watermark under group serialization, never `nextval` reservation or wall time. Send-before-join commits at J and is excluded; join-before-send makes that later send eligible. Remove-before-send denies removed sender; send-before-remove may commit, but removed viewer has no current GROUP access. Gaps are allowed only with proven committed watermark semantics; no receipt prefix manufactured across hidden gaps. Overflow denies safely pending allocator policy.

Consequences: shared owner review increases lead time; coarse locking may limit throughput; durable outbox/revisions add storage; strong revocation can add admission/drain latency. Benefits are one explainable atomic authority boundary and auditable lineage. No migration, event type, numeric timeout, version or deployment is approved by this ADR.

## Adoption and evidence record

Required signers: Phúc for full ADR/Groups/foundation, Quốc Đạt for security/social/account fences, Tiến Đạt for Conversation/sequence/log/transport, every additional affected schema owner, and the named custodian for environment execution only. Record exact ADR commit/hash, selected strategy, rejected alternatives, scope/conditions/evidence, signer/date/source and authorized next action using the [checkpoint handoff](TV3_SHARED_ARCHITECTURE_CHECKPOINT_HANDOFF.md).

Until then: **PROPOSED — AWAITING OWNER APPROVAL**; DB lineage UNKNOWN; no versions allocated, no V1 edit, no V2, no shared DB access or Day 1 Exit.
