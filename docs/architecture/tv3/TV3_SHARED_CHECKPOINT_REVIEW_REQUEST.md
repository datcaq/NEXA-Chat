# CONTRACT-01E.3 — Shared Architecture Checkpoint Review Request

26/11/2026, Asia/Saigon. Authorized publication for Hoàng Phúc, TV3. **Review requested; TV1/TV2 signatures pending. PR17 stays Draft.**

## 1. Review packet and evidence

Read [Owner Decision Record](TV3_OWNER_DECISION_RECORD_01E3.md) first, then [Formal Owner Response / full D01–D30 mapping](TV3_FORMAL_OWNER_RESPONSE_01E3.md). [01E.3 worksheet](TV3_OWNER_DECISION_REVIEW_01E3.md) is preserved preparatory context. [ADR](ADR-D08-SHARED-FLYWAY-REGISTRY.md), [cards](TV3_SHARED_ARCHITECTURE_CHECKPOINT_DECISION_CARDS.md) and [old handoff](TV3_SHARED_ARCHITECTURE_CHECKPOINT_HANDOFF.md) remain historical proposals; their unselected alternatives are superseded only within the current owner record's scope. Ordered locks remain an evaluation candidate, not an approved transaction strategy.

The local TV2 01E.2C addendum has now been read. Its actual hashes, fallback provenance and older policy-pending state are recorded in the Owner Decision Record. No unpublished artifact is reconstructed or falsely assigned a Git URL. TV2's addendum and TV1's published confirmations do not supply missing cross-approval of today's detailed selections.

Review target is the exact new PR17 publication HEAD, supplied in the PR description/delivery after push. A review of older727d018 is not automatically a review of this packet. No shared register status changes are made:21 OPEN,9 CONFLICT,0 APPROVED.

## 2. TV1 — datcaq / Quốc Đạt

- [ ] D08: confirm TV3 administrative charter, domain/affected-owner approval split and separately authorized custodian. No unilateral TV3 Auth/User/Security schema approval.
- [ ] D09: review latest-ACTIVE GROUP privacy, no P1 authority inheritance, current account/block fences and concealed retry behavior. Provide separate DIRECT acceptance/history/block/unfriend/unblock/reopen scope with TV2.
- [ ] D10: approve/amend eligible sender + own-only disclosure; no peer/former member receipt; no high-water hidden activity; author excluded; no aggregate this phase. Review UNKNOWN/NONE/WITHHELD and historical identity leakage without adding new enums implicitly.
- [ ] D25: define authoritative current ACTIVE OWNER/inviter and invitee eligibility/block/account recheck, cancellation actor, role-change/accept and revoke races. Do not treat current code's ACTIVE-only inviter check as conformance to new OWNER policy.
- [ ] TX: provide account/sid/social authority inventory, cross-instance revoke completion and final response/WSS admission boundary; physical cleanup is not immediate revocation proof.
- [ ] Retain TV1 Auth ownership/Session V2; audience/key/refresh/recovery pilot and production evidence stay scoped. Review current principal/security adapter boundary without treating policy publication as implementation approval.

## 3. TV2 — Tender0603 / Tiến Đạt

- [ ] D08: explicit concurrence at current publication revision on registry charter/domain ownership/affected-owner review; supply missing shared transaction participation requirements.
- [ ] D09/D13: commit-safe J allocator, rollback/gaps/overflow, new-period boundaries and exact wire for selected future-open interval + snapshot H + period/future floor. No timestamp/nextval-reservation/UUID-order shortcut; legacy history activation waits for a proven safe allocator baseline/cutover, no fabricated floor.
- [ ] D10/D16/D17: immutable audience-at-send internal eligibility, author exclusion, per-message projection and no any/all/count aggregate this phase; persistence keys, READ<=DELIVERED, durable device/viewport proof, no high-cursor leak or account/period/history-revision merging; sender versus own/former visibility fixtures.
- [ ] D25: stable member/new-period rejoin via new invitation, OWNER cancellation/current inviter recheck, processed/ACTIVE409 compatibility, atomic invite/member/period/floor/facts. Identify exact DTO/schema/adapter gaps without changing them in this publication.
- [ ] D12/D27: durable outbox/conversation/account facts, dedup/order/coverage and current recipient publication; no invented event types or blanket registry expansion.
- [ ] D20/D26: unknown-commit reconciliation preserving original identity, usable-transaction conflict reread and canonical DIRECT uniqueness under current ACL. Do not change409 to200 without a joint scoped decision.
- [ ] Confirm receipt of the owner record and relate it to supplied01E.2C; update interpretation of older pending-policy notes through a dated response, not a rewrite of historical artifacts.

## 4. Three-owner checkpoint agenda / required evidence

| Topic | Required decision or evidence | Gate |
| --- | --- | --- |
| Exact provenance | Current PR head, owner instruction scope, TV1/TV2 response source/date; measured01E.2C bytes; missing historical approvals explicitly retained | Scoped sign-off/G7 |
| D08 charter | Coordinator/domain/custodian separation; future serialized reservation protocol, one chain/history; no version allocated | Governance adoption |
| D09/D25 shared shape | Stable member/new period, committed J+1, empty-floor metadata, legacy mapping, current authority, rejoin/cancel/reinvite/409 field and action matrix | G0/G1 before dependent implementation |
| D10 scope | Exact eligible sender/own/no peer/no former projection; no aggregate phase boundary; explicit handling of any required full-P0 aggregate acceptance | G1/G6, no silent scope reduction |
| Physical TX | A/B/SERIALIZABLE comparison, complete roots/ranks/missing-row serialization, cross-instance proof, final disclosure/revoke completion, crash/recovery and bounded capacity | DEFER until selection justified |
| Atomic publication/recovery | One transaction for invite/member/period/J+1/facts; outbox duplicate/order/current ACL; unknown commit and original identity | G0/G2, later runtime activation |
| DB lineage | Named authorized custodian; sanitized target/config/tool versions/schema/history/version/script/checksum/success/drift; backup/restore readiness | UNKNOWN until supplied; no DB access here |
| Remaining decisions | Full30 statuses/conditions, D22 route compatibility, D23 bounded provenance, D24 translation privacy, Host/security/transport and full P0 | Applicable G0–G7 |

Design-only discussion/sign-off need not wait for every future runtime test. Exact protocol/assumptions must exist before dependent code; runtime proofs are later gates and remain NOT RUN unless logs/pins show actual execution. Existing initial Groups acceptance tests do not prove fresh-period/cancel/remove behavior or production JWT/directory correctness.

## 5. Checklist before any separately authorized CONTRACT-01F

- [ ] Phúc's selected policy record published at an immutable commit; TV1/TV2 review the same revision, with exact selected scope/exclusions.
- [ ] Required affected-owner selections and disagreements recorded per decision, including full30 “not addressed”/conditional items; no blanket APPROVED conversion.
- [ ] D08 governance and full ADR/TX status separated; TX DEFER remains a blocker for dependent writes unless a justified strategy is subsequently selected by all three.
- [ ] D09 allocator/empty/legacy and D10 wire/privacy/device scope, D25 lifecycle/atomicity conditions assigned to named owners with closure criteria.
- [ ] Historical limitations accepted explicitly or recovery actions recorded;01E.2C availability is no longer marked missing in the current ledger, while older searches remain historical.
- [ ] Formal checkpoint record names alternative, exact commit/hash, human signer/date/attributable source, conditions/evidence owner and publication eligibility.
- [ ] Exact01F documentation scope explicitly authorized afterward. This packet does not create01F, edit FIX3 Draft/Register, freeze an API, approve DDL or authorize implementation.

Partial checkpoint outcomes may record a decision as conditional or deferred; that does not remove its dependency gate. Day1 Exit additionally needs the required D08–D10/D22–D25 level, Auth/Messaging/membership-history-receipt boundaries, DB lineage, adopted ADR/transaction strategy, register provenance and actual scoped signatures/checkpoint record. **Day1 Exit is NOT READY.**

## 6. Required review response format

```text
Reviewer / owner / date / attributable source:
Exact PR17 HEAD reviewed:
Decision ID / bounded subdecision:
Agree / agree with conditions / request revision / defer:
Exact adopted scope and exclusions:
Unresolved conditions, evidence owner, closure criterion/gate:
Source/contract/runtime evidence refs (NOT RUN where applicable):
Permitted next publication scope (if any):
```

Reviewers `datcaq` and `Tender0603` were already requested on PR17 at preflight. Preserve those requests; a dated description update advertises this new packet but does not fabricate a newly submitted review or delivered private message. Post-push current reviewer/request state is verified in delivery output. Do not claim a re-request operation unless actually performed.

## 7. Publication boundary and stop

Normal commit/push on `docs/tv3-day01-close`; existing Draft PR17 only. Publish the three new records and the unchanged prior01E.3 worksheet as supporting input. Preserve historical documents, existing backend build artifacts/Phase1 drafts and original input ZIPs. No source/test/schema/migration/interface/Word changes, no new migration reservation or shared DB connection.

After publication, STOP for TV1/TV2 formal review and the Shared Architecture Checkpoint. No approval/merge on behalf of others, Ready-for-Review transition, automatic contract freeze, CONTRACT-01F or Day2 implementation.
