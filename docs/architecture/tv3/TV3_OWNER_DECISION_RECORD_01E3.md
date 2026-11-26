# CONTRACT-01E.3 — TV3 Owner Decision Record

26/11/2026, Asia/Saigon. **TV3 OWNER POLICY CONFIRMED — AFFECTED-OWNER REVIEW PENDING.**

Owner: Hoàng Phúc, TV3. Codex records the explicit selections in the user's current task, “Publish TV3 Owner Decisions & Prepare Shared Architecture Checkpoint”, section3. This is an attributable owner instruction in this conversation, not an invented GitHub review, cryptographic signature or signature by TV1/TV2. Private source identifier: attachment `164fd84f-7b26-424f-abc3-6c0d2cbfff5e/Pasted text.txt`; no public attachment URL is claimed.

## 1. Authority, chronology and immutable evidence

This record adopts the exact TV3 policy scope below. It does not freeze a shared API/schema, select a physical strategy, authorize implementation or establish deployed DB lineage. Original Messaging FIX3 remains **21 OPEN / 9 CONFLICT / 0 APPROVED**. Architecture D-01–D-10 is a different ID namespace; its historical directions remain intact.

Baseline branch `docs/tv3-day01-close`, local/origin and PR17 HEAD before publication: `727d01879018b5d59d5902534425be2a630e06ba`, after fetch. PR17 Draft/unmerged, reviewers `datcaq`/`Tender0603`, no submitted reviews or issue comments at preflight. PR16 remains `c48f75eacf3b9f428c1f683e4a612d7ec9638d63`; PR9 remains `ea239b9f66a153ac9f848fd089e5d4dd8b8874e4`; neither has a submitted review. Their earlier issue comments/owner documents are not new shared approvals.

Read with [01E.3 preparatory worksheet](TV3_OWNER_DECISION_REVIEW_01E3.md), [01E.2B adoption draft](TV3_CONTRACT_01E2B_OWNER_ADOPTION_DRAFT.md), [ADR D08](ADR-D08-SHARED-FLYWAY-REGISTRY.md), [cards](TV3_SHARED_ARCHITECTURE_CHECKPOINT_DECISION_CARDS.md), [handoff](TV3_SHARED_ARCHITECTURE_CHECKPOINT_HANDOFF.md), [architecture register](DECISION_REGISTER.md) and [historical FIX3 response](TV3_FORMAL_OWNER_RESPONSE_FIX3.md). Their old “pending Phúc” wording describes their preparation date/scope. This record supersedes only the specific unselected TV3 alternatives listed below; original bytes/history are preserved. Publishing the worksheet now retains it as preparatory evidence, not current unselected policy.

### Newly received TV2 01E.2C

Local `D:/Nexa-Chat/NEXA-TV2-01E2C-OWNER-ADOPTION.zip` is now available. Read its complete sole entry, `NEXA-TV2-CONTRACT-01E2C-OWNER-ADOPTION-ADDENDUM.md`.

| Artifact | Computed actual SHA256 | Measured availability / limits |
| --- | --- | --- |
| 01E.2C ZIP | `54A1590F62F4455CA7A57C99F0CF69B50C47F094FCC5AFE920EE4EB5FA76B864` | Local supplied package; no independent expected hash supplied |
| 01E.2C addendum | `7D87CA23EB298FA8BCFCC55216810555B7B131FAD4F86ACF3E62AA8C43095FF9` | 43654 bytes, 1464 physical lines; UTF-8 decoded; read without rewriting/normalizing original |

The addendum is a delivered Markdown fallback with escaped numbering and expanded lines. Its section12 says the author-side write timed out, file was not created then, and output-file integrity was not measured there. These are historical author-side facts. The current recipient package now contains actual bytes with the measured identity above; do not claim recovery of an original author-side saved file or a previously expected output hash.

01E.2C pins TV3 at older `7d125d5`, explicitly claims no fresh live HEAD, closes only the FIX3 report recipient-delivery gap and leaves detailed D09/D10/D25 pending. That accurately predates the current Phúc choices. It attributes supplements to the user's submission, not official PR reviews. It supports discussing request option C, with TV2 preference no intro; it does not prove all owners selected the complete model. No retrospective change to that artifact is made.

Previously verified 01E.2B report SHA256 `1A1DA5E921236F7DDB877899A1D5BD41CAA6CA3279C135C5F9B079297D73A89C`, checkpoint ZIP `E7B55FD61D564E5F246AFB2EB87D2A6E4D9C39D593AB195333864A33909C170D`, and FIX3 author report `34DBAF6ECC895718F864E9030CC728775093774C14005156C7F0DF44B89417E3` retain their exact evidence identities. Source hashes establish bytes, not approval or installed Flyway checksum. Original01A/prior-draft/preliminary fallback provenance and historical scoped signatures remain limited; DB lineage remains UNKNOWN.

## 2. D08 — Alternative A adopted by TV3 within governance scope

**Selected:** one shared Flyway registry, one canonical migration history; TV3 administrative coordinator. TV1 owns Auth/User/Friend/Security; TV2 Conversation/Message/Sequence; TV3 Groups/Membership/Backend Foundation. Each domain owner controls its migrations; cross-domain changes require every affected owner's approval. Database custodian requires separately granted operational authority and is not assumed to be TV3; no named custodian or grant is established by this record.

TV3 coordinates inventory, dependency/order and naming review, proposed reservations, checksum/CI/readiness records and affected-owner routing. This does not grant unilateral TV1/TV2 schema approval or database execution authority. Preserve V1; create no V2 and reserve no version. Actual DB lineage is not verified.

**Rationale:** one auditable history avoids colliding branch-local version claims while preserving domain accountability. Alternatives were independent module histories in one shared schema or unilateral schema approval; neither is selected. A database split would require a separate consistency design and is not authorized.

**Assumptions/conditions:** TV1/TV2 confirm exact governance scope at the publication revision; identify custodian; approve a future serialized reservation protocol before allocating any version. Domain author submits an unnumbered candidate, coordinator checks canonical/reserved inventory, affected owners review dependencies/compatibility, a separately approved atomic registry operation reserves a version, CI and custodian verify readiness, then separately authorized execution is recorded. This describes future protocol, not a reservation performed now.

V1 retained identity from Groups `2e804c033631049683cffd1d7500bb7406658bfa`: blob `03e3057ee5c6bbaaf0696ed271767991c205eb8c`, raw SHA256 `BDE9DCD689CBC86D4966A6C0E1327950AE89D67440E6986C0B054BAB19018E60`. Need sanitized target/operator identity, PostgreSQL/Flyway/config/schema/history location, installed_rank/version/script/checksum/success and drift validation. Fresh/populated upgrade/restart/failure/backup-restore execution evidence belongs to later authorized readiness gates. No shared DB connection here.

**Affected owners:** all three; custodian for operations. Full ADR contains unselected TX and other evidence conditions: governance adoption is not approval of every ADR section or a READY deployment statement.

## 3. D09 — Stable member + fresh period + latest-period-only adopted by TV3

**Selected invariants:** stable member UUID for conversation/user; new nonreused period UUID on every JOIN/REJOIN; role change does not create period. GROUP history requires latest authorized ACTIVE period. No P1 history/cursor/receipt/cache authority is inherited into P2. Floor is committed `J+1`, inclusive; J is finalized committed message watermark captured under the same authoritative transition/allocator boundary, never timestamp or sequence reservation. Removal closes period at committed J for audit and removes current access. Empty eligible history must retain future-floor metadata. No legacy history is inferred from joinedAt/timestamps. DIRECT semantics are excluded and await TV1/TV2.

**Examples of selected policy, not runtime results:** J0 gives floor1; joining at committed J41 gives floor42 even when snapshot H41 is empty. A send42 that commits before join raises observed J to42 and yields floor43; join-first yields floor42 and later committed42 may be visible. Remove at J48 closes P1 audit interval but does not permit inactive reading; rejoin at J53 gives the same member/new P2/floor54 and no P1 authority.

**Rationale/alternative:** fresh periods isolate rejoin and stale multi-device evidence while stable member preserves identity. Full/union historical intervals or implicit backfill were alternatives and are not selected as GROUP default. Old role or authorship does not bypass selected latest-period authority. Empty wire may keep a future-open interval or an attested empty set plus explicit future-floor metadata; exact DTO/snapshot encoding is still TV2 review, not a new approved field.

**Assumptions/conditions:** TV2 must confirm commit-safe allocator, rollback/gap/overflow behavior, period/revision/cursor mapping and empty wire before implementation. Unknown legacy bounds require a separately approved evidence-based/conservative mapping; no guessed historical grant. Physical serialization remains DEFER. Preserve current hidden-P1/absent concealment and frozen origin/key; no automatic P1 resend/rekey under P2. Current source rejects existing rows409 and has no period/history allocator; selection does not claim implementation.

**Affected owners:** TV3 GROUP policy; TV2 sequence/wire/model; TV1 privacy/current authority and separate DIRECT history/consent. Require join/send/remove both-order, rollback, stale P1/P2, account and empty-floor tests later; NOT RUN here.

## 4. D10 — Alternative A, privacy-first, adopted by TV3

**Selected:** sender may see receipts only for recipients still eligible for disclosure; a recipient sees only their own receipts, never another recipient's. No former-member receipt disclosure. Do not silently change the denominator on departure. Exclude author from recipient denominator. No any-read/all-read or aggregate receipt in this phase. Do not expose hidden activity through high-water cursors. READ requires genuine viewport evidence; DELIVERED requires durable eligible device evidence; READ<=DELIVERED. Never merge different accounts, membership periods or history revisions.

Sender disclosure still requires current owning-message/GROUP entitlement and eligible recipient-period disclosure; former member status cannot be disguised as a current recipient after rejoin. Current membership alone never authorizes P1 evidence. Exact viewer/subject projections require TV1/TV2 review; no full high cursor is implicitly permitted. NONE/UNKNOWN/WITHHELD distinctions from the reviewed boundary remain conditions: no evidence is inferred from nondisclosure, and WITHHELD must not reveal an otherwise hidden identity. No extra wire enum is adopted here.

**Rationale/alternative:** privacy-first limits peer/activity leakage. Authorized historical named receipts (Alternative B) are not selected. Any/all, audience-at-send versus current aggregate semantics remain unselected because aggregate is not offered in this phase. Excluding author and forbidding silent denominator change are adopted constraints for any eventual design, not selection of a currently implemented denominator algorithm. Empty audience must not manufacture READ.

**Assumptions/conditions:** TV1 reviews privacy/disclosure/error/hidden activity; TV2 reviews persistence, exact wire, safe per-message/range projection, snapshot and device reconciliation. Holding aggregate unavailable does not waive full P0 acceptance if the project requires it; scope/acceptance remains a shared checkpoint item. No receipt implementation or test PASS is claimed.

**Affected owners:** TV3 policy, TV1 privacy, TV2 receipts/device/wire. Required future evidence includes sender/own/peer/former/P2 privacy matrix, durable/viewport receipt evidence, hidden gaps, account switch and no cross-period/history-revision merging.

## 5. D25 — Controlled invitation lifecycle adopted by TV3

**Selected:** GroupInvitation remains a separate resource; no automatic GROUP membership/history/media/receipt grant. No automatic expiry in this phase. Only current ACTIVE OWNER manages invitations; cancellation is performed by current ACTIVE OWNER; ADMIN receives no new invitation-management authority. Inviter must still be ACTIVE OWNER at accept. Re-invitation uses a new invitation ID. LEFT/REMOVED cannot self-rejoin: a new invitation and fresh period are required. Preserve409 for processed invitation and existing ACTIVE member. Do not change409 to200 without a joint unknown-commit decision.

**Atomicity requirement adopted:** invitation consumption, membership, fresh period, committed J+1 floor and durable facts must share one transaction. Publication occurs only after successful commit and current-authorized admission; physical implementation/ranks are not selected. A stale invitation must not undo removal. Accept-first/remove-second closes the new period; remove-first must invalidate stale invite authority. Cancel-first prevents accept; accept-first does not make cancellation equivalent to member removal.

**Rationale/alternatives:** separate invitation prevents pending access. Automatic expiry/TTL, ADMIN management, self-rejoin, reused invite identity and automatic200 were alternatives and are not adopted. No expiry is a bounded phase selection, not a lifetime retention promise. Retention, declined/cancelled reissue constraints and effective-pending uniqueness still need exact shared lifecycle mapping; reinvitation does not bypass current eligibility/block/removal authority.

**Implementation gap/compatibility:** current code atomically inserts an initial ACTIVE MEMBER and processed409 but checks inviter ACTIVE rather than ACTIVE OWNER. It rejects any existing member, including LEFT/REMOVED; it has no cancel endpoint/expiry/rejoin/period pipeline. CANCELLED enum is not implemented cancellation authority. Future new-invite fresh-period rejoin is the owner's desired change, not current behavior. No implementation is changed now; current runtime behavior remains until separately reviewed implementation.

**Assumptions/conditions:** TV1 supplies current inviter/invitee/account/block eligibility; TV2 confirms period/allocator/wire/durable fact mapping; all approve the transaction protocol, missing-row revoke representation and shared schema. Tests must cover accept/accept, accept/decline, accept/cancel, accept/remove, accept/reinvite, role-change/accept and account-revoke/accept, plus forced rollback at every coupled write. These are NOT RUN here.

**Affected owners:** TV3 lifecycle, TV1 eligibility/privacy/security, TV2 Conversation/period/sequence/publication.

## 6. Physical transaction strategy — explicit DEFER

**TV3 decision: DEFER. Neither A (ordered locks + READ COMMITTED), B (generation fencing + completion barrier), nor SERIALIZABLE is selected.** The earlier ADR describes A as an evaluation candidate, not an approved strategy. This explicit defer governs current interpretation without editing the historical ADR.

Need complete lock/authority inventory, missing-row serialization, cross-instance race evidence, revocation completion semantics, final disclosure barrier, crash/recovery, bounded capacity, idempotency/unknown-commit handling and TV1/TV2/TV3 concurrence. A single-module atomic test or isolation label cannot establish these properties. Invite batch100 is not a group-size cap; DB commit does not recall socket/provider bytes. Durable outbox does not itself solve final recipient authority.

**Rationale/alternatives:** A is easier to explain but can have large lock sets/contention; B adds distributed fencing/barrier and failure-recovery complexity; SERIALIZABLE adds DB abort/retry costs and does not cover external handoff by itself. No candidate has complete assumptions/evidence in current packet. Dependent write implementation remains blocked; prepare proof and return for explicit selection.

## 7. Adoption limits and publication

TV3 policy selection is now explicit; TV1/TV2 signatures and formal checkpoint are not inferred. The [formal response/full30 impact ledger](TV3_FORMAL_OWNER_RESPONSE_01E3.md) separates direct policy adoption, conditional shared adoption and cross-review. The [review request](TV3_SHARED_CHECKPOINT_REVIEW_REQUEST.md) defines requested confirmations/evidence.

Publication is authorized by the same current owner task: normal commit/push to existing Draft PR17, preserve reviewers and append a dated PR summary. No official review approval on behalf of another owner. Existing worksheet/drafts/register remain historical. Actual publication commit and verification are reported externally to avoid a self-referential commit hash.

Day1 Exit remains NOT READY; CONTRACT-01F and Day2 are not authorized. No production source/interface/schema/V1/Word change, migration reservation, shared DB access or runtime test is performed by this record.
