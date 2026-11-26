# Implementation readiness — 26/11/2026

**Documentation handoff PREPARED; shared implementation / production integration NOT READY.** This milestone creates reviewable evidence and plans only. Current source/tests and old failures are attributed in [audit](FOUNDATION_AUDIT.md); [PR16 review](PR16_TV3_REVIEW.md) identifies blockers. No phase is complete just because a document exists.

| Phase / current state | Entry conditions | Required deliverables / owner | Dependencies | Exit criteria | Current blockers |
| --- | --- | --- | --- | --- | --- |
| Architecture approval — PARTIAL | Current refs/evidence and historical directions reconciled | D01–D10/subdecision ledger, current PR16 review, exact-head sign-off; all3 respective owners | TV2 full wire/register; TV1 inventory/principal; TV3 policy/manifest | Owners approve exact contract/schema/hunks and record conditions; explicit scope to implement | Detailed wire/policies open; D08 full ADR insufficient evidence; PR16 no official review |
| Backend infrastructure — PREPARATION ONLY | Approved file/hunk scope, table lineage decision and safe target | One bootstrap/pom/config/DB/health/log/error runbook +isolated smoke tests; TV3, TV1/TV2 review | Security beans and shared entities currently mixed into PR6; env owner | Fresh boot/datasource/Flyway/restart/failure tests actually pass on approved candidate; one app/history | No isolated baseline PR; extraction not authorized by docs milestone; production health/log policy absent |
| Shared schema approval — BLOCKED | Canonical V1/env history and owners identified | Conversation/Message/periods/sequence/identity mapping, approved migration/backfill/version registry; TV2/TV3/TV1 table owners | D02–D08; sanitized target DB evidence | Exact SQL reviewed; populated upgrade, compatibility, rollback/restore strategy and invariant tests | Message/period schema/wire absent; DB application state UNKNOWN |
| Auth integration — BLOCKED | TV1-approved Auth/identity/security design; baseline ready | Real issuance/UserDirectory/SessionRepository/store/rotation/revoke/TrustedPrincipal +adapters; TV1 | Provider/OTP/login policy, sid consistency, schema/security | Real valid/invalid JWT and user lifecycle, single-flight/lost-response/logout/outage, directory privacy and cross-account tests | FIX1 approved foundation alone supplies none of these production adapters |
| Messaging integration — BLOCKED | TV2 wire/register/schema approved; principal and membership boundary ready | Persisted Conversation/Message/outbox/ACK, REST/WS/sequence/receipts/history/replay +physical cancellation; TV2 | TV1 credentials/host; TV3 periods/ACL; single history | Real A/B DIRECT/GROUP send/reopen/reconnect/dedupe, no unauthorized replay/fanout, correct unknown outcomes | RAM fake only; complete wire/register missing; no production transport |
| Groups integration — PARTIAL foundation | Real identity/directory +approved policy/schema; TV1 host bridge and TV2 Message entry | Adapt Groups ports and scope gate; membership lifecycle, invite policy and DB tests; TV3 with TV1/TV2 | Shared principal/social API/Message wire, periods migration | Real create/invite/accept/reject then chat; remove/rejoin/history/media revoke races; UI/session isolation with actual adapters | Default directory503; GROUP02 absent; ADMIN/succession/disband/expiry open |
| E2E acceptance — MISSING | Integrated reviewed builds with real A/B and approved environments | Device/server/DB trace packet, P0 regression/demo, backup/restore evidence; all3 | All above, real media/Friends/Profile and product acceptance | Auth→Home→friends→DIRECT→GROUP persisted/reopened; block/removal/expiry/logout/switch/late-callback negative tests; exact SHA/XML0 skipped | Existing API37 fixture UI and test JWT do not verify this path |

## Work that can proceed independently

Review owner packets, supply sanitized environment inventory, update stale PR16 evidence and publish TV2 contract artifacts. Preparation does not need a new fake Auth/WS implementation. Future scoped foundation work can be authorized without pretending production E2E is already complete; its own interfaces/lineage/tests must first be approved.

## Concrete integration acceptance

- TV1 supplies matching latest UUID/epoch access and real credentials, one shared admission fence, directory projection and navigation adapter; no local UUID login.
- TV2 supplies exact versioned wire/register and Message/membership integration requirements; no contract authored on its behalf here.
- TV3 supplies [Groups policy gaps](GROUPS_CONTRACT_READINESS.md), [file/hunk manifest](BACKEND_FOUNDATION_PREPARATION.md) and [Flyway plan](FLYWAY_VERIFICATION_PLAN.md).
- Each real transport verifies invalidation-before-enqueue, timeout vs cancellation vs termination, bounded retiring capacity, no shared pool disposal, and stale callback protection at result application.
- Schema rollout requires approved target/backup/restore and current history. No shared DB access was authorized or attempted here.

Checklist counts from older 14-day audit are historical snapshots; this documentation task does not mark product days or GROUP02 complete. Build/test commands were not rerun because no executable files changed; existing evidence was read, and Markdown/ref/path/diff checks are the required current verification.
