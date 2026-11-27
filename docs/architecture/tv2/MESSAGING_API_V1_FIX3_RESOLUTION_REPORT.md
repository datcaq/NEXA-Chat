# TV2-CONTRACT-01C-FIX3 — Minimal Targeted Resolution Report

> **Local publication candidate — identified public-safe derivative; owner clearance and final publication authorization pending.**
> Source F3R is a historical snapshot. Source measurements identify the internal original, not these derivative bytes. See [README](README.md) and [artifact manifest](artifact-manifest.md). Historical findings/status statements below remain dated evidence; later status is separately described in README.


**DRAFT — FOR TV1 / TV2 / TV3 REVIEW — NOT APPROVED FOR PRODUCTION IMPLEMENTATION.**

2026-11-25, Asia/Saigon. Author revision of the exact FIX2 artifacts in response to independent R3 verdict C — REQUEST TARGETED CONTRACT REVISIONS. This is not another independent review or architecture audit.

**R3-01, R3-02 and R3-03 status: REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION.**

No finding is marked CLOSED. Proposed privacy/transport details require owner review. Runtime acceptance remains NOT RUN.

## A. Repository preflight

Target [target repository], distinct from IDE worktree [internal review workspace]. Read-only Git ran in the explicit target directory with GIT_OPTIONAL_LOCKS=0:
- Root: [target repository].
- Branch: feature/messaging.
- HEAD: ea239b9f66a153ac9f848fd089e5d4dd8b8874e4.
- git status --porcelain=v2 --untracked-files=all: no entries.
- git diff --quiet and git diff --cached --quiet: both exit 0.
- Explicit root/branch/HEAD/clean-tree assertions passed.

All three FIX3 destinations were absent, confirmed by Test-Path and directory inventory. No existing artifact was overwritten. No Git mutation or remote operation, production source/test/schema/migration/repository-document edit, Foundation Sync or runtime test was performed.

## B. Eleven-input inventory and hashes

All initial SHA256 values below were calculated on disk and matched before work. The complete R3 report and three complete FIX2 texts were read through Android Studio read_file; the other seven historical hashes were checked against R3 section B. The R3 inventory agrees with those seven exact hashes. No historical artifact was reconstructed or edited.

- MESSAGING_API_V1_DRAFT.md
  - SHA256: 070442D06C21958F8E50F2EF0CE7BDB75866AF034C2DC66078DDBCED47A2A8CE
- MESSAGING_API_V1_DECISION_REGISTER.md
  - SHA256: B05D676D06B93FA42375A5FE2F6E2DDCB2CEAB8CEED556ACAFE44AE32F7E3F83
- MESSAGING_API_V1_INDEPENDENT_REVIEW.md
  - SHA256: B9D7960DA78FEC56D7F172FC3D3EFC6E9D8A6D78677D633A814756E6AE40A4F5
- MESSAGING_API_V1_DRAFT_FIX1.md
  - SHA256: 2E1251CC7C143FEA9E6405C61C1B81C7044333BE034E983E487F785CB050FA4D
- MESSAGING_API_V1_DECISION_REGISTER_FIX1.md
  - SHA256: D193BD1F1B9C1A5210829D3E727FD876E2ABEAB5224734AB4ED49301B5E24675
- MESSAGING_API_V1_FIX1_RESOLUTION_REPORT.md
  - SHA256: 973AA9E9181C2A3901C87A7640239973455068DFF95B0E1C5E6ADB3B576DC656
- MESSAGING_API_V1_INDEPENDENT_REVIEW_R2.md
  - SHA256: 15BE814FDAFBBD7B0E1BCA1817287056CFAF24D9006D2B6E31B2EFC40B6E906C
- MESSAGING_API_V1_DRAFT_FIX2.md
  - SHA256: 9E361198B1DDFCCC4B31D703D241438C44A931B3FFB7075AB4A49E83DCDCBD0C
- MESSAGING_API_V1_DECISION_REGISTER_FIX2.md
  - SHA256: C26FB4DBF3ECC9F5C8877010BEBB7ABCE8BAE3C25D20E9B2676CF2451484628C
- MESSAGING_API_V1_FIX2_RESOLUTION_REPORT.md
  - SHA256: 575905159165A632E34DE68CA6326A6097E0D56435C5936E8A09E474CE97DF3C
- MESSAGING_API_V1_INDEPENDENT_REVIEW_R3.md
  - SHA256: 70F7F5A30BE92B34E8015E501CA31E62E29DB2B6292C215B6AF0DA1EC009DDBF

All eleven are immutable inputs, including the R2/R3 review and FIX2 author report. R3 section M remains the preserved Day 1 planning triage; this revision neither rewrites it nor authorizes Phase 2.

## C. Output artifacts and hashes

Exactly three new UTF-8 Markdown artifacts in [internal contract artifact collection]:
- MESSAGING_API_V1_DRAFT_FIX3.md — complete revised draft, not a patch.
  - 227587 bytes; 2463 physical lines; UTF-8 without BOM. SHA256: 3E7B57B740BA448C257ECA3445C1075D0B85C665055C69E2E4FB4F7843E9F9CA.
- MESSAGING_API_V1_DECISION_REGISTER_FIX3.md — complete revised register, not a patch.
  - 128705 bytes; 1117 physical lines; UTF-8 without BOM. SHA256: 0E41865166D33ED5E3F06455E4C4BE477814B36A96ADFE9CE7816BFED1CD9B2C.
- MESSAGING_API_V1_FIX3_RESOLUTION_REPORT.md — this targeted author report.
  - Its final byte size, line count and SHA256 are supplied in the delivery handoff after readback. Its own digest is kept outside the file to avoid a recursively changing self-hash.

Input files are untouched. No helper scripts, schemas, tests or additional deliverable files are created. IDE infrastructure tool logs are not authored contract artifacts.

## D. R3-01 contradiction and selected candidate

FIX2 §5.2 L456 required missing and hidden committed keys to be indistinguishable. Branch C at L464 concealed hidden committed P1 history with404, while E at L466 permitted409 MEMBERSHIP_CHANGED for a retired uncommitted P1 key; walkthrough L474 and acceptance L2349 repeated that discriminator. Identical current P2 authority, original P1 header and request could therefore reveal hidden commitment through status.

FIX3 selects the user-specified candidate: in the paired undisclosable old-period context C and E use the same canonical concealed404. This is a **PROPOSED contract correction**, not an approved TV1/TV3 privacy policy. The profile refines the existing ErrorResponse and error codes; no new endpoint, control, domain event, schema or API operation is needed.

Proposed observable profile:
- HTTP404; code NOT_FOUND.
- Safe default message "Resource not found."; identical approved localization for the same request locale.
- details=null and retryAfterSeconds=null, consistently emitted keys.
- Same relevant header names/semantic values: Content-Type application/json; Cache-Control no-store; no Retry-After or commitment/reconciliation hint header. Common TV1 security headers use the same key-independent policy.
- requestId and correlation-only header identifiers may differ independently of commitment.
- Same terminal concealed-denial classification for the attempt: no automatic resend, refresh triggered by404, rekey, origin-period rewrite, P1-to-P2 migration, ACK/SENT inference or false rollback conclusion.
- No ACK, MessageDTO, hidden committed identity, equality result, sequence, version or reconciliation hint.

The canonical message/header choices are within this proposed correction and still require TV1 privacy/security and TV3 membership/history cross-review. No timing-side-channel elimination, constant-time execution, response-size equality or runtime indistinguishability is claimed.

Frozen clientMessageId, payload, originMembershipPeriodId, account UUID/epoch and operation fences remain unchanged. Authorized credential refresh preserves these originals. A concealed response preserves account-scoped uncertainty under existing policy; it is not evidence that an earlier attempt failed to commit. No automatic retry policy is added.

## E. Before/after branch A–G matrix

"Before" below is historical FIX2 behavior, not normative FIX3 permission. All after outcomes remain proposed and require current authorization.

| Branch | FIX2 before | FIX3 after | Preserved invariant |
| --- | --- | --- | --- |
| A Same-period committed identical | Authorized200 same creation ACK/current message | Unchanged | One identity; no new message/event/binding, READY recheck or recalled-body restoration |
| B Removed requester | Concealed404 | Same canonical concealed profile for committed/absent pair | No protected ACK/message or new authority |
| C P2 cannot see committed P1 message | Concealed404 | Canonical404 equivalent to E across status/code/message/details/headers/recovery | Current send permission is not old-history visibility; key stays reserved |
| D Explicit approved visible-history exception | Conditional authorized200 identical | Unchanged conditional200 and original identity; not blanket old-period rejection | Explicit exception plus current action/full ACK/current MessageDTO authorization |
| E Retired uncommitted P1 pending send | Permitted409 MEMBERSHIP_CHANGED or404 | Canonical404 same as C; zero new message/event/binding writes | No P2 authority inheritance, automatic resend/rekey or origin rewrite |
| F Changed payload/path/origin under committed key | Authorized409 conflict; hidden404 | Unchanged; hidden case uses canonical profile | Compare/disclose conflict only when current full disclosure is allowed |
| G Approved purge or unexpected original loss | Authorized409 reconciliation or503 unavailable; hidden denial | Same authorized behavior, canonical concealment before hidden reconciliation | Permanent identity protection; no hash-only success or duplicate recreation |

A genuinely new current-period key still follows existing new-send checks. D's approved exception is neither selected nor disabled. E does not inherit D's committed result because no committed evidence exists. G's retention/reservation policy is not approved by this revision.

MEMBERSHIP_CHANGED audit:
- Removed its retired-uncommitted-send permission from §5.2 branch E, walkthrough and §16.2 expected result.
- Updated §14 NOT_FOUND applicability and client action to reference the paired profile.
- Updated §14 MEMBERSHIP_CHANGED applicability and explanatory mapping: independently disclosable period/history/visibility preconditions remain; knowing a retired header alone does not permit a key discriminator.
- Retained receipt/read/delivered fencing, snapshot/traversal/resume revisions, stale-stream controls and resync diagram uses. These are independently disclosable membership/capability preconditions, not send-key comparison outcomes; removed access remains concealed.
- Any other retained use must give the same semantic outcome for otherwise identical absent/hidden-key requests where commitment is undisclosable; if not possible, use concealment. There is no remaining normative E409 branch or old walkthrough discriminator.

No error is removed globally. The 26-code registry is unchanged in membership; only two applicability rows and the related explanation change.

## F. Paired observable-response validation

Existing §16.2 R2-02 acceptance is corrected; a seven-row targeted extension uses existing IT04/IT05/IT12/IT15/IT16 and AT05/AT06/AT08/AT18/AT19/AT22/AT23. No new acceptance IDs or JSON blocks are added. Every new runtime schedule is NOT RUN.

Document schedules verified:
1. Otherwise identical K/origin P1 requests under send-eligible P2 without old-history exception: hidden committed versus absent/uncommitted ->same canonical404 and zero new message/event/binding writes in both worlds. Existing hidden committed rows remain; "zero" means no new writes, not erasing history.
2. Changed payload with the same retired P1 header/current authority ->same404 before equality/conflict disclosure. The paired probe does not change the header into current-period new-send authority.
3. After successful permitted credential refresh ->same originals and404 profile.404 itself never triggers another refresh. Changed-payload-after-refresh pair is also checked.
4. Removed requester: committed/absent ->same concealed404, no protected response/new send.
5. Fully authorized prior-period exception: D preserves200 same original ACK for identical; F preserves409 IDEMPOTENCY_CONFLICT for changed request. Both require complete current disclosure and no new writes.
6. Retired uncommitted pending send: E404, zero new writes, retained original key/period/uncertainty; no false SENT or P2 inheritance.
7. Purge/loss: approved authorized purge409 RECONCILIATION_REQUIRED and unexpected authorized loss503 UNAVAILABLE remain. Hidden purge/loss and absent retired P1 conceal identically; permanent committed identity is not freed.

Actual checker work, in memory only:
- Parsed HTTP status/code/default message from the new canonical normative paragraph and verified the profile body against unchanged ErrorResponse schema.
- Constructed seven paired semantic comparisons: base pair, changed payload, refreshed credentials, changed payload after refresh, removed requester, hidden approved purge, hidden unexpected loss. All compare equal after excluding only independently generated correlation identifiers.
- Checked oracle sensitivity with eight deliberately distinct outcomes:409 status; MEMBERSHIP_CHANGED code; message hint; details hint; retryAfterSeconds hint; Retry-After header; changed recovery classification; hidden message identity. Each is rejected as unequal.
- Inspected actual C/E rows and conditional A/D/F/G guards separately; the comparator is a document fixture/proposed response oracle, not production key-lookup or request-dispatch code.
- Zero new writes, no rekey/origin mutation and no ACK/SENT inference are explicit document constraints, not measured database/client effects.

Forbidden404-versus409/code/details/header/recovery differences are now rejected by the specified oracle. Timing, transport termination, distributed authorization and actual DB/Android effects remain future evidence.

## G. R3-02 schema trace table correction

Removed only FIX2's unintended blank L2422 between RevokeRequest and MembershipBaseline. No trace row content, ownership, decision or gate changed. MembershipBaseline, ConnectionReady, SubscriptionClose and SubscriptionClosed are now actual rows of the existing §17.1 table.

Validation checked contiguous Markdown blocks, a header and valid delimiter row, and every data row's arity. The one repaired table has all47 unique schema mappings, identical in content/order to the original43 table rows plus four detached rows. No schema is missing or duplicated.

FIX2 had72 pipe blocks:71 valid tables plus one four-row orphan. FIX3 has72 valid tables and no orphan: repair merges the detached block, while the directly required paired-oracle table adds one valid table. Matching pipe arity alone was not treated as structural success. No visual renderer is claimed.

Status: **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION**.

## H. R3-03 D27 registry correction

Changed only the stale D27 recommendation and dependency rationale from the historical three-control count to six; both now name the complete existing union:
- DeliveryComplete.
- AppliedReport.
- ControlError.
- ConnectionReady.
- SubscriptionClose.
- SubscriptionClosed.

Ten domain event types remain. D27 remains CONFLICT. No new control, routing behavior, schema or approval is introduced. Existing §8.1 lifecycle is unchanged. A D27 note traces R3-02/R3-03 to §17.1/registry and existing acceptance cases. Current Draft/Register scans find zero stale three-control normative references; historical FIX2/report/R3 descriptions of the old count remain immutable evidence.

Status: **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION**.

## I. Exact changed sections and regression preservation

Actual line-by-line FIX2-to-FIX3 comparison was calculated from complete texts. A reverse application of every recorded edit reconstructed each FIX2 input exactly, proving no unlisted text changes.

Draft:14 removed lines,35 added lines (line diff; replacements count on both sides),16 diff hunks:
- Title and §1: revision identity, exact FIX2/R3 provenance, current links, authorized scope/status and eleven-input report reference.
- §5.2: scoped disclosure rule; canonical semantic/Android/precondition paragraphs; C/E rows; deterministic walkthrough.
- §14: NOT_FOUND and MEMBERSHIP_CHANGED applicability/client behavior; related safe mapping paragraph.
- §16.2: existing R2-02 expected/forbidden cells and seven-row paired schedule extension under existing IDs.
- §17.1: one blank-line deletion only.
- §18: current revision approval-column label and stop/evidence dependencies.

Register:6 removed lines,19 added lines,12 diff hunks:
- Title/header links and revision/evidence limitations.
- D03/D09/D14/D18/D20/D21: two short R3-01 trace/responsibility lines each, preserving original metadata and historical FIX2 fields.
- D27: two stale counts corrected and one targeted trace note.
- Approval-record paragraph: independent R2/R3 are inputs, not FIX3 self-closure; preserve R3 Day 1 planning and no automatic further review.

Exact diff ranges (1-based; insertions have no old removed lines):
- Draft: FIX2 L1 -> FIX3 L1.
- Draft: FIX2 L7 -> FIX3 L7.
- Draft: FIX2 L9 -> FIX3 L9.
- Draft: FIX2 L456 -> FIX3 L456.
- Draft: FIX2 insertion before L460 -> FIX3 L460–467.
- Draft: FIX2 L464 -> FIX3 L472.
- Draft: FIX2 L466 -> FIX3 L474.
- Draft: FIX2 L474 -> FIX3 L482.
- Draft: FIX2 L2164 -> FIX3 L2172.
- Draft: FIX2 L2168 -> FIX3 L2176.
- Draft: FIX2 L2187 -> FIX3 L2195.
- Draft: FIX2 L2349 -> FIX3 L2357.
- Draft: FIX2 insertion before L2356 -> FIX3 L2364–2377.
- Draft: FIX2 L2422 -> FIX3 deletion at boundary L2444.
- Draft: FIX2 L2434 -> FIX3 L2455.
- Draft: FIX2 L2442 -> FIX3 L2463.
- Register: FIX2 L1 -> FIX3 L1.
- Register: FIX2 L7–8 -> FIX3 L7–8.
- Register: FIX2 insertion before L131 -> FIX3 L131–132.
- Register: FIX2 insertion before L320 -> FIX3 L322–323.
- Register: FIX2 insertion before L507 -> FIX3 L511–512.
- Register: FIX2 insertion before L658 -> FIX3 L664–665.
- Register: FIX2 insertion before L721 -> FIX3 L729–730.
- Register: FIX2 insertion before L774 -> FIX3 L784–785.
- Register: FIX2 L953 -> FIX3 L965.
- Register: FIX2 L965 -> FIX3 L977.
- Register: FIX2 insertion before L1003 -> FIX3 L1015.
- Register: FIX2 L1100 -> FIX3 L1113.

Draft sections2/3/4/6/7/8/9/10/11/12/13/15 are text-identical to FIX2. In particular:
- No S/H/R/A, MembershipBaseline, WSS ready/subscription lifecycle, literal-null schema or TTL typing redesign.
- No frozen-payload/post-commit ACK, account discovery, receipt period, media retry or transaction-strategy change.
- No Host ownership, bounded-execution, hidden-tab READ/TYPING, account scope or full P0 requirement change.
- All47 field tables and all50 JSON examples are exactly unchanged.
- All30 existing decision status/owner/cross-review/baseline/activation fields compare unchanged.
- Original16 finding identities/severities/historical classifications remain in preserved evidence (11 CONFIRMED,3 PARTIAL,2 NEEDS EVIDENCE; eight P1/eight P2).
- R3 dispositions remain: R2-01/R2-03/R2-05 design-resolved/owner-approval-pending; R2-04 document-defect-resolved. R2-02's historical partial disposition is not overwritten by this author's proposal.

No API surface expansion or unrelated correction was necessary.

## J. Decision and acceptance coverage

D01–D30:30 unique decisions;21 OPEN,9 CONFLICT,zero APPROVED. Existing owners, cross-reviewers and baseline/activation gates compare exactly equal to FIX2. New notes explicitly identify TV2 as correction owner with TV1 privacy/security and TV3 membership/history cross-review; they do not reassign D03's or any other existing decision ownership.

AT01–AT30 and IT01–IT16:46 unique definitions, unchanged in identity; no duplicate IDs. New schedules extend existing cases with runtime NOT RUN. Every single acceptance/decision reference in the revised draft resolves to an existing identifier. All47 schema-to-gate mappings are now inside a valid table and retain the original rows.

R3 Day 1 triage is preserved by reference to the immutable R3 report section M at its verified hash. It is planning evidence only. No OPEN/CONFLICT becomes APPROVED, no finding is CLOSED, and no Phase 2 authorization follows.

## K. Actual document validation results

The in-memory table/fixture checker from the preceding review was rerun against actual FIX3 texts; counts were recalculated rather than forced to match:
- 47 named schemas;259 fields.
- 50 existing JSON examples, all syntactically parseable:44 positive accepted,6 negative rejected.
- 16 REST operations;10 domain event types (including two nondurable typing types);6 controls;26 errors.
- 30 decisions;46 acceptance definitions.
- No schema/JSON count change because paired schedules are a Markdown oracle table rather than additional JSON examples.

Required/nullability/type probes:257 required-key deletion negatives,229 forbidden-null negatives,259 wrong-type negatives =745 expected rejections,zero unexpected results. Also checked two optional-key deletion acceptances and30 nullable acceptances:777 total field mutations. Literal-null accepts only present null; missing/0/numeric1/string1 reject. All47 schemas are reached by positive/nested examples.

Reran14 primitive negatives: UUID variant/case, decimal leading zero/overflow/sign/positive-zero/numeric wire type, integer zero/overflow/fraction, UTC-Z, empty token, enum case and null array member. All reject.

Reran19 selected cross-field negatives: ACK identity, tombstone body, read<=delivered, baseline requester/ACTIVE/completeness, H/period/role/mode/token binding, coverage order, receipt period, START/STOP expiry, interval overlap, projection mode/version and account upsert. All reject. List extra message watermark and missing MessagePage S remain the intended two page negatives; no generic additive-response ban is invented.

Paired-oracle checks: seven semantic equality pairs and eight negative sensitivity probes, as section F. Profile body conforms to ErrorResponse. Exact required/nullability/type tables and JSON fixture identity against FIX2 also pass. These checks cover documented selected constraints; they are not exhaustive standards certification, generated serializer tests or cryptographic token/clock/authority proofs.

Structural checks: Draft72 valid Markdown tables,zero orphan/arity failures; Register0 tables. Report structure is checked at readback. All47 trace rows have a proper header/delimiter. Six control names reconcile to the existing registry/schemas; no stale normative three-control count remains. MEMBERSHIP_CHANGED references were reviewed as scoped in E. No new code or machine error is emitted by the proposal.

No Gradle, emulator, Android device, PostgreSQL, backend, provider or runtime acceptance tests ran. No new independent review was executed automatically. Passing author document checks is not runtime PASS or independent closure.

## L. Remaining owner dependencies

Preserved and unresolved:
- TV1 PR #16 Security & Transport Addendum V1 compatibility, reported HEAD acf30030a549c3bf0cbb501a68e376430710beeb. Exact PR source artifacts are not supplied/inspected in this task; cross-review is separate. No bearer WSS/expiry/sid revocation/credential replay/error/cancellation/timeout approval is inferred.
- TV2-R13-01 and CORE-04A FIX1 pending exact new revision and independent evidence.
- TV3 latest-join/ACTIVE architecture scope versus exact membership boundary, exceptions, DIRECT periods, history/receipt privacy and audience decisions.
- Shared Backend Foundation, global transaction/admission fence/fanout strategy and bounded capacity.
- Flyway deployment history/checksums and reserved version before dependent migrations; no history rewrite or version guess.
- JWT audience conflict nexa-api versus nexa-chat-api.
- Historical01A/previous-draft/full-ledger evidence limitations; no missing approval reconstructed.
- Retention, permanent identity protection/purge, privacy, clock/TTL/device, transport and resource-limit policies.

The proposed default English safe message, localization equivalence and header profile also remain owner-review candidates. No production implementation, shared contract freeze, Foundation Sync or Phase 2 begins.

## M. Final integrity and verdict

All three new outputs are read back via Android Studio and compared exactly to authored text. On-disk sizes, physical line counts, SHA256 and strict UTF-8 validity are measured for delivery. Draft/Register measurements in C are taken after saving. This report's final hash is external to avoid self-reference.

All eleven input hashes are recalculated and compared after output creation; required repository root/feature-messaging/HEAD/clean working tree/index/untracked state is rechecked. The final delivery tool evidence establishes those checks; this paragraph alone is not proof of execution. Exactly three new contract artifacts are added to the initial eleven.

**FIX3 READY FOR TARGETED VERIFICATION AND FORMAL OWNER REVIEW WITH EXPLICIT OWNER DEPENDENCIES.**

Each of R3-01/R3-02/R3-03 remains **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION**. Recommended next step is separately authorized targeted verification of these exact FIX3 hashes and scoped TV1/TV3 formal owner review, including PR #16 compatibility as a separate milestone. Stop after FIX3 delivery; do not execute that review automatically or start implementation.
