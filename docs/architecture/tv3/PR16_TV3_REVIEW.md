# PR16 — TV3 review

26/11/2026. Exact reviewed HEAD `fae99ae0cf96f620da426b89abbd3c42920702e8`; main `8b730dccc171c6d55fd8ae127448ed2b6c5f23a4`. Source and exact-head review provenance: [audit](FOUNDATION_AUDIT.md). No official GitHub review submitted. Severity describes implementation gates, not demonstrated exploits.

## Coverage and conclusion

All eight changed files were read: README (baseline/evidence), auth-user-friend-api-inventory (all18 entries), composition-root-integration (host/adapters), coordinator-fix1-design (F01–F09), cross-team-decision-register (R01–R26 and D01–D10), jwt-session-lifecycle (storage/refresh/revoke), security-auth-spec (OTP/principal/provisioning) and security-transport-addendum-v1 (A–F and T01–T17). They are all under docs/architecture/tv1 at the reviewed HEAD.

TV3 supports the existing direction: one backend; TV1 shared identity/security/composition root; server ACL; distinct history/replay; bounded owner-scoped cancellation; separate Foundation stabilization. This handoff recommendation neither approves contracts for other owners nor authorizes merge/shared implementation.

TV3 cannot yet consume production identity/directory interfaces: runtime, principal/session authority and approved wire/adapters remain missing. Public DTO allowlisting is appropriate as a proposal. No actual private-user lookup leak or runtime defect introduced by PR16 was reproduced; its entire diff is Markdown.

## Findings

Each record has type, exact evidence, risk, correction, owner, dependency and status. OPEN means delivery/correction missing; NEEDS OWNER DECISION means unresolved policy. Source Sxx references resolve through the audit index.

### TV3-R16-01 — BLOCKER

- Type: Missing implementation.
- Source: [docs/architecture/tv1/security-auth-spec.md:56](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/security-auth-spec.md#L56); [backend/src/main/java/com/nexa/chat/backend/groups/GroupConfiguration.java:36](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupConfiguration.java#L36); [backend/src/main/java/com/nexa/chat/backend/groups/GroupController.java:21](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupController.java#L21).
- Evidence: Proposal requires trusted UUID/sid. Current issuer/audience decoder and local sub parsing have no TrustedPrincipal mapper, active-session authority or issuance runtime; missing issuer rejects all tokens.
- Risk: Assuming sid revocation/required-claim guarantees from current code is unsupported.
- Correction: TV1 publishes approved trust/claim/session-consistency contract and one principal boundary; implement malformed/missing claim, wrong key/issuer/audience, revoke/outage and REST/WS negative tests. No TV3 duplicate decoder.
- Owner: TV1; TV2/TV3 consumers.
- Dependency: Auth design, user schema, shared baseline.
- Status: **OPEN**.

### TV3-R16-02 — BLOCKER

- Type: Missing directory / cross-team policy.
- Source: [docs/architecture/tv1/auth-user-friend-api-inventory.md:41](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/auth-user-friend-api-inventory.md#L41); [backend/src/main/java/com/nexa/chat/backend/groups/GroupConfiguration.java:17](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupConfiguration.java#L17); [backend/src/main/java/com/nexa/chat/backend/groups/GroupDirectory.java:6](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupDirectory.java#L6).
- Evidence: Inventory is proposed. Default server directory503; Android PeopleDirectory is only a callback port; V1 users contains id only.
- Risk: Production create/accept cannot use unavailable adapter; client search/friend/eligible state cannot authorize server actions.
- Correction: TV1 provides authoritative user provisioning/current-user and server eligibility adapter; TV3 chooses invite policy; TV2 reviews DIRECT effects. Specify hidden/deleted/disabled users, both block directions, pending invite after block and accept-vs-block ordering; outage fails closed.
- Owner: TV1 authority, TV3 Groups policy, TV2 DIRECT.
- Dependency: identity/friend/block schema and routes.
- Status: **OPEN**.

### TV3-R16-03 — HIGH

- Type: Missing enforcement / open contract.
- Source: [docs/architecture/tv1/security-transport-addendum-v1.md:44](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/security-transport-addendum-v1.md#L44); [backend/src/main/java/com/nexa/chat/backend/groups/GroupService.java:183](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupService.java#L183); [backend/src/main/resources/db/migration/V1__shared_conversations_and_group_invitations.sql:13](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/resources/db/migration/V1__shared_conversations_and_group_invitations.sql#L13).
- Evidence: Current reads require ACTIVE; any existing member row prevents rejoin. No period/removal/history/media/WS implementation exists.
- Risk: Future send/history/replay/download paths have no approved visibility floor or revoke ordering.
- Correction: TV3 defines membership state/period/revoke admission boundary; TV2 integrates sequence/fanout/media; TV1 reviews privacy. Approve backfill/migration before implementation and distinguish new admission from bytes already handed to transport.
- Owner: TV3 membership, TV2 data paths, TV1 privacy.
- Dependency: TV2 full wire, periods schema, DB history.
- Status: **NEEDS OWNER DECISION**.

### TV3-R16-04 — HIGH

- Type: Shared ownership conflict.
- Source: [docs/architecture/tv1/cross-team-decision-register.md:37](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/cross-team-decision-register.md#L37); [backend/src/main/java/com/nexa/chat/backend/groups/GroupConfiguration.java:56](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupConfiguration.java#L56); [backend/src/main/java/com/nexa/chat/backend/groups/GroupErrors.java:11](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupErrors.java#L11).
- Evidence: Global chain/decoder and unscoped advice live in Groups; backend is absent from main.
- Risk: Copying all PR6 for bootstrap would inherit domain/schema/security policy and risk duplicate infrastructure.
- Correction: Use file/hunk manifest: TV3 infra, TV1 security/principal, TV2 Conversation, TV3 membership. Preserve behavior until owner-reviewed follow-up; one app/history.
- Owner: TV3 coordination; TV1 security; TV2 Conversation.
- Dependency: manifest/table-owner approvals and environment evidence.
- Status: **OPEN**.

### TV3-R16-05 — MEDIUM

- Type: Documentation inconsistency.
- Source: [docs/architecture/tv1/coordinator-fix1-design.md:9](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/coordinator-fix1-design.md#L9); [docs/architecture/tv1/jwt-session-lifecycle.md:9](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/jwt-session-lifecycle.md#L9).
- Evidence: PR16 says FIX1 design-only/changes requested at1c459c5. Current source5f8c081 has recoverable hook containment, quarantine/retry; TV2 officially approved exact HEAD.
- Risk: Stale blocker can cause duplicate implementation or imply production adapters were also approved.
- Correction: TV1 adds dated current snapshot and exact-head approval. Preserve original2/3 UI failure and scope limits; no implied host/transport/merge approval.
- Owner: TV1 docs.
- Dependency: PR13 exact-head review in audit.
- Status: **OPEN**.

### TV3-R16-06 — MEDIUM

- Type: Documentation inconsistency.
- Source: [docs/architecture/tv1/README.md:40](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/README.md#L40); [docs/architecture/tv1/composition-root-integration.md:11](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/composition-root-integration.md#L11).
- Evidence: PR16 still reports Groups45c90a7/73 JVM and compilation-only UI. Current2e804c0 report section13 records93 JVM,13 actual UI and independent cleanup lanes; backend unchanged.
- Risk: Stale evidence obscures delivered lifecycle fixes or conflates fixture UI with production security.
- Correction: TV1 adds current dated source/report references, retaining historical failures and fixture limits.
- Owner: TV1 docs; TV3 supplies evidence.
- Dependency: S19 and inspected logs/XML.
- Status: **OPEN**.

### TV3-R16-07 — MEDIUM

- Type: Proposed adapter / privacy mismatch.
- Source: [docs/architecture/tv1/auth-user-friend-api-inventory.md:23](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/auth-user-friend-api-inventory.md#L23); [app/src/main/java/com/nexa/chat/feature/groups/model/GroupModels.java:10](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/app/src/main/java/com/nexa/chat/feature/groups/model/GroupModels.java#L10); [app/src/main/java/com/nexa/chat/feature/groups/repository/PeopleDirectory.java:13](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/app/src/main/java/com/nexa/chat/feature/groups/repository/PeopleDirectory.java#L13).
- Evidence: Proposed public DTO is id/displayName/avatarUrl with opaque cursor. Existing Person additionally carries publicIdentifier/eligible; PeopleDirectory uses page/hasNext. No real mapper exists; no PII exposure established.
- Risk: Blind projection could expose phone; naive cursor conversion can skip/duplicate users or reuse state across accounts.
- Correction: Approve explicit mapper: omit private fields; optional publicIdentifier absent unless separately authorized; eligible advisory. Scope cursor state to account/query/page; close owned adapter calls only.
- Owner: TV1 directory adapter; TV3 consumer.
- Dependency: privacy/paging/empty/error contract.
- Status: **NEEDS OWNER DECISION**.

### TV3-R16-08 — LOW

- Type: Wire/error documentation gap.
- Source: [docs/architecture/tv1/auth-user-friend-api-inventory.md:27](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/auth-user-friend-api-inventory.md#L27); [backend/src/main/java/com/nexa/chat/backend/groups/GroupService.java:57](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupService.java#L57); [backend/src/main/java/com/nexa/chat/backend/groups/GroupErrors.java:40](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupErrors.java#L40).
- Evidence: Inventory validation convention400; current duplicate/self invite422. Advice/auth/body-limit producers allocate independent requestIds.
- Risk: Unreviewed common renderer could change client semantics or concealment; correlation is not one request lifecycle yet.
- Correction: TV1 renderer, TV2 wire and TV3 domain review decide422 mapping; assign ID once across producers and test413/401/403/404/409; no refresh on403/concealed404.
- Owner: TV1 common security; TV2 wire; TV3 domain.
- Dependency: approved shared error contract.
- Status: **NEEDS OWNER DECISION**.

### TV3-R16-09 — INFORMATIONAL

- Type: Resolved historical implementation defect.
- Source: [docs/architecture/tv1/coordinator-fix1-design.md:9](https://github.com/datcaq/NEXA-Chat/blob/fae99ae0cf96f620da426b89abbd3c42920702e8/docs/architecture/tv1/coordinator-fix1-design.md#L9).
- Evidence: TV2-R13-01 is CLOSED by exact-head approval at5f8c081. It remains an old reproduced defect, not a current open source finding.
- Risk: Losing provenance would wrongly re-open the defect or overstate approval.
- Correction: Keep closure only for Coordinator FIX1; production admission/physical termination still separately gated. No new source correction requested.
- Owner: TV1 FIX1; TV2 reviewer.
- Dependency: exact-head review.
- Status: **RESOLVED**.

## Consumption boundaries

Self profile is not actor authority. TV1 provisions stable UUIDs, including external-sub mapping if chosen, before membership FK use. Public DTO permits only policy-filtered labels/avatar/UUID; no phone, block state or internal session fields. Absence of a production endpoint means privacy is not E2E verified.

Server GroupDirectory is distinct from Android PeopleDirectory and must make transaction-consistent existence/eligibility checks. Own pending-invitation controls are authorized by invitee identity before membership, with no group-resource/history access. Current processed invitation409 remains until reviewed change.

Current Groups bounded accounting can retain TERMINATED vs SETTLED while adopting a future TV1 gate. Integration must prove actual enqueue fencing, one total deadline, current-epoch result application, owned-call cancellation and preserved shared pools. Snapshot lookup alone does not close enqueue races. Timeout values remain candidates.

Shared implementation remains gated by [manifest](BACKEND_FOUNDATION_PREPARATION.md), [decisions](DECISION_REGISTER.md), sanitized DB history and [TV2 handoff](TV2_DEPENDENCY_TRACKER.md). Findings01–04 are delivery/decision gates,05–06 stale docs,07–08 owner choices; no claim of a new runtime security exploit.
