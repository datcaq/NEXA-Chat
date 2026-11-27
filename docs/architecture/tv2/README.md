# TV2 Messaging architecture review materials

**Local publication candidates — OWNER CLEARANCE PENDING — NOT AUTHORIZED FOR PUBLICATION.**

## Purpose and audience

These eight documents are prepared for a separate future Documentation PR in the public NEXA-Chat repository. They support TV1/TV2/TV3 architecture and checkpoint review; they are not a frozen production contract, deployed API specification or runtime acceptance record.

P1B preparation was authorized through human-supplied TV1 and TV3 responses. That attribution is not an independently verified GitHub approval event. Domain clearance and final public release authorization remain pending.

## Exact inventory and reading order

1. [README.md](README.md): purpose, evidence hierarchy, current proposed status and review boundaries.
2. [artifact-manifest.md](artifact-manifest.md): source/derivative provenance and integrity measurements.
3. [MESSAGING_API_V1_DRAFT_FIX3.md](MESSAGING_API_V1_DRAFT_FIX3.md): historical proposed protocol, schemas and acceptance criteria (source F).
4. [MESSAGING_API_V1_DECISION_REGISTER_FIX3.md](MESSAGING_API_V1_DECISION_REGISTER_FIX3.md): all 30 historical decision records (source R).
5. [MESSAGING_API_V1_FIX3_RESOLUTION_REPORT.md](MESSAGING_API_V1_FIX3_RESOLUTION_REPORT.md): historical targeted FIX3 corrections and limitations (source F3R).
6. [shared-decision-resolution-01e2b.md](shared-decision-resolution-01e2b.md): historical cross-owner checkpoint reconciliation (source B).
7. [owner-adoption-addendum-01e2c.md](owner-adoption-addendum-01e2c.md): historical supplemental owner reconciliation and blank scoped sign-off records (source C).
8. [checkpoint-technical-positions.md](checkpoint-technical-positions.md): historical TV2 technical positions and meeting proposals from an exported conversation response (source P).

Only these eight files belong to this candidate publication inventory. Internal review reports and the two later internal checkpoint evidence documents are excluded.

## Historical and current evidence hierarchy

F/R/F3R are FIX3 snapshots; B and C are later historical reconciliations. Their statements about missing inputs, pending adoption, unresolved findings and source state describe their own evidence time. They have not been rewritten into current approval or runtime claims. Do not reopen previously CLOSED findings solely because an earlier snapshot retains pre-closure wording.

C was manually saved from a complete Markdown fallback after the original write-tool timeout. Its original FILE NOT CREATED and missing-output-validation statements remain historical facts. Current candidate verification cannot retroactively make the original write succeed. Flattened tables remain paragraph-form historical records where reconstruction would require assumptions; heading repairs are formatting only.

P was exported from a conversation response with a leading progress statement and four technical cards. It was not an original directly generated Markdown artifact. Its ordered-lock/outbox preference is a TV2 proposal; it does not select a shared physical transaction strategy.

Public immutable owner-response baselines:
- TV1 PR #16: [c48f75eacf3b9f428c1f683e4a612d7ec9638d63](https://github.com/datcaq/NEXA-Chat/tree/c48f75eacf3b9f428c1f683e4a612d7ec9638d63/docs/architecture/tv1).
- TV3 PR #17 formal-review baseline: [89dbc824c76618d278398a96dd4777dcf8ee3f3b](https://github.com/datcaq/NEXA-Chat/tree/89dbc824c76618d278398a96dd4777dcf8ee3f3b/docs/architecture/tv3).

B/C retain their earlier PR17 pin as historical evidence; the later baseline does not overwrite it. No live GitHub HEAD or review metadata has been freshly verified in P1B.

## Proposed current summary S-CURRENT — clearance required

This entire section is a proposed public summary derived from internal CONTRACT-01E.3-R and C1 clarification evidence, supplemented by human-supplied later status communications. TV1/TV2/TV3 must clear its scope and wording before publication; neither internal document is copied into this directory.

Current shared contract status: **CHECKPOINT READY — SCOPED OWNER SIGN-OFF PENDING.**

The internal formal-review disposition is **B — READY FOR CHECKPOINT WITH EXPLICIT CONDITIONS**: 30 decisions reviewed, 7 ACCEPT, 23 ACCEPT WITH CONDITIONS, 0 REQUEST CHANGES and 0 NEEDS EVIDENCE. These bounded review dispositions do not mutate the historical Decision Register.

The user reports that a formal GitHub review was subsequently submitted as **Comment**, not Approve. This later human-provided status is not live metadata verification and does not change the earlier report's historical statement that no review had then been submitted.

C1 records TV2/TV3 technical alignment; TV1 acknowledges it for checkpoint review. Message snapshot S and conversation event snapshot H are distinct domains. JOIN floor is committed J+1 and is evaluated against S and actual eligible committed messages, never against H. An empty eligible interval stays empty, and the future-open floor survives even before an eligible message exists. Rejoin grants no previous-period history authority or receipt cursor; current authorization is independently required. Message history, event replay and account discovery remain distinct.

**Shared scoped sign-off pending. Exact wire approval pending.** Technical alignment introduces no field names, serialization freeze or implementation permission.

C2–C6 remain checkpoint conditions: receipt mapping/privacy; explicit phase/P0 treatment; invitation authority/lifecycle/409 and recovery; complete physical/final-admission protocol; lineage/provenance/custodian evidence. Full ADR availability as a proposal does not establish shared adoption or deployed lineage.

**P0 obligations unchanged. Physical Transaction: DEFER. Actual database lineage: UNKNOWN.** No contract freeze, Day 1 Exit or Day 2 authorization follows.

## Decision Register and technical preservation

Original register: **21 OPEN / 9 CONFLICT / 0 APPROVED**. D01–D30 remain accounted for with their original owners and statuses. PROPOSED and ACCEPT WITH CONDITIONS are not APPROVED.

Preserve concealment/error semantics, immutable account-scoped send identity and original period binding, current authorization/disclosure, message/event/account domains, receipt evidence, durable replay/checkpoints, media/mutation conditions and acceptance criteria. Source verification is not runtime acceptance. Public API examples, including reserved example.invalid hosts and placeholder credentials/cursors, describe proposed protocol requirements, not private deployment endpoints or actual populated secrets.

Historical future tests remain NOT RUN unless separately attributed to a specific earlier evidence scope. No runtime, database, backend, emulator or device test was run for P1B.

## Required owner review

- TV1: Auth and OTP/password policies, JWT/proposed audience, TrustedPrincipal, CredentialProvider, Session V2 and revocation, WSS authentication, Message Requests, Friend/Block, E.164 discovery, authorization, privacy and concealment.
- TV3: Groups, membership/periods, GroupInvitation, Groups receipt policy, Backend Foundation, Shared Flyway Registry, migration governance, lineage and shared transaction dependencies.
- TV2: Messaging REST/WSS, sequence allocation, history/cursors, idempotency, durable events/replay, media, transport lifecycle and publication integrity.
- All affected owners: shared security/authorization/transaction boundaries, S-CURRENT, private-source attribution summaries and the eight explicitly flagged paraphrases in F/P. P1B authorization does not complete this review.

## Provenance and integrity policy

The six source-derived candidates are identified public-safe derivatives, not byte-identical canonical originals. Canonical sources remain internal and unchanged. Neutral source identifiers replace private locations; private Word document filenames/fingerprints and unnecessary personal names are withheld. The seven architecture direction summaries in F and exported progress summary in P need meaning-preservation review. No technical JSON example is modified.

Candidate policy: strict UTF-8 without BOM, CRLF line endings and one final newline. Source P originally lacked a final newline; source measurements describe original bytes. Manifest candidate measurements describe final candidate bytes. Local paths and removed private values are not published. Neutral references to excluded evidence are intentional unavailability, not broken links or a claim that missing evidence was recovered.

The bounded sensitive-content scan is not exhaustive secret-scanner certification. Retained security mechanisms are technical requirements, not deployed operational settings; affected owners must still clear their public disclosure. External URLs are retained as public references, not all freshly verified.

## Publication and implementation boundaries

Local candidates only. Final publication authorization remains pending from TV1 and TV3 after domain clearance. No staging, commit, push, branch/worktree creation, Documentation PR or changes to PR #9/#16/#17 are authorized by this packet. No staged Git blob identity exists.

Future publication identifiers are the separate branch docs/tv2-checkpoint-preparation and destination docs/architecture/tv2/; neither was created in P1B. Future P2 must recheck attributes/core.autocrlf and compare staged blob bytes to the owner-approved candidate bytes. Persistent Git configuration and .gitattributes are unchanged.

No FIX4, full re-audit, canonical/register edit, production/shared-interface/schema/migration change, version allocation, physical transaction selection, contract freeze, Foundation Sync, Day 1 Exit or Day 2 activation.
