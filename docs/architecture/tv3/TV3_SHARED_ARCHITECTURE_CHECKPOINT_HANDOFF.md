# TV3 Shared Architecture Checkpoint — Handoff

26/11/2026, Asia/Saigon. Prepared by Codex. **PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION.** This packet is for a human checkpoint, not an approval message sent on Phúc's behalf.

## Outcome sought and attendees

Record exact owner selections, conditions, evidence gaps and permitted next work. Required: Hoàng Phúc (TV3 Groups/Membership/foundation), Quốc Đạt (TV1 Auth/User/Friend/Security), Tiến Đạt (TV2 Conversation/Message/sequence/request/wire). A named environment custodian is required to establish deployment authority and supply sanitized DB lineage; registry coordination is not that authority. No shared DB operation is authorized by this packet.

Scope: D08–D10, D22–D25, Auth/Message Request/GroupInvitation boundaries, physical transaction choice and full D01–D30 impacts. Out of scope: implementation, version allocation, migration execution, Word edits, contract freeze, PR approvals/merge, Day 1 Exit declaration, CONTRACT-01F and Day 2. Do not replace TV2's 01E.2C.

## Pre-read and provenance

1. [Owner adoption draft and exact revision/hash ledger](TV3_CONTRACT_01E2B_OWNER_ADOPTION_DRAFT.md).
2. [ADR D-08 with all thirteen transaction topics](ADR-D08-SHARED-FLYWAY-REGISTRY.md).
3. [Nine decision cards](TV3_SHARED_ARCHITECTURE_CHECKPOINT_DECISION_CARDS.md).
4. [Historical TV3 FIX3 response](TV3_FORMAL_OWNER_RESPONSE_FIX3.md), [original architecture register](DECISION_REGISTER.md), [ownership matrix](OWNERSHIP_MATRIX.md), [Flyway verification plan](FLYWAY_VERIFICATION_PLAN.md), [Groups readiness](GROUPS_CONTRACT_READINESS.md), [backend preparation](BACKEND_FOUNDATION_PREPARATION.md), [dependency tracker](TV2_DEPENDENCY_TRACKER.md). These remain dated records, not silently updated approvals.
5. [TV1 formal response at c48f75e](https://github.com/datcaq/NEXA-Chat/blob/c48f75eacf3b9f428c1f683e4a612d7ec9638d63/docs/architecture/tv1/01-formal-owner-response-confirmed.md) and its narrow human policy addendum/source reconciliation.
6. External checkpoint ZIP and two verified reports, original FIX3 Draft/Register/R4/Handoff at hashes in the ledger. Reviewers need these exact bytes; no fake Git publication links. Newly delivered author report does not rewrite its earlier absence.
7. [Groups source/initial atomic acceptance](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupService.java) and [existing PostgreSQL tests](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/test/java/com/nexa/chat/backend/groups/GroupPostgresTest.java).

Latest TV1/TV2 response wording in the master task is user-reported alignment. GitHub inspection did not recover those exact comments/reviews. TV1's published narrow confirmation independently supports part of the direction. TV2 01E.2C was not found in searched local paths/origin trees/matching history or PR9 metadata/comments; obtain it with exact revision/hash before claiming consumption. Reported alignment is enough to prepare options, not to record signatures.

## Proposed agenda

| Order | Topic | Concrete record expected |
| --- | --- | --- |
| 1 | Pins, provenance, response scope, 01E.2C | Confirm available bytes, reported versus published confirmations and missing historical inputs |
| 2 | D08 responsibilities/lineage | Coordinator/domain/custodian split; ADR status; evidence owner, no version allocation |
| 3 | Physical transaction | A/B/isolation/ranks/admission/completion/capacity; all thirteen open topics assigned |
| 4 | D09 | Stable member/new period; committed J+1, empty interval, legacy and DIRECT separation |
| 5 | D10 | A/B privacy; named/former viewers; any/all and audience basis; empty/unknown/author/tombstone |
| 6 | D25 | Expiry/reinvite/cancel/roles/409/fresh-period policy; exact ordered accept and seven race pairs |
| 7 | D22/D23/D24 | Route compatibility and NOT RUN fixtures; bounded provenance acceptance; translation ACL/version/privacy |
| 8 | Auth and Message Requests | TV1 security/current account fence; C/no-intro/chat consent and no GROUP effects; TV2 wire/transaction |
| 9 | D01–D30 / gates | Review every row, sign only exact scope, assign condition closure and next authorization |

## Required evidence and Day 1 checklist

The categories below describe this packet's assessment, not new original-register statuses. Runtime schedules in cards/ADR are proposed and **NOT RUN**. Historical tests stay attributed to their original reports; no build/test is rerun merely for four Markdown additions.

| Requirement | Current category | Evidence / remaining gate |
| --- | --- | --- |
| Exact checkpoint ZIP/report delivery | Resolved (integrity/delivery only) | Expected hashes match; original author report newly received and matches explicit historical reference |
| TV1 owner response | Accepted with conditions / aligned direction | Published narrow business confirmation; latest response relayed; detailed shared sign-off pending |
| TV2 owner response | Aligned direction / needs evidence | B report + latest reported concurrence; obtain 01E.2C and scoped adoption |
| TV3 owner response | Pending human decision | Z and this packet are Codex proposals; Phúc has not signed them |
| D08 ownership / full ADR | Aligned direction / pending human decision | Full ADR now prepared; adoption, physical strategy and custodian still pending |
| D09 history / DIRECT interaction | Pending human decision | Exact boundary/empty representation/legacy/exception and TV1 privacy selection |
| D10 receipt audience / aggregate | Pending human decision | A/B and denominator unselected; privacy/wire proof pending |
| D22 compatibility | Aligned direction / needs evidence | Route-specific direction reported aligned; exact inventory, mapping and fixtures pending |
| D23 provenance | Needs evidence | Historical01A/draft/approval chain and exact preliminary fallback unavailable; bounded limitation acceptance not signed |
| D24 translation | Accepted with conditions | Dispatch/result/use ACL/version/account boundary proposed; privacy/provider selection and human adoption pending |
| D25 lifecycle | Conflict / pending human decision | Original CONFLICT unchanged; separate invitation direction, fresh-period/expiry/cancel/reinvite choices unresolved |
| Auth/Security boundary | Aligned direction / needs evidence | TV1 OTP registration/password login; detailed trusted UUID/sid/refresh/revoke/WSS evidence pending |
| Messaging boundary | Aligned direction / needs evidence | Reported separate no-intro requests/chat-only consent; no preaccept full DIRECT; TV2 exact wire/acceptance pending |
| Membership/history/receipt shared shape | Needs evidence | Current Groups lacks periods/revisions/sequence/receipt ACL; no schema selected here |
| Database migration lineage | Needs evidence | Actual target schema/Flyway history/checksum UNKNOWN; raw V1 hash not deployment proof |
| Shared transaction strategy | Pending human decision / needs evidence | Candidate A detailed, B alternative; global order/final-admission/capacity proof absent |
| Original decision register provenance | Needs evidence / conflict retained | 30 exact IDs and 21 OPEN/9 CONFLICT/0 APPROVED preserved; scoped future signatures needed |
| Formal checkpoint record / owner approvals | Pending human decision | No meeting or signature fabricated; use template below |

## Decision recording template

Record one entry per decision, not blanket approval of a report. Keep earlier entries append-only; new source revision requires explicit applicability review.

```text
Decision ID / title:
Meeting date/time/timezone and attendees:
Document commit / artifact hash / original decision revision:
Published evidence versus user-reported confirmation:
Selected alternative and exact normative scope:
Rejected alternatives and reasons:
Domain owner(s) and affected cross-reviewers:
Named human signer / date / attributable source URL or record:
Conditions, evidence owner, due milestone and closure criterion:
Tests performed (exact run/log/pin) versus NOT RUN:
Unresolved limits, privacy/history/DB lineage exclusions:
Status: resolved / aligned direction / accepted with conditions /
        needs evidence / conflict / pending human decision
Permitted next action (separately authorized):
```

Until a real signature exists, required Phúc entries say **PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION**: D09 exact history/DIRECT interaction, D10 audience/former-member/aggregate policy, D25 expiry/reinvite/cancellation/accept-to-period, full ADR D08, physical transaction choice and any cross-domain schema. No Codex-generated entry names Phúc as having approved.

## Exit criteria and blockers

Preparation is complete when the four documents, exact evidence ledger, all nine ten-field cards, thirteen-topic transaction analysis, full30 mapping and link/diff checks are reviewable and published on existing Draft PR17. This is not Day 1 Exit.

Checkpoint resolution requires actual owner selections and attributable record; remaining conditions must identify whether they block design adoption or later activation. Day 1 Exit additionally requires evidenced owner responses/sign-offs, resolved required D08–D10/D22–D25 level, Auth/Messaging/membership-history-receipt boundaries, DB lineage, adopted full ADR/transaction strategy, accepted register provenance and formal checkpoint record. No owner alignment or raw hash substitutes for those gates.

Current blockers: Phúc decisions; TV1 detailed DIRECT/privacy/current revocation decisions; TV2 01E.2C/sequence/wire/physical strategy concurrence; unknown DB history/custodian; incomplete historical approval provenance; unproven final admission/strong revoke/capacity; current Groups integration gaps. Later runtime acceptance remains separate, unexecuted evidence rather than a reason to fabricate checkpoint completion.

Git delivery target: `docs/tv3-day01-close`, existing [Draft PR17](https://github.com/datcaq/NEXA-Chat/pull/17), reviewers datcaq/Tender0603 preserved. Normal commit/push only; no new PR, review approval, messages to owners, merge, force-push or amendments. Commit identity and post-push exact-head verification are reported in delivery output to avoid a self-referential commit hash.

**STOP after publication. Wait for Hoàng Phúc's explicit review/confirmation; do not proceed to CONTRACT-01F or Day 2.**
