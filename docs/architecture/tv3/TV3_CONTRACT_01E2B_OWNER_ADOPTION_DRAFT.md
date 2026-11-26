# TV3 CONTRACT-01E.2B — Owner adoption draft

26/11/2026, Asia/Saigon. Prepared by Codex for Hoàng Phúc; publication is not his signature.

**PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION.** Ready for checkpoint discussion, not contract freeze, Day 1 Exit, CONTRACT-01F or Day 2 implementation. This is a bounded adoption package, not another general architecture audit or TV2's 01E.2C addendum.

## 1. Executive summary

Reported TV1/TV2 alignment now supports TV3 administering the shared Flyway registry, affected-owner schema review, route-specific Unicode/pagination compatibility and a separate Message Request resource. This removes a direction-level disagreement; it does not close D08/D22 or transfer domain ownership. Recommend separate, no-intro Message Requests with chat consent independent of friendship, subject to exact TV1/TV2 adoption and wire design. Groups membership remains independent.

Recommend bringing a concrete ordered-lock transaction candidate, latest-period GROUP history with a committed J+1 floor, explicit receipt privacy alternatives and invitation-to-fresh-period acceptance to the checkpoint. Each remains a proposal. Current Groups implements separate invitations and atomic initial acceptance, but not membership periods, rejoin, message sequence/history, receipt ACL or the proposed shared transaction strategy.

The original FIX3 register is unchanged: **30 decisions; 21 OPEN, 9 CONFLICT, 0 APPROVED**. Previous TV3 dispositions remain historical recommendations: 20 ACCEPT WITH CONDITIONS, 8 NEEDS EVIDENCE, 2 REQUEST CHANGES. New alignment is recorded alongside them, not substituted for owner signatures.

## 2. Evidence and revision ledger

### Repository / remote pins verified before editing

| Source | Exact revision | Meaning |
| --- | --- | --- |
| main | `8b730dccc171c6d55fd8ae127448ed2b6c5f23a4` | Shared main baseline |
| PR17 / docs/tv3-day01-close | `7d125d53a344c8c940acd8cca0090bfb5d205186` | Pre-edit local and remote HEAD; OPEN, Draft, unmerged; reviewers datcaq and Tender0603 |
| PR16 / TV1 docs | `c48f75eacf3b9f428c1f683e4a612d7ec9638d63` | OPEN, Draft, unmerged; published owner-policy documents |
| PR9 / Messaging | `ea239b9f66a153ac9f848fd089e5d4dd8b8874e4` | OPEN, Draft, unmerged; external contract files are not inferred to be in this branch |
| PR6 / Groups | `2e804c033631049683cffd1d7500bb7406658bfa` | Historical/current Groups source inspected, not merged main |
| PR13 / CORE-04A FIX1 | `5f8c081c6fadeb135fea8adcc9f22f493c13dce7` | Exact-head TV2 approval and TV2-R13-01 closure recorded in prior TV3 response; not production integration acceptance |

[Previous TV3 formal response](TV3_FORMAL_OWNER_RESPONSE_FIX3.md) and its references remain immutable historical evidence. Its Git blob at the pre-edit pin is `155ba48af0d4a74161421c482d1858e67280ada4`. [TV1 formal response](https://github.com/datcaq/NEXA-Chat/blob/c48f75eacf3b9f428c1f683e4a612d7ec9638d63/docs/architecture/tv1/01-formal-owner-response-confirmed.md), [narrow human policy confirmation](https://github.com/datcaq/NEXA-Chat/blob/c48f75eacf3b9f428c1f683e4a612d7ec9638d63/docs/architecture/tv1/05-human-owner-confirmation-confirmed.md), source reconciliation and TV1 handoff at the same pin distinguish business confirmation from shared contract approval. The formal response's older draft heading does not cancel its narrowly scoped confirmation addendum or approve all 30 decisions.

### Artifact integrity / availability

Raw SHA256 values below describe file bytes, not installed Flyway checksums or signatures.

| Artifact | SHA256 | Availability / comparison |
| --- | --- | --- |
| NEXA-TV2-01E2B-CHECKPOINT.zip | `E7B55FD61D564E5F246AFB2EB87D2A6E4D9C39D593AB195333864A33909C170D` | Local ZIP; MATCH user-supplied expected hash |
| NEXA-TV2-CONTRACT-01E2B-SHARED-DECISION-RESOLUTION.md | `1A1DA5E921236F7DDB877899A1D5BD41CAA6CA3279C135C5F9B079297D73A89C` | 99785 bytes / 310 lines; read; MATCH supplied expected hash |
| MESSAGING_API_V1_FIX3_RESOLUTION_REPORT.md | `34DBAF6ECC895718F864E9030CC728775093774C14005156C7F0DF44B89417E3` | Newly received, 24584 bytes / 264 lines; read; computed actual MATCH explicit historical Handoff/IR4 reference |
| FIX3 Draft | `3E7B57B740BA448C257ECA3445C1075D0B85C665055C69E2E4FB4F7843E9F9CA` | Previously supplied and reviewed; unchanged reference |
| FIX3 Register | `0E41865166D33ED5E3F06455E4C4BE477814B36A96ADFE9CE7816BFED1CD9B2C` | Previously supplied and reviewed; unchanged reference |
| R4-LITE | `1C5F1F3152D4A37A0C6FE19A468FC2CFC45834C1485E4D0789D94C51F97E6FDD` | Previously supplied targeted independent document review |
| Original TV3 review handoff | `525E0467B3F5C5D5D252E7918574B9B859773A8430017A3E1581130C8798E87A` | Previously computed inventory; not a signed approval |

Checkpoint ZIP was extracted outside the repository under `D:/Nexa-Chat/test-artifacts/tv3-checkpoint-input`; originals were preserved. Its two files are supplied external inputs, not invented Git URLs. Newly receiving the FIX3 author report narrows the delivery gap; it does not make the earlier statement that it was unavailable false. The author report predates R4: its R3 findings remain author-proposed there; later R4 records R3-01 design resolved / owner approval pending and R3-02/03 document defects resolved, within targeted scope only.

Still missing: independently recoverable original 01A/previous draft and scoped historical approval chain; exact preliminary 01E.2A fallback text; complete historical input set for independently repeating all FIX2-to-FIX3 checks. C/X cross-review documents referenced by 01E.2B are not in this two-file ZIP. Do not claim their complete readback. Database lineage remains **UNKNOWN**.

### Confirmation matrix and provenance

| Owner | Published / verified evidence | Latest reported alignment | Scope still pending |
| --- | --- | --- | --- |
| TV1 / Quốc Đạt | PR16 c48f75e documents: OTP registration + password login, privacy, registry support; 25 AGREE WITH CONDITIONS / 5 NEEDS EVIDENCE | User's master task relays “TV1 RESPONSE — CONTRACT-01E.2B / SHARED ARCHITECTURE CHECKPOINT”: route compatibility, separate no-intro request, chat consent independent of friendship | Detailed Auth/security, DIRECT privacy, affected-owner and shared sign-off |
| TV2 / Tiến Đạt | Supplied 01E.2B report at exact hash, PR9 ea239b9 metadata | User relays “TV2 RESPONSE — OWNER ADOPTION RECONCILIATION / CHECKPOINT PREPARATION”: coordinator, affected-owner review, route compatibility, option C and current authorization after accept | 01E.2C delivery, physical/wire/sequence mapping and signed selections |
| TV3 / Hoàng Phúc | Published 7d125d5 formal response authored by Codex, not a human signature | This four-document package prepares choices | Explicit Phúc adoption, especially D09/D10/D25, full ADR, physical strategy and cross-domain schema |

Fresh PR9/16 review and inline-comment lists were empty; PR9 issue comments were empty; PR16 had two older review-request issue comments, not these latest response texts. Therefore the latest relayed confirmations are attributed to the user's request, not fabricated GitHub comments. Published TV1 documents corroborate narrower directions. Earlier 01E.2B describes intro/friendship and some concurrence as unresolved: latest reported alignment is a dated delta, not a retroactive edit.

**01E.2C dependency:** no matching addendum found in local filename search, all fetched origin branch trees, matching Git history paths, or inspected PR9 metadata/comments. An unpublished, differently named or externally held document may still exist. Continue this TV3 package; do not duplicate TV2's deliverable or invent its conclusions. TV2 must provide exact bytes/pin and adoption record.

## 3. D08–D25 assessment

| Decision | Current checkpoint assessment / next evidence |
| --- | --- |
| D08 | Aligned coordinator direction; [full ADR proposal](ADR-D08-SHARED-FLYWAY-REGISTRY.md), physical strategy and unknown target lineage prevent closure |
| D09 | Fresh periods / current ACTIVE direction; exact J+1, empty interval, legacy mapping and DIRECT interaction await Phúc and affected owners |
| D10 | Period receipts; choose A privacy-first or B authorized historical disclosure, audience and denominator explicitly; no aggregate selected |
| D11 | Native bearer WSS direction; TV1/TV2 must prove expiry/sid/refresh/revoke/current authority; no Groups implementation approval |
| D12 | Durable replay and transactional facts required; outbox is proposed, S/H/R/A and current disclosure remain distinct |
| D13 | Final committed sequence must serialize with join/remove; a reservation or timestamp is not committed J |
| D14 | Keep frozen original identity and canonical hidden/absent 404; no P1-to-P2 rekey or inference of rollback from concealment |
| D15 | Preserve opaque snapshot/history-bound traversal; Groups page numbers cannot imply Messaging replay coverage |
| D16 | Durable device delivery evidence, no socket-flush shortcut; period/device merge and physical evidence pending |
| D17 | Existing Groups UUID lastReadMessageId cannot silently become sequence cursor; explicit model/adapter needed |
| D18 | Current ACL at metadata/download/use; old period and pending request grant no media authority |
| D19 | Reply/edit/recall use current authority/version; mutation window still candidate, no extra routes approved |
| D20 | Retry only known transient rollback with frozen key; unknown commit retains uncertainty; concealment is terminal, not refresh trigger |
| D21 | Partition Room/draft/outbox by UUID/epoch/period as relevant; no P1 cursor inheritance or old-account apply |
| D22 | Route compatibility direction aligned; document UTF-16 versus code points, whitespace and page/cursor units; tests remain NOT RUN |
| D23 | Author report newly received and verified; other historical evidence/approval and DB lineage still incomplete |
| D24 | ACL at dispatch/result/use, versioned cache and account isolation; provider/privacy/retention selection pending |
| D25 | Separate GroupInvitation already implemented; fresh-period rejoin, expiry/cancel policy and global accept transaction remain proposed |

Detailed choices and acceptance schedules are in the [nine decision cards](TV3_SHARED_ARCHITECTURE_CHECKPOINT_DECISION_CARDS.md). Every acceptance schedule is future evidence, not a test result.

## 4. Auth and Message Request boundaries

TV1 owns OTP phone verification during registration, safe password enrollment, E.164 + password login and separate recovery proof. UUID remains identity; recycled phone possession alone is not account recovery. JWT `sid` is a server session identity; Android epoch is a local account fence. Session V2 signatures remain unchanged. CredentialProvider/refresh/revoke belong to TV1; WSS authentication is TV1/TV2 work. `nexa-api` is TV1's audience choice pending shared concurrence; access 15 minutes / refresh 30 days / skew 30 seconds are pilot candidates only. Issuer/key custody/hash cost/reset/revoke details remain open.

TV3 consumes trusted UUID, checks current GROUP authority, logically invalidates before asynchronous physical cleanup, rejects stale callbacks and isolates account state. Local epoch checks and a group JWT subject parse do not demonstrate shared server sid revocation or atomic production admission.

Recommend separate Message Request option C, initially no intro and no ordinary Message/media/READ/TYPING/full DIRECT before acceptance. Acceptance grants chat consent independently of friendship, subject to TV1 policy and TV2 atomic implementation. Recheck current account/block/consent at acceptance, commit and final disclosure. Canonical UUID pair uniqueness survives duplicate/crossed requests; uniqueness alone grants no access. Keep request idempotency distinct from frozen Message keys. No automatic friendship on accept or new consent on unblock/unfriend.

A Message Request creates neither GroupInvitation nor GROUP membership and cannot mutate GROUP state or bypass Group ACL. TV1 owns eligibility/block/privacy/abuse policy; TV2 owns request resource, wire, runtime and canonical DIRECT creation; TV3 reviews shared schema/transaction effects and protects GROUP boundaries.

## 5. Full D01–D30 impact mapping

Role is TV3 review scope, not a reassignment of original domain ownership. “Conditions”, “needs evidence” and “request changes” below are prior Codex recommendations; **all human adoption remains pending**. Original status column is copied unchanged. No row is APPROVED.

| ID | Subject | Original status | TV3 role | Prior TV3 disposition | Checkpoint impact / responsible next step |
| --- | --- | --- | --- | --- | --- |
| D01 | API prefix | CONFLICT | Shared | ACCEPT WITH CONDITIONS | /api/v1 direction; affected owners sign route inventory |
| D02 | DTO naming/nullability | CONFLICT | Shared | ACCEPT WITH CONDITIONS | TV2 wire + TV3 Groups projection/adapter; explicit literal-null/enums |
| D03 | 403/404 errors | OPEN | Cross | ACCEPT WITH CONDITIONS | TV1 concealment, TV2 response profile; old-period parity evidence |
| D04 | CredentialProvider/JWT | OPEN | Cross | NEEDS EVIDENCE | TV1 password inventory/sid and shared audience; TV3 decoder compatibility |
| D05 | Friend/block | OPEN | Cross | NEEDS EVIDENCE | Separate requests change friends-only assumption; TV1/TV2 consent policy |
| D06 | DIRECT retained history | OPEN | Cross | NEEDS EVIDENCE | TV1 decides block/unfriend/history; no GROUP-floor transplant |
| D07 | Host/resource hooks | OPEN | Shared | NEEDS EVIDENCE | TV1 composition, TV2 runtime, TV3 caller; exact PR13 closure != integration |
| D08 | Backend/schema/registry | OPEN | Shared | REQUEST CHANGES | Coordinator direction aligned; ADR/transaction/lineage await all affected owners |
| D09 | Join/rejoin bounds | OPEN | Direct | ACCEPT WITH CONDITIONS | Phúc exact periods/J+1; TV2 committed allocator; TV1 DIRECT privacy |
| D10 | Receipt audience/aggregate | OPEN | Direct | ACCEPT WITH CONDITIONS | Phúc selects A/B and denominator; TV1 privacy, TV2 representability |
| D11 | WSS auth renewal | OPEN | Cross | NEEDS EVIDENCE | TV1/TV2 sid/expiry/current-ACL transport proof |
| D12 | Durable replay | OPEN | Shared | ACCEPT WITH CONDITIONS | TV2 log/replay + TV3 authority facts/outbox atomicity |
| D13 | Sequence watermark | OPEN | Cross | ACCEPT WITH CONDITIONS | TV2 commit-safe ordering synchronized with TV3 period transition |
| D14 | Frozen original/dedup | CONFLICT | Cross | ACCEPT WITH CONDITIONS | TV2 permanent identity; TV1/TV3 hidden P1 parity and no origin rewrite |
| D15 | Opaque pagination | OPEN | Cross | ACCEPT WITH CONDITIONS | TV2 snapshot/coverage, TV3 explicit route mapping only |
| D16 | Durable device delivery | OPEN | Cross | ACCEPT WITH CONDITIONS | TV2 device persistence proof, TV3 period-scoped authorization |
| D17 | READ ID versus sequence | CONFLICT | Shared | ACCEPT WITH CONDITIONS | TV2/TV3 explicit cursor/schema mapping, no UUID numeric inference |
| D18 | Media lifecycle/ACL | CONFLICT | Shared | ACCEPT WITH CONDITIONS | Current TV1 privacy/TV3 history + TV2 media capability; no pending request media |
| D19 | Reply/edit/recall | CONFLICT | Shared | ACCEPT WITH CONDITIONS | TV2 CAS/version and TV3 history; window/activation remain separate |
| D20 | Retry/backoff | OPEN | Cross | NEEDS EVIDENCE | Same key on rollback; unknown commit preserved; no 404-triggered refresh |
| D21 | Room/process/outbox | OPEN | Cross | NEEDS EVIDENCE | TV2 durability, UUID/epoch/period isolation and stale apply prevention |
| D22 | Validation/limits/types | OPEN | Shared | REQUEST CHANGES | Compatibility aligned; exact route units/mappings and tests still required |
| D23 | Historical provenance | OPEN | Shared | NEEDS EVIDENCE | Newly received report; bounded limitation acceptance by all owners |
| D24 | Translation stale version | OPEN | Direct | ACCEPT WITH CONDITIONS | TV3 ACL and versioned cache, TV1 consent, TV2 Message version |
| D25 | INVITED versus invitation | CONFLICT | Direct | ACCEPT WITH CONDITIONS | Phúc lifecycle/rejoin; preserve existing processed409; shared atomic strategy |
| D26 | DIRECT pair uniqueness | OPEN | Cross | ACCEPT WITH CONDITIONS | TV2 canonical pair/accept transaction; TV1 block race, no GROUP mutation |
| D27 | WSS envelope/order | CONFLICT | Cross | ACCEPT WITH CONDITIONS | Keep FIX3 10 domain / 6 controls; request event changes need review |
| D28 | Version/stale mutation | OPEN | Cross | ACCEPT WITH CONDITIONS | TV2 current version/tombstone; no stale resurrection/translation overwrite |
| D29 | Same-epoch suspension | OPEN | Shared | ACCEPT WITH CONDITIONS | TV1/TV2/TV3 latest-snapshot admission; retirement physical proof pending |
| D30 | P0/P1/demo scope | CONFLICT | Shared | ACCEPT WITH CONDITIONS | Full P0 rubric jointly owned; TEXT-only demo cannot satisfy full acceptance |

## 6. Recommendations and explicit open decisions

**PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION:** full ADR D08, physical transaction strategy, any cross-domain schema, D09 exact inclusive floor/empty interval/legacy mapping and DIRECT interaction, D10 audience/former-member disclosure/aggregate denominator, D25 expiry/reinvite/cancellation/authority/accept-to-period policy. The cards compare alternatives; this document does not select them for Phúc.

TV1 must select exact DIRECT acceptance/block/unfriend/reopen/retained-history policy, current account/sid revocation fence, privacy for named historical receipts, and request abuse/retention/recovery parameters. TV2 must deliver 01E.2C, confirm sequence/period/receipt wire, canonical pair semantics, outbox/replay projection, global lock participation and bounded capacity. Affected owners jointly approve schema/transaction interfaces; the environment custodian supplies sanitized DB lineage and deployment authority later.

## 7. Checkpoint agenda and Day 1 gate

Agenda: validate provenance and relayed confirmations; establish D08 responsibilities and transaction candidate; decide D09/D10/D25; record D22 compatibility and D23 evidence limits; confirm D24/Auth/request boundaries; inspect all 30 impacts; record signed choices, conditions and next authorization. See [handoff checklist and recording template](TV3_SHARED_ARCHITECTURE_CHECKPOINT_HANDOFF.md).

Day 1 is **NOT READY TO EXIT**. Preparation can be complete while adoption remains pending. Required gates: scoped responses from all three owners; adopted D08–D10 and D22–D25; Auth/Messaging boundaries; period/history/receipt policy; DB lineage; full adopted ADR and shared transaction strategy; original register provenance; checkpoint record; affected-owner signatures and explicit condition handling. Aligned direction is not APPROVED. Runtime tests are not required merely to convene this checkpoint, but none may be reported as passed without execution evidence or used to bypass activation gates.

This task changes four Markdown documents only. No source, tests, schema, V1, migration allocation, shared DB connection, original Word or historical register is changed. Stop after publication for Phúc review.
