# Messaging API V1 Decision Register FIX3

> **Local publication candidate — identified public-safe derivative; owner clearance and final publication authorization pending.**
> Source R is a historical snapshot. Source measurements identify the internal original, not these derivative bytes. See [README](README.md) and [artifact manifest](artifact-manifest.md). Historical findings/status statements below remain dated evidence; later status is separately described in README.


**DRAFT — FOR TV1 / TV2 / TV3 REVIEW — NOT APPROVED FOR PRODUCTION IMPLEMENTATION.**

**Full contract status: PENDING. NOT FROZEN.**

TV2-CONTRACT-01C-FIX3 · 2026-11-25 · [Revised Draft](MESSAGING_API_V1_DRAFT_FIX3.md) · [Resolution Report](MESSAGING_API_V1_FIX3_RESOLUTION_REPORT.md).
All eleven inputs, including Original/FIX1/FIX2 artifacts and independent R2/R3, remain unchanged. FIX1/FIX2-labelled fields retain their historical evidence. FIX3 changes only R3-01 concealment/oracle references, R3-02 trace-table reference and R3-03 D27 count, plus revision metadata. Status: **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION** for each correction; no finding is CLOSED and no wire choice or implementation is approved.

## Evidence and interpretation

Source aliases S1–S8 resolve to exact inventory/hashes/pins in revised Draft §1. Source requirements and implementation snapshots are evidence, not approval. Original Register SHA256 B05D676D06B93FA42375A5FE2F6E2DDCB2CEAB8CEED556ACAFE44AE32F7E3F83. Independent01C verdict remains REQUEST CONTRACT REVISIONS; 16 records retain original severity/classification.

No new IDs: all30 D01–D30 preserved,21 OPEN and9 CONFLICT. Account sync/projection fit existing shared durable architecture D12, pagination D15, wire union D27; typing sessions fit existing ingress D16/D27; no new ownership domain introduced. Recommendations extended to make those protocols explicit, not silently approved.

G0–G7 are the design/activation milestones defined in Draft §17. Historical01B Priority is recorded for traceability only and cannot override the separate gates below. Baseline fields/transaction design must be resolved before dependent code/migration even when feature endpoint activation is later. Missing history D23 blocks provenance/freeze, not documentation revisions/re-review. CORE-04A FIX1 remains dependency for production Host.

## TV3 architecture evidence addendum

S9 = TV3 Phase 1 Foundation Architecture Audit **communication provided by user** in FIX1-ADDENDUM. Confirmed directions only: D-01 Modular Monolith/one backend deployable; D-02 shared DIRECT/GROUP Conversation UUID/message pipeline; D-03 period history/latest-join default; D-04 least privilege/ACTIVE-only access; D-05 server-authoritative transactional counter per conversation; D-07 standardized REST/WS with separate history/replay cursors; D-10 Messaging runtime/router ownership and TV1 integration adapter. Full artifact/Architecture Decision Ledger not supplied/read, no TV1 approval inferred.

Architecture IDs with hyphens (D-01 etc) are a different namespace from Messaging D01–D30. Complete eight-column alignment matrix and remaining wire/policy limits are in revised Draft §1.1; seven deliverables mapping in §15.2. No Messaging status/approval evidence is converted to APPROVED. D09/D10/D17/D25 now distinguish confirmed latest-join default and ACTIVE-only architecture from OPEN exact boundaries/exceptions/receipt policies. D07/D29 keep CORE-04A FIX1 and TV2-R13-01 unresolved pending verified new revision. Shared package/signatures in §15.1 are PROPOSED and need TV1/TV3 review.

## Decision records

### D01 — API prefix

- Unique decision ID: **D01**.
- Category: Shared cross-domain; REST,WS gateway.
- Decision question: API prefix sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 §5/B.7; S2 §5; S4 DAY1; S5 GroupController @RequestMapping /api/v1 và README REST contract. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-07 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: /api/v1 hoặc /v1; khác biệt có trong nguồn và/hoặc original requirement so với current domain behavior.
- Available alternatives: /api/v1 hoặc /v1.
- Recommended option: **PROPOSED — /api/v1**.
- Recommendation rationale: Bám quy ước chính của MASTER; không tạo hai API song song.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: REST,WS gateway.
- Implementation impact: Chốt base URL trước adapter.
- Historical01B priority: Initial text P0 blocker.
- Status: **CONFLICT**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang API prefix.
- Blocking milestone: G0 prefix/schema; dependent feature: Initial REST/WS routes. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 prefix/schema.
- Feature activation gate: Initial REST/WS routes; separately authorized after relevant acceptance evidence.
- Dependency rationale: Shared base URL and single prefix precede adapters.
- Related findings: CR-10.
- Related acceptance tests: IT09 IT10.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-07 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

### D02 — DTO JSON naming và nullability

- Unique decision ID: **D02**.
- Category: Shared cross-domain; DTO,Android,backend.
- Decision question: DTO JSON naming và nullability sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 §7/B.8; S2 §4–5; S4 Message. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-02, D-07 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: id/messageId; text/body; attachmentId/attachmentIds; replyToId/replyToMessageId; khác biệt có trong nguồn và/hoặc original requirement so với current domain behavior.
- Available alternatives: id/messageId; text/body; attachmentId/attachmentIds; replyToId/replyToMessageId.
- Recommended option: **PROPOSED — messageId, body, attachmentIds, replyToMessageId cho message; id cho conversation/attachment**.
- Recommendation rationale: Tên entity rõ; list hỗ trợ full media; mapping domain riêng.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: DTO,Android,backend.
- Implementation impact: Serializer phải theo đúng revised Draft §3; không alias im lặng. S5 GroupMapper.ConversationView chưa có lastServerSequence/myMembership, MemberView dùng lastReadMessageId, nên cần projection/adapter review chung; không tự coi Group DTO conform draft hoặc sửa TV3 interface.
- Historical01B priority: Initial text P0 blocker.
- Status: **CONFLICT**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang DTO JSON naming và nullability.
- Blocking milestone: G0 DTO required/nullability; G1 GROUP projection; dependent feature: Initial REST adapter, later GROUP/media. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 DTO required/nullability; G1 GROUP projection.
- Feature activation gate: Initial REST adapter, later GROUP/media; separately authorized after relevant acceptance evidence.
- Dependency rationale: Every required DTO is an early interoperability contract.
- Related findings: CR-08 CR-10 IR-13 IR-15.
- Related acceptance tests: IT08 IT09 IT10 IT13 IT15.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-02, D-07 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

- FIX2 R2-01 reference/sections: Draft FIX2 §3/§4/§7/§9.1a/§9.2–9.5/§10.2/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-01 preferred PROPOSED candidate/alternative: Embedded snapshot-bound requester MembershipBaseline; reuse member/interval schemas. Separate resource only on demonstrated capacity/schema need with equivalent bindings. No extra roster grant. Period UUID plus role/authority/history revisions fence every capability.
- FIX2 R2-01 rationale/baseline impact: Makes cold bootstrap/full-resync A=H constructible using specified wire resources. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-01 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-01 baseline/activation: G0/G1/G2; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-01 acceptance: IT02 IT09 IT11 AT12 AT18 AT24; runtime NOT RUN.
- FIX2 R2-01 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-04 reference/sections: Draft FIX2 §3 literal-null convention/TypingEvent/§10.4/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-04 preferred PROPOSED candidate/alternative: Type literal null, R, Nullable Yes. Present null accepted; missing/numeric0/numeric sequence/string reject using actual table.
- FIX2 R2-04 rationale/baseline impact: Corrects normative nullability conflict while preserving ephemeral noncheckpoint semantics. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-04 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-04 baseline/activation: Document re-review; G2 typing; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-04 acceptance: IT08 IT12 IT07; runtime NOT RUN.
- FIX2 R2-04 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D03 — 403 hay 404 và error envelope

- Unique decision ID: **D03**.
- Category: TV1; security,REST,media.
- Decision question: 403 hay 404 và error envelope sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 §8/B.7; S5 GroupService.requireMember. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-04 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: 403 minh bạch hoặc 404 che resource; details tùy chọn.
- Available alternatives: 403 minh bạch hoặc 404 che resource; details tùy chọn.
- Recommended option: **PROPOSED — 404 cho resource không thuộc quyền nhìn; 403 cho action bị cấm trên resource đã thấy**.
- Recommendation rationale: Giảm enumeration; phải thống nhất toàn backend.
- Owner: **TV1**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: security,REST,media.
- Implementation impact: Chốt mapping và response parity; không refresh vì 403.
- Historical01B priority: Initial text P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang 403 hay 404 và error envelope.
- Blocking milestone: G0 error/authorization registry; dependent feature: Initial REST and sync controls. No implementation approval in FIX1.
- Cross-reviewers: TV2 and TV3.
- Baseline design gate: G0 error/authorization registry.
- Feature activation gate: Initial REST and sync controls; separately authorized after relevant acceptance evidence.
- Dependency rationale: Concealment/errors apply before any endpoint ships.
- Related findings: IR-12.
- Related acceptance tests: IT12.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-04 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

- FIX2 R2-02 reference/sections: Draft FIX2 §4/§5.1–5.3/§7/§11/§14/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-02 preferred PROPOSED candidate/alternative: Full current MessageDTO and immutable ACK disclosure gate before equality/conflict/reconciliation; concealed404 when unauthorized. Capture immutable originMembershipPeriodId in X-Membership-Period; do not rebind uncommitted old pending work. Metadata-only reconciliation remains separately approved alternative.
- FIX2 R2-02 rationale/baseline impact: Current send authority is not visibility of old committed history; retain permanent account-key identity without hidden-key disclosure. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-02 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-02 baseline/activation: G0/G1/G2; dependent G3/G6; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-02 acceptance: IT04 IT05 IT15 IT16 AT05 AT06 AT08 AT18 AT19; runtime NOT RUN.
- FIX2 R2-02 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.
- FIX3 R3-01 candidate/trace: Draft FIX3 §5.2/§14/§16.2 uses the same canonical404 NOT_FOUND status/code/safe message/details/relevant headers/client recovery for hidden committed and absent retired-P1 keys under identical undisclosable P2 authority. A/D authorized reconciliation and authorized F/G conflict/purge/loss branches remain; no P2 send, rekey, origin rewrite or hidden ACK/SENT inference.
- FIX3 R3-01 responsibility/evidence: TV2 correction owner; existing decision owner and cross-review assignments above unchanged. TV1 privacy/security and TV3 membership/history cross-review; IT04 IT05 IT12 IT15 IT16 AT05 AT06 AT08 AT18 AT19 AT22 AT23 paired schedules, runtime NOT RUN. G0/G1/G2 and dependent G3/G6 unchanged. **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION**.


### D04 — CredentialProvider và JWT validation

- Unique decision ID: **D04**.
- Category: TV1; Auth,Session,transport.
- Decision question: CredentialProvider và JWT validation sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 B.3; S4 CredentialProvider; S5 GroupConfiguration. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: issuer/audience/expiry, refresh/revoke và adapter chưa có.
- Available alternatives: issuer/audience/expiry, refresh/revoke và adapter chưa có.
- Recommended option: **PROPOSED — JWT sub UUID server-issued; CredentialProvider single-flight; một replay 401 có giới hạn**.
- Recommendation rationale: Không suy ra login từ local user/epoch; không token ở UI.
- Owner: **TV1**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: Auth,Session,transport.
- Implementation impact: CORE-04A FIX1 và credential adapter phải được review độc lập.
- Historical01B priority: Initial text P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang CredentialProvider và JWT validation.
- Blocking milestone: G0 credential/authority handoff; G4 Host; dependent feature: Auth-backed REST/WS and E2E. No implementation approval in FIX1.
- Cross-reviewers: TV2 and TV3.
- Baseline design gate: G0 credential/authority handoff; G4 Host.
- Feature activation gate: Auth-backed REST/WS and E2E; separately authorized after relevant acceptance evidence.
- Dependency rationale: Current scope/revision required even before media/GROUP.
- Related findings: IR-15.
- Related acceptance tests: IT15 AT22 AT23.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.

- FIX2 R2-03 reference/sections: Draft FIX2 §2.1/§4/§8.1–8.2/§9/§14/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-03 preferred PROPOSED candidate/alternative: Authenticated connection.ready before POST; handshake-header alternative compared. Receiving g/q route metadata; replacement uniqueness, close/closed disposal, connection cleanup. Limits remain proposals.
- FIX2 R2-03 rationale/baseline impact: Removes first-generation circularity and fences old subscriptions without changing durable identity or account epoch. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-03 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-03 baseline/activation: G2/G4; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-03 acceptance: IT02 IT03 IT12 IT15 AT12 AT16 AT22 AT27; runtime NOT RUN.
- FIX2 R2-03 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D05 — Friendship và block policy

- Unique decision ID: **D05**.
- Category: TV1; Contacts,security,Messaging.
- Decision question: Friendship và block policy sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 CHAT-01/BLOCK-01; S2 §5; S5 GroupDirectory. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-04 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Friends-only hoặc message requests; block một/hai chiều, group exemption.
- Available alternatives: Friends-only hoặc message requests; block một/hai chiều, group exemption.
- Recommended option: **PROPOSED — Initial DIRECT chỉ accepted friendship, không block hai chiều; group policy tách riêng**.
- Recommendation rationale: Phương án hạn chế để chờ owner, không mặc định áp block DIRECT lên GROUP.
- Owner: **TV1**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: Contacts,security,Messaging.
- Implementation impact: Cần API authority transaction-safe; chưa có thì fail closed.
- Historical01B priority: Initial text P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Friendship và block policy.
- Blocking milestone: G0 current relationship serialization; dependent feature: Initial DIRECT/send; cross-domain block. No implementation approval in FIX1.
- Cross-reviewers: TV2 and TV3.
- Baseline design gate: G0 current relationship serialization.
- Feature activation gate: Initial DIRECT/send; cross-domain block; separately authorized after relevant acceptance evidence.
- Dependency rationale: Commit authority cannot rely on eventual preflight.
- Related findings: IR-14.
- Related acceptance tests: AT04 IT14.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-04 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

### D06 — DIRECT history khi block/unfriend

- Unique decision ID: **D06**.
- Category: TV1; history,attachments.
- Decision question: DIRECT history khi block/unfriend sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 BLOCK-01; S1 CHAT-05. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-04 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Giữ lịch sử cũ hoặc revoke toàn bộ quyền; thao tác mới khác đọc cũ.
- Available alternatives: Giữ lịch sử cũ hoặc revoke toàn bộ quyền; thao tác mới khác đọc cũ.
- Recommended option: **PROPOSED — Giữ history đã có quyền, chặn tương tác mới theo D05; không tự xóa**.
- Recommendation rationale: MASTER nói block không tự xóa lịch sử; quyền đọc chi tiết chưa khóa.
- Owner: **TV1**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: history,attachments.
- Implementation impact: Chốt history và download cùng policy trước E2E.
- Historical01B priority: Initial text P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang DIRECT history khi block/unfriend.
- Blocking milestone: G0 history/disclosure policy; G3 privacy; dependent feature: DIRECT history/download/replay. No implementation approval in FIX1.
- Cross-reviewers: TV2 and TV3.
- Baseline design gate: G0 history/disclosure policy; G3 privacy.
- Feature activation gate: DIRECT history/download/replay; separately authorized after relevant acceptance evidence.
- Dependency rationale: Old-history visibility differs from permission to interact.
- Related findings: IR-16.
- Related acceptance tests: IT01 IT16.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-04 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

### D07 — Shared Host Integration và resource-hook failure

- Unique decision ID: **D07**.
- Category: Shared cross-domain; Host,Session,Groups,Messaging.
- Decision question: Shared Host Integration và resource-hook failure sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S3 §6; S6 SessionCoordinator; yêu cầu tác vụ CORE-04A FIX1. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-10 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Port chung chưa frozen; optional resource failure/throw/cleanup failure.
- Available alternatives: Port chung chưa frozen; optional resource failure/throw/cleanup failure.
- Recommended option: **PROPOSED — TV1 composition root retains/injects one TV2-owned MessagingExecution/runtime and adapter; independent leases, observable fail-closed resource recovery; proposed §15.1 ports require TV1/TV3 review**.
- Recommendation rationale: Không coi snapshot bị REQUEST CHANGES là contract.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: Host,Session,Groups,Messaging.
- Implementation impact: Cần interface và failure acceptance; không triển khai ở 01B.
- Historical01B priority: Production Host/E2E blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Shared Host Integration và resource-hook failure.
- Blocking milestone: G4 Host resource policy; dependent feature: Production Host/E2E. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G4 Host resource policy.
- Feature activation gate: Production Host/E2E; separately authorized after relevant acceptance evidence.
- Dependency rationale: Foundation caps and coordinator snapshot do not resolve resource hook failure.
- Related findings: IR-15.
- Related acceptance tests: AT17 AT23 IT15.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. CORE-04A FIX1 remains unresolved dependency.
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-10 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.
- Addendum Host dependency: TV1 owns App Shell/Home/SessionCoordinator/System Back and assembles/retains the TV2 runtime; TV2 owns MessagingController/Router/Presentation/Execution and adapter. Proposed core.integration signatures/package need TV1/TV3 review. CORE-04A FIX1 and TV2-R13-01 are unresolved until verified new revision.

### D08 — Backend/schema ownership và migration registry

- Unique decision ID: **D08**.
- Category: Shared cross-domain; backend,DB.
- Decision question: Backend/schema ownership và migration registry sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 §6/B.9; S5 baseline proposal và V1. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-01, D-02, D-05 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Registry, môi trường đã áp dụng, checksum chưa biết.
- Available alternatives: Registry, môi trường đã áp dụng, checksum chưa biết.
- Recommended option: **PROPOSED — Một backend; TV1 điều phối registry/security, domain owner đề xuất migration tiến**.
- Recommendation rationale: Tránh backend trùng và sửa migration đã áp dụng.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: backend,DB.
- Implementation impact: Không cấp version cuối hoặc SQL; kiểm deployment history trước.
- Historical01B priority: Initial text P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Backend/schema ownership và migration registry.
- Blocking milestone: G0 registry/global transaction strategy; G1 shared schema; dependent feature: Any backend migration/transaction. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 registry/global transaction strategy; G1 shared schema.
- Feature activation gate: Any backend migration/transaction; separately authorized after relevant acceptance evidence.
- Dependency rationale: All writers require one reviewed lock/admission order and applied-checksum evidence.
- Related findings: IR-14 CR-09.
- Related acceptance tests: IT09 IT14.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-01, D-02, D-05 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

### D09 — Join/rejoin và history bounds

- Unique decision ID: **D09**.
- Category: TV3; Groups,history,ACL.
- Decision question: Join/rejoin và history bounds sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 GROUP-03/B.6; S5 member UNIQUE và status. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-03, D-04 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: All history hoặc từ join; một membership row hay epochs nhiều lần join.
- Available alternatives: All history hoặc từ join; một membership row hay epochs nhiều lần join.
- Recommended option: **PROPOSED wire mechanics — Stable membershipPeriodId, separate role/authority/history revisions and visible intervals representing TV3-confirmed latest-join default/ACTIVE-only direction. Exact boundary, exceptions, DIRECT period/receipt/authority-publication policy remains OPEN; no wire approval inferred**.
- Recommendation rationale: Một joinedAt bị ghi đè không biểu diễn được các lần tham gia.
- Owner: **TV3**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: Groups,history,ACL.
- Implementation impact: Chốt rejoin, LEFT/REMOVED, revoke subscription và attachment bounds.
- Historical01B priority: Full P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Join/rejoin và history bounds.
- Blocking milestone: G1 period/history; G0 shared fields if used; dependent feature: GROUP/receipt/replay or reviewed DIRECT-only extension. No implementation approval in FIX1.
- Cross-reviewers: TV1 and TV2.
- Baseline design gate: G1 period/history; G0 shared fields if used.
- Feature activation gate: GROUP/receipt/replay or reviewed DIRECT-only extension; separately authorized after relevant acceptance evidence.
- Dependency rationale: Period compatibility cannot wait until after dependent receipt migration.
- Related findings: CR-04 CR-09 IR-11.
- Related acceptance tests: IT04 IT09 IT11.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-03, D-04 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.
- Addendum membership/history boundary: Latest-join default and ACTIVE-only access are TV3-confirmed architecture. Exact membership-transaction history boundary, previous-period exceptions, DIRECT period semantics, receipt visibility after LEFT/REMOVED, per-message audience, all/any denominator, period lifetime and authority revision publication remain OPEN.

- FIX2 R2-01 reference/sections: Draft FIX2 §3/§4/§7/§9.1a/§9.2–9.5/§10.2/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-01 preferred PROPOSED candidate/alternative: Embedded snapshot-bound requester MembershipBaseline; reuse member/interval schemas. Separate resource only on demonstrated capacity/schema need with equivalent bindings. No extra roster grant. Period UUID plus role/authority/history revisions fence every capability.
- FIX2 R2-01 rationale/baseline impact: Makes cold bootstrap/full-resync A=H constructible using specified wire resources. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-01 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-01 baseline/activation: G0/G1/G2; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-01 acceptance: IT02 IT09 IT11 AT12 AT18 AT24; runtime NOT RUN.
- FIX2 R2-01 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-02 reference/sections: Draft FIX2 §4/§5.1–5.3/§7/§11/§14/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-02 preferred PROPOSED candidate/alternative: Full current MessageDTO and immutable ACK disclosure gate before equality/conflict/reconciliation; concealed404 when unauthorized. Capture immutable originMembershipPeriodId in X-Membership-Period; do not rebind uncommitted old pending work. Metadata-only reconciliation remains separately approved alternative.
- FIX2 R2-02 rationale/baseline impact: Current send authority is not visibility of old committed history; retain permanent account-key identity without hidden-key disclosure. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-02 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-02 baseline/activation: G0/G1/G2; dependent G3/G6; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-02 acceptance: IT04 IT05 IT15 IT16 AT05 AT06 AT08 AT18 AT19; runtime NOT RUN.
- FIX2 R2-02 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.
- FIX3 R3-01 candidate/trace: Draft FIX3 §5.2/§14/§16.2 uses the same canonical404 NOT_FOUND status/code/safe message/details/relevant headers/client recovery for hidden committed and absent retired-P1 keys under identical undisclosable P2 authority. A/D authorized reconciliation and authorized F/G conflict/purge/loss branches remain; no P2 send, rekey, origin rewrite or hidden ACK/SENT inference.
- FIX3 R3-01 responsibility/evidence: TV2 correction owner; existing decision owner and cross-review assignments above unchanged. TV1 privacy/security and TV3 membership/history cross-review; IT04 IT05 IT12 IT15 IT16 AT05 AT06 AT08 AT18 AT19 AT22 AT23 paired schedules, runtime NOT RUN. G0/G1/G2 and dependent G3/G6 unchanged. **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION**.


### D10 — GROUP receipt visibility và aggregation

- Unique decision ID: **D10**.
- Category: TV3; Groups,receipts,UI.
- Decision question: GROUP receipt visibility và aggregation sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 GROUP-03/B.6; S4 MessageStatus. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-03, D-04 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: any/all recipients; denominator lúc gửi hay hiện tại; privacy.
- Available alternatives: any/all recipients; denominator lúc gửi hay hiện tại; privacy.
- Recommended option: **PROPOSED — Period-specific member receipts and explicit snapshot evidence; no GROUP-wide READ/denominator choice until TV3 resolves audience, visibility and all/any policy**.
- Recommendation rationale: Enum local không định nghĩa trạng thái nhóm.
- Owner: **TV3**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: Groups,receipts,UI.
- Implementation impact: Chốt đối tượng được thấy receipt, người join/leave và multi-device.
- Historical01B priority: Full P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang GROUP receipt visibility và aggregation.
- Blocking milestone: G1 receipt visibility/interval semantics; dependent feature: GROUP receipts/unread/aggregation. No implementation approval in FIX1.
- Cross-reviewers: TV1 and TV2.
- Baseline design gate: G1 receipt visibility/interval semantics.
- Feature activation gate: GROUP receipts/unread/aggregation; separately authorized after relevant acceptance evidence.
- Dependency rationale: Wire period identity and visibility precede receipt data storage.
- Related findings: CR-04 IR-11.
- Related acceptance tests: IT04 IT11.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-03, D-04 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.
- Addendum membership/history boundary: Latest-join default and ACTIVE-only access are TV3-confirmed architecture. Exact membership-transaction history boundary, previous-period exceptions, DIRECT period semantics, receipt visibility after LEFT/REMOVED, per-message audience, all/any denominator, period lifetime and authority revision publication remain OPEN.

- FIX2 R2-01 reference/sections: Draft FIX2 §3/§4/§7/§9.1a/§9.2–9.5/§10.2/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-01 preferred PROPOSED candidate/alternative: Embedded snapshot-bound requester MembershipBaseline; reuse member/interval schemas. Separate resource only on demonstrated capacity/schema need with equivalent bindings. No extra roster grant. Period UUID plus role/authority/history revisions fence every capability.
- FIX2 R2-01 rationale/baseline impact: Makes cold bootstrap/full-resync A=H constructible using specified wire resources. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-01 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-01 baseline/activation: G0/G1/G2; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-01 acceptance: IT02 IT09 IT11 AT12 AT18 AT24; runtime NOT RUN.
- FIX2 R2-01 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D11 — WebSocket authentication/renewal

- Unique decision ID: **D11**.
- Category: TV1; security,WS.
- Decision question: WebSocket authentication/renewal sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S2 §7; S4 CredentialProvider; S5 JWT security. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-07 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Authorization handshake, short-lived ticket hoặc subprotocol; renewal frame hay reconnect.
- Available alternatives: Authorization handshake, short-lived ticket hoặc subprotocol; renewal frame hay reconnect.
- Recommended option: **PROPOSED — Native WSS Authorization handshake, reconnect có giới hạn khi credential đổi; không token trong URL**.
- Recommendation rationale: Giảm log credential; kiểm lại epoch ngay handoff.
- Owner: **TV1**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: security,WS.
- Implementation impact: Chốt expiry close reason và refresh; không áp bearer vào frame tự đặt.
- Historical01B priority: Full P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang WebSocket authentication/renewal.
- Blocking milestone: G2 connection/auth renewal/control; dependent feature: WS production. No implementation approval in FIX1.
- Cross-reviewers: TV2 and TV3.
- Baseline design gate: G2 connection/auth renewal/control.
- Feature activation gate: WS production; separately authorized after relevant acceptance evidence.
- Dependency rationale: Connection generation/auth lifetime required before replay.
- Related findings: CR-02.
- Related acceptance tests: IT02 AT27.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-07 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

- FIX2 R2-03 reference/sections: Draft FIX2 §2.1/§4/§8.1–8.2/§9/§14/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-03 preferred PROPOSED candidate/alternative: Authenticated connection.ready before POST; handshake-header alternative compared. Receiving g/q route metadata; replacement uniqueness, close/closed disposal, connection cleanup. Limits remain proposals.
- FIX2 R2-03 rationale/baseline impact: Removes first-generation circularity and fences old subscriptions without changing durable identity or account epoch. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-03 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-03 baseline/activation: G2/G4; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-03 acceptance: IT02 IT03 IT12 IT15 AT12 AT16 AT22 AT27; runtime NOT RUN.
- FIX2 R2-03 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-05 reference/sections: Draft FIX2 §8.1/TypingPayload/§10.4/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-05 preferred PROPOSED candidate/alternative: B TTL-bounded residual; A recipient-visible retirement oracle remains alternative. Separate origin generation, receiving g/q, typingId/revision/absolute expiry; terminal STOP, any-live-device and bounded-clock fail-closed semantics.
- FIX2 R2-05 rationale/baseline impact: An unordered UUID cannot distinguish reconnect from two devices; no unconditional remote-retirement claim. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-05 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-05 baseline/activation: G2 typing; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-05 acceptance: IT07 IT15 AT14 AT16 AT27; runtime NOT RUN.
- FIX2 R2-05 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D12 — Durable replay architecture

- Unique decision ID: **D12**.
- Category: Shared cross-domain; backend,WS,cache.
- Decision question: Durable replay architecture sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 B.8; S2 §6.2 thiếu durable mutation log. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-04, D-05, D-07 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Durable per-conversation event log hoặc transactional CDC với ordered resume token.
- Available alternatives: Durable per-conversation event log hoặc transactional CDC với ordered resume token.
- Recommended option: **PROPOSED — Transactional commit-safe conversation log with recipient-authorized projection, S/H/R/A coverage protocol and independent account change feed; bounded authoritative polling remains an account-feed alternative**.
- Recommendation rationale: afterSequence của messages không thấy edit/recall/receipt.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: backend,WS,cache.
- Implementation impact: Chốt retention, replay protocol và full resync; không hứa lossless trước test.
- Historical01B priority: Full P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Durable replay architecture.
- Blocking milestone: G0 durable mutation write/order/projection; G2 snapshot/replay/account; dependent feature: Initial transactional events, then replay/account feed. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 durable mutation write/order/projection; G2 snapshot/replay/account.
- Feature activation gate: Initial transactional events, then replay/account feed; separately authorized after relevant acceptance evidence.
- Dependency rationale: Log identity is not wire projection; server delivery is not client applied coverage.
- Related findings: CR-01 CR-02 CR-03 CR-09 IR-11 IR-16.
- Related acceptance tests: IT01 IT02 IT03 IT09 IT11 IT16.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-04, D-05, D-07 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

- FIX2 R2-01 reference/sections: Draft FIX2 §3/§4/§7/§9.1a/§9.2–9.5/§10.2/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-01 preferred PROPOSED candidate/alternative: Embedded snapshot-bound requester MembershipBaseline; reuse member/interval schemas. Separate resource only on demonstrated capacity/schema need with equivalent bindings. No extra roster grant. Period UUID plus role/authority/history revisions fence every capability.
- FIX2 R2-01 rationale/baseline impact: Makes cold bootstrap/full-resync A=H constructible using specified wire resources. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-01 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-01 baseline/activation: G0/G1/G2; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-01 acceptance: IT02 IT09 IT11 AT12 AT18 AT24; runtime NOT RUN.
- FIX2 R2-01 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-03 reference/sections: Draft FIX2 §2.1/§4/§8.1–8.2/§9/§14/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-03 preferred PROPOSED candidate/alternative: Authenticated connection.ready before POST; handshake-header alternative compared. Receiving g/q route metadata; replacement uniqueness, close/closed disposal, connection cleanup. Limits remain proposals.
- FIX2 R2-03 rationale/baseline impact: Removes first-generation circularity and fences old subscriptions without changing durable identity or account epoch. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-03 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-03 baseline/activation: G2/G4; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-03 acceptance: IT02 IT03 IT12 IT15 AT12 AT16 AT22 AT27; runtime NOT RUN.
- FIX2 R2-03 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D13 — Message sequence allocation và watermark

- Unique decision ID: **D13**.
- Category: TV2; messages,DB.
- Decision question: Message sequence allocation và watermark sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 CHAT-02; S4 fake rows.size+1. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-05 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: DB sequence cấp trước commit hoặc counter row serialize transaction.
- Available alternatives: DB sequence cấp trước commit hoặc counter row serialize transaction.
- Recommended option: **PROPOSED — Per-conversation counter lock giữ đến commit; eventSequence tách; gaps cho phép**.
- Recommendation rationale: Ngăn seq thấp commit sau watermark cao đã công bố.
- Owner: **TV2**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: messages,DB.
- Implementation impact: DB owner review lock order với membership; không dùng fake algorithm production.
- Historical01B priority: Initial text P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Message sequence allocation và watermark.
- Blocking milestone: G0 commit-safe message/event allocation; dependent feature: Initial send/counters. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 commit-safe message/event allocation.
- Feature activation gate: Initial send/counters; separately authorized after relevant acceptance evidence.
- Dependency rationale: Watermark cannot overtake a lower transaction still able to commit.
- Related findings: IR-14.
- Related acceptance tests: AT09 IT14.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-05 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

### D14 — Frozen original payload equality và dedup retention

- Unique decision ID: **D14**.
- Category: TV2; outbox,idempotency,backend.
- Decision question: Frozen original payload equality và dedup retention sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 B.3; S4 Message.samePayload/Fake.send. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval.
- Conflicting requirements: Domain equality có mutable timestamps so với send request bất biến; khác biệt có trong nguồn và/hoặc original requirement so với current domain behavior.
- Available alternatives: Domain equality có mutable timestamps so với send request bất biến.
- Recommended option: **PROPOSED — Account-scoped key and exact frozen original schema/path/bytes; current authorization first, committed comparison before new-send-only checks; no key reuse after purge**.
- Recommendation rationale: Retry sau edit/recall phải so original send, không so mutable representation.
- Owner: **TV2**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: outbox,idempotency,backend.
- Implementation impact: Retention đề xuất bằng vòng đời identity message; purge cần policy không tái tạo.
- Historical01B priority: Initial text P0 blocker.
- Status: **CONFLICT**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Frozen original payload equality và dedup retention.
- Blocking milestone: G0 frozen equality/key privacy; G3 media branch; dependent feature: Initial send/ACK and later media/recovery. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 frozen equality/key privacy; G3 media branch.
- Feature activation gate: Initial send/ACK and later media/recovery; separately authorized after relevant acceptance evidence.
- Dependency rationale: Committed comparison before mutable new-send validation; retention cannot free key.
- Related findings: CR-05 IR-15 IR-16.
- Related acceptance tests: IT05 IT15 IT16.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.

- FIX2 R2-02 reference/sections: Draft FIX2 §4/§5.1–5.3/§7/§11/§14/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-02 preferred PROPOSED candidate/alternative: Full current MessageDTO and immutable ACK disclosure gate before equality/conflict/reconciliation; concealed404 when unauthorized. Capture immutable originMembershipPeriodId in X-Membership-Period; do not rebind uncommitted old pending work. Metadata-only reconciliation remains separately approved alternative.
- FIX2 R2-02 rationale/baseline impact: Current send authority is not visibility of old committed history; retain permanent account-key identity without hidden-key disclosure. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-02 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-02 baseline/activation: G0/G1/G2; dependent G3/G6; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-02 acceptance: IT04 IT05 IT15 IT16 AT05 AT06 AT08 AT18 AT19; runtime NOT RUN.
- FIX2 R2-02 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.
- FIX3 R3-01 candidate/trace: Draft FIX3 §5.2/§14/§16.2 uses the same canonical404 NOT_FOUND status/code/safe message/details/relevant headers/client recovery for hidden committed and absent retired-P1 keys under identical undisclosable P2 authority. A/D authorized reconciliation and authorized F/G conflict/purge/loss branches remain; no P2 send, rekey, origin rewrite or hidden ACK/SENT inference.
- FIX3 R3-01 responsibility/evidence: TV2 correction owner; existing decision owner and cross-review assignments above unchanged. TV1 privacy/security and TV3 membership/history cross-review; IT04 IT05 IT12 IT15 IT16 AT05 AT06 AT08 AT18 AT19 AT22 AT23 paired schedules, runtime NOT RUN. G0/G1/G2 and dependent G3/G6 unchanged. **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION**.


### D15 — Opaque pagination và snapshot

- Unique decision ID: **D15**.
- Category: TV2; REST,cache.
- Decision question: Opaque pagination và snapshot sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 B.7; S4 MessagePage Long cursor. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-07 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Sequence công khai hoặc opaque scoped cursor; expiry.
- Available alternatives: Sequence công khai hoặc opaque scoped cursor; expiry.
- Recommended option: **PROPOSED — Separate SNAPSHOT, TRAVERSAL, CONVERSATION_RESUME and ACCOUNT_RESUME tokens; endpoint-specific list/message pages; new forward range from snapshot capability only**.
- Recommendation rationale: Chống sửa cursor và tránh hiểu Long local là REST cursor.
- Owner: **TV2**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: REST,cache.
- Implementation impact: Chốt secret rotation/expiry, forward catch-up và list snapshot.
- Historical01B priority: Initial text P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Opaque pagination và snapshot.
- Blocking milestone: G0 token/page taxonomy; G2 account/snapshot handoff; dependent feature: Pagination then replay/discovery. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 token/page taxonomy; G2 account/snapshot handoff.
- Feature activation gate: Pagination then replay/discovery; separately authorized after relevant acceptance evidence.
- Dependency rationale: Traversal capability and durable resume are different domains.
- Related findings: CR-02 CR-03 CR-06 IR-13.
- Related acceptance tests: IT02 IT03 IT06 IT13.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-07 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

- FIX2 R2-01 reference/sections: Draft FIX2 §3/§4/§7/§9.1a/§9.2–9.5/§10.2/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-01 preferred PROPOSED candidate/alternative: Embedded snapshot-bound requester MembershipBaseline; reuse member/interval schemas. Separate resource only on demonstrated capacity/schema need with equivalent bindings. No extra roster grant. Period UUID plus role/authority/history revisions fence every capability.
- FIX2 R2-01 rationale/baseline impact: Makes cold bootstrap/full-resync A=H constructible using specified wire resources. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-01 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-01 baseline/activation: G0/G1/G2; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-01 acceptance: IT02 IT09 IT11 AT12 AT18 AT24; runtime NOT RUN.
- FIX2 R2-01 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-03 reference/sections: Draft FIX2 §2.1/§4/§8.1–8.2/§9/§14/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-03 preferred PROPOSED candidate/alternative: Authenticated connection.ready before POST; handshake-header alternative compared. Receiving g/q route metadata; replacement uniqueness, close/closed disposal, connection cleanup. Limits remain proposals.
- FIX2 R2-03 rationale/baseline impact: Removes first-generation circularity and fences old subscriptions without changing durable identity or account epoch. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-03 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-03 baseline/activation: G2/G4; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-03 acceptance: IT02 IT03 IT12 IT15 AT12 AT16 AT22 AT27; runtime NOT RUN.
- FIX2 R2-03 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D16 — Delivery ingress và durable device evidence

- Unique decision ID: **D16**.
- Category: TV2; receipts,Android,backend.
- Decision question: Delivery ingress và durable device evidence sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 CHAT-03; S4 không delivery transport. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: WS delivered command hoặc REST delivery cursor; RAM receive hay local commit.
- Available alternatives: WS delivered command hoặc REST delivery cursor; RAM receive hay local commit.
- Recommended option: **PROPOSED — Period/history-scoped durable device delivery ingress plus connection-bound typingId/revision sessions; no socket-flush receipt or typing replay**.
- Recommendation rationale: DELIVERED không đồng nghĩa websocket write thành công.
- Owner: **TV2**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: receipts,Android,backend.
- Implementation impact: Endpoint bổ sung PROPOSED; cần TV1 device policy và TV3 group review.
- Historical01B priority: Full P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Delivery ingress và durable device evidence.
- Blocking milestone: G1 period delivery shape; G2 delivery/typing session protocol; dependent feature: Delivery receipts and typing. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G1 period delivery shape; G2 delivery/typing session protocol.
- Feature activation gate: Delivery receipts and typing; separately authorized after relevant acceptance evidence.
- Dependency rationale: Device durable evidence and ephemeral generations need explicit ingress.
- Related findings: CR-04 CR-07 IR-11.
- Related acceptance tests: IT04 IT07 IT11.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.

- FIX2 R2-04 reference/sections: Draft FIX2 §3 literal-null convention/TypingEvent/§10.4/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-04 preferred PROPOSED candidate/alternative: Type literal null, R, Nullable Yes. Present null accepted; missing/numeric0/numeric sequence/string reject using actual table.
- FIX2 R2-04 rationale/baseline impact: Corrects normative nullability conflict while preserving ephemeral noncheckpoint semantics. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-04 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-04 baseline/activation: Document re-review; G2 typing; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-04 acceptance: IT08 IT12 IT07; runtime NOT RUN.
- FIX2 R2-04 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-05 reference/sections: Draft FIX2 §8.1/TypingPayload/§10.4/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-05 preferred PROPOSED candidate/alternative: B TTL-bounded residual; A recipient-visible retirement oracle remains alternative. Separate origin generation, receiving g/q, typingId/revision/absolute expiry; terminal STOP, any-live-device and bounded-clock fail-closed semantics.
- FIX2 R2-05 rationale/baseline impact: An unordered UUID cannot distinguish reconnect from two devices; no unconditional remote-retirement claim. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-05 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-05 baseline/activation: G2 typing; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-05 acceptance: IT07 IT15 AT14 AT16 AT27; runtime NOT RUN.
- FIX2 R2-05 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D17 — READ message ID hay sequence cursor

- Unique decision ID: **D17**.
- Category: Shared cross-domain; receipts,DB.
- Decision question: READ message ID hay sequence cursor sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 §7/B.7; S5 last_read_message_id; S4 markRead. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-03, D-04 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: lastReadMessageId hoặc lastReadSequence; khác biệt có trong nguồn và/hoặc original requirement so với current domain behavior.
- Available alternatives: ID-only, sequence-only, or both server message ID and sequence; local markRead(ServerMessage) supplies both, which does not choose wire. Period/history fencing required whichever alternative.
- Recommended option: **PROPOSED — Wire membershipPeriodId/historyRevision plus decimal-string lastReadSequence and eligible interval evidence; retain both server ID and sequence in local adapter evidence**.
- Recommendation rationale: Giữ local ServerMessage evidence; wire cursor không do local port quyết định.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: receipts,DB.
- Implementation impact: Migration tiến và GROUP visibility cần review.
- Historical01B priority: Initial text P0 blocker.
- Status: **CONFLICT**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang READ message ID hay sequence cursor.
- Blocking milestone: G0 READ shape; G1 period compatibility; dependent feature: Initial READ, then GROUP receipts. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 READ shape; G1 period compatibility.
- Feature activation gate: Initial READ, then GROUP receipts; separately authorized after relevant acceptance evidence.
- Dependency rationale: Scalar cursor evidence is only eligible period prefix, not invisible global prefix.
- Related findings: CR-04 CR-09 CR-10 IR-11.
- Related acceptance tests: IT04 IT09 IT10 IT11.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-03, D-04 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.
- Addendum membership/history boundary: Latest-join default and ACTIVE-only access are TV3-confirmed architecture. Exact membership-transaction history boundary, previous-period exceptions, DIRECT period semantics, receipt visibility after LEFT/REMOVED, per-message audience, all/any denominator, period lifetime and authority revision publication remain OPEN.

### D18 — Media lifecycle, endpoint và ACL

- Unique decision ID: **D18**.
- Category: Shared cross-domain; media,storage,Messaging.
- Decision question: Media lifecycle, endpoint và ACL sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 §8/B.7; S2 §5.1; S4 enum media. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-04 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: upload-init/complete hoặc media/uploads; single/multiple attachments; khác biệt có trong nguồn và/hoặc original requirement so với current domain behavior.
- Available alternatives: upload-init/complete hoặc media/uploads; single/multiple attachments.
- Recommended option: **PROPOSED — Baseline ordered attachmentIds with[] TEXT; media uploads/finalize READY and atomic new-send binding; committed identical retries bypass mutable READY checks**.
- Recommendation rationale: Không message tham chiếu upload chưa hợp lệ; ACL mỗi lần download.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: media,storage,Messaging.
- Implementation impact: Chốt quota/types/size/orphan TTL/provider trước media implementation.
- Historical01B priority: Full P0 blocker.
- Status: **CONFLICT**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Media lifecycle, endpoint và ACL.
- Blocking milestone: G0 initial[]/type/schema; G3 media lifecycle; dependent feature: Full P0 media. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 initial[]/type/schema; G3 media lifecycle.
- Feature activation gate: Full P0 media; separately authorized after relevant acceptance evidence.
- Dependency rationale: Early DTO fields cannot defer to upload implementation; replay skips READY checks.
- Related findings: CR-05 CR-09 IR-16.
- Related acceptance tests: IT05 IT09 IT16.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-04 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

- FIX2 R2-02 reference/sections: Draft FIX2 §4/§5.1–5.3/§7/§11/§14/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-02 preferred PROPOSED candidate/alternative: Full current MessageDTO and immutable ACK disclosure gate before equality/conflict/reconciliation; concealed404 when unauthorized. Capture immutable originMembershipPeriodId in X-Membership-Period; do not rebind uncommitted old pending work. Metadata-only reconciliation remains separately approved alternative.
- FIX2 R2-02 rationale/baseline impact: Current send authority is not visibility of old committed history; retain permanent account-key identity without hidden-key disclosure. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-02 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-02 baseline/activation: G0/G1/G2; dependent G3/G6; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-02 acceptance: IT04 IT05 IT15 IT16 AT05 AT06 AT08 AT18 AT19; runtime NOT RUN.
- FIX2 R2-02 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.
- FIX3 R3-01 candidate/trace: Draft FIX3 §5.2/§14/§16.2 uses the same canonical404 NOT_FOUND status/code/safe message/details/relevant headers/client recovery for hidden committed and absent retired-P1 keys under identical undisclosable P2 authority. A/D authorized reconciliation and authorized F/G conflict/purge/loss branches remain; no P2 send, rekey, origin rewrite or hidden ACK/SENT inference.
- FIX3 R3-01 responsibility/evidence: TV2 correction owner; existing decision owner and cross-review assignments above unchanged. TV1 privacy/security and TV3 membership/history cross-review; IT04 IT05 IT12 IT15 IT16 AT05 AT06 AT08 AT18 AT19 AT22 AT23 paired schedules, runtime NOT RUN. G0/G1/G2 and dependent G3/G6 unchanged. **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION**.


### D19 — Reply/Edit/Recall và endpoint

- Unique decision ID: **D19**.
- Category: Shared cross-domain; messages,Groups,UI.
- Decision question: Reply/Edit/Recall và endpoint sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 CHAT-04 §8/B.7; S2 §5.1. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval.
- Conflicting requirements: revoke/recall; window; version conflict; moderation; khác biệt có trong nguồn và/hoặc original requirement so với current domain behavior.
- Available alternatives: revoke/recall; window; version conflict; moderation.
- Recommended option: **PROPOSED — /messages/{messageId}/revoke; sender-only edit/revoke; expected version; 15 phút candidate**.
- Recommendation rationale: Giữ tombstone, event durable và idempotency original.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: messages,Groups,UI.
- Implementation impact: P1 không chặn initial text; nếu enabled phải có ACL và optimistic concurrency.
- Historical01B priority: P1.
- Status: **CONFLICT**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Reply/Edit/Recall và endpoint.
- Blocking milestone: G0 mutation/redaction compatibility; G5 policy/endpoints; dependent feature: P1 reply/edit/revoke. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 mutation/redaction compatibility; G5 policy/endpoints.
- Feature activation gate: P1 reply/edit/revoke; separately authorized after relevant acceptance evidence.
- Dependency rationale: Version/authorized projection must be baseline-safe; sender/window/moderation OPEN.
- Related findings: CR-01 CR-10 IR-16.
- Related acceptance tests: IT01 IT10 IT16 AT25.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.

### D20 — Retry classification và bounded backoff

- Unique decision ID: **D20**.
- Category: TV2; transport,outbox.
- Decision question: Retry classification và bounded backoff sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S4 DAY1/ChatFailure. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-10 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: OFFLINE/TIMEOUT hiện có so với tự retry 429/5xx.
- Available alternatives: OFFLINE/TIMEOUT hiện có so với tự retry 429/5xx.
- Recommended option: **PROPOSED — Giữ auto-retry eligibility OFFLINE/TIMEOUT; không thêm scheduler; auth replay riêng do D04**.
- Recommendation rationale: Không thay policy bằng ánh xạ HTTP tùy ý.
- Owner: **TV2**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: transport,outbox.
- Implementation impact: Retry-After hiển thị chưa tự retry; giới hạn attempt/backoff phải duyệt trước scheduler.
- Historical01B priority: Initial text P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Retry classification và bounded backoff.
- Blocking milestone: G0 failure/unknown-commit classification; G4 transport bounds; dependent feature: Initial REST recovery then production transport. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 failure/unknown-commit classification; G4 transport bounds.
- Feature activation gate: Initial REST recovery then production transport; separately authorized after relevant acceptance evidence.
- Dependency rationale: Auth replay different from retry scheduler; cancellation not rollback.
- Related findings: IR-12 IR-15.
- Related acceptance tests: IT12 IT15 AT08.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-10 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.
- Addendum Host dependency: TV1 owns App Shell/Home/SessionCoordinator/System Back and assembles/retains the TV2 runtime; TV2 owns MessagingController/Router/Presentation/Execution and adapter. Proposed core.integration signatures/package need TV1/TV3 review. CORE-04A FIX1 and TV2-R13-01 are unresolved until verified new revision.

- FIX2 R2-02 reference/sections: Draft FIX2 §4/§5.1–5.3/§7/§11/§14/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-02 preferred PROPOSED candidate/alternative: Full current MessageDTO and immutable ACK disclosure gate before equality/conflict/reconciliation; concealed404 when unauthorized. Capture immutable originMembershipPeriodId in X-Membership-Period; do not rebind uncommitted old pending work. Metadata-only reconciliation remains separately approved alternative.
- FIX2 R2-02 rationale/baseline impact: Current send authority is not visibility of old committed history; retain permanent account-key identity without hidden-key disclosure. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-02 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-02 baseline/activation: G0/G1/G2; dependent G3/G6; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-02 acceptance: IT04 IT05 IT15 IT16 AT05 AT06 AT08 AT18 AT19; runtime NOT RUN.
- FIX2 R2-02 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.
- FIX3 R3-01 candidate/trace: Draft FIX3 §5.2/§14/§16.2 uses the same canonical404 NOT_FOUND status/code/safe message/details/relevant headers/client recovery for hidden committed and absent retired-P1 keys under identical undisclosable P2 authority. A/D authorized reconciliation and authorized F/G conflict/purge/loss branches remain; no P2 send, rekey, origin rewrite or hidden ACK/SENT inference.
- FIX3 R3-01 responsibility/evidence: TV2 correction owner; existing decision owner and cross-review assignments above unchanged. TV1 privacy/security and TV3 membership/history cross-review; IT04 IT05 IT12 IT15 IT16 AT05 AT06 AT08 AT18 AT19 AT22 AT23 paired schedules, runtime NOT RUN. G0/G1/G2 and dependent G3/G6 unchanged. **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION**.

- FIX2 R2-03 reference/sections: Draft FIX2 §2.1/§4/§8.1–8.2/§9/§14/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-03 preferred PROPOSED candidate/alternative: Authenticated connection.ready before POST; handshake-header alternative compared. Receiving g/q route metadata; replacement uniqueness, close/closed disposal, connection cleanup. Limits remain proposals.
- FIX2 R2-03 rationale/baseline impact: Removes first-generation circularity and fences old subscriptions without changing durable identity or account epoch. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-03 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-03 baseline/activation: G2/G4; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-03 acceptance: IT02 IT03 IT12 IT15 AT12 AT16 AT22 AT27; runtime NOT RUN.
- FIX2 R2-03 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D21 — Room, process death và outbox recovery

- Unique decision ID: **D21**.
- Category: TV2; Room,workers,Session.
- Decision question: Room, process death và outbox recovery sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 CHAT-02/ROT-01; S4 in-memory. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-10 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Persist thế nào, xóa/khóa cache logout, reconcile epoch mới.
- Available alternatives: Persist thế nào, xóa/khóa cache logout, reconcile epoch mới.
- Recommended option: **PROPOSED — Atomic durable cache/coverage checkpoint design before WS; per-user frozen/current separation and separately authorized process-recovery UX**.
- Recommendation rationale: Epoch mới không phải server identity mới; không dispatch stale stored job.
- Owner: **TV2**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: Room,workers,Session.
- Implementation impact: Cần TV1 secret/cache policy, DAO transaction và process-death tests.
- Historical01B priority: Full P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Room, process death và outbox recovery.
- Blocking milestone: G2 durable applied checkpoint/atomic cache design; G6 recovery UX; dependent feature: WS durable apply then full Room/process recovery. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G2 durable applied checkpoint/atomic cache design; G6 recovery UX.
- Feature activation gate: WS durable apply then full Room/process recovery; separately authorized after relevant acceptance evidence.
- Dependency rationale: A design blocks WS even if advanced recovery activates later.
- Related findings: CR-02 CR-09 IR-15 IR-16.
- Related acceptance tests: IT02 IT09 IT15 IT16 AT28.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-10 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.
- Addendum Host dependency: TV1 owns App Shell/Home/SessionCoordinator/System Back and assembles/retains the TV2 runtime; TV2 owns MessagingController/Router/Presentation/Execution and adapter. Proposed core.integration signatures/package need TV1/TV3 review. CORE-04A FIX1 and TV2-R13-01 are unresolved until verified new revision.

- FIX2 R2-01 reference/sections: Draft FIX2 §3/§4/§7/§9.1a/§9.2–9.5/§10.2/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-01 preferred PROPOSED candidate/alternative: Embedded snapshot-bound requester MembershipBaseline; reuse member/interval schemas. Separate resource only on demonstrated capacity/schema need with equivalent bindings. No extra roster grant. Period UUID plus role/authority/history revisions fence every capability.
- FIX2 R2-01 rationale/baseline impact: Makes cold bootstrap/full-resync A=H constructible using specified wire resources. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-01 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-01 baseline/activation: G0/G1/G2; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-01 acceptance: IT02 IT09 IT11 AT12 AT18 AT24; runtime NOT RUN.
- FIX2 R2-01 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-02 reference/sections: Draft FIX2 §4/§5.1–5.3/§7/§11/§14/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-02 preferred PROPOSED candidate/alternative: Full current MessageDTO and immutable ACK disclosure gate before equality/conflict/reconciliation; concealed404 when unauthorized. Capture immutable originMembershipPeriodId in X-Membership-Period; do not rebind uncommitted old pending work. Metadata-only reconciliation remains separately approved alternative.
- FIX2 R2-02 rationale/baseline impact: Current send authority is not visibility of old committed history; retain permanent account-key identity without hidden-key disclosure. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-02 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-02 baseline/activation: G0/G1/G2; dependent G3/G6; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-02 acceptance: IT04 IT05 IT15 IT16 AT05 AT06 AT08 AT18 AT19; runtime NOT RUN.
- FIX2 R2-02 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.
- FIX3 R3-01 candidate/trace: Draft FIX3 §5.2/§14/§16.2 uses the same canonical404 NOT_FOUND status/code/safe message/details/relevant headers/client recovery for hidden committed and absent retired-P1 keys under identical undisclosable P2 authority. A/D authorized reconciliation and authorized F/G conflict/purge/loss branches remain; no P2 send, rekey, origin rewrite or hidden ACK/SENT inference.
- FIX3 R3-01 responsibility/evidence: TV2 correction owner; existing decision owner and cross-review assignments above unchanged. TV1 privacy/security and TV3 membership/history cross-review; IT04 IT05 IT12 IT15 IT16 AT05 AT06 AT08 AT18 AT19 AT22 AT23 paired schedules, runtime NOT RUN. G0/G1/G2 and dependent G3/G6 unchanged. **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION**.


### D22 — Validation limits và protocol types

- Unique decision ID: **D22**.
- Category: Shared cross-domain; DTO,validation,backend.
- Decision question: Validation limits và protocol types sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 B.7; S4 domain only nonblank. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-07 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Độ dài body, request bytes, sequence number/string.
- Available alternatives: Độ dài body, request bytes, sequence number/string.
- Recommended option: **PROPOSED — UTF-8 body <=16000 bytes, nonblank TEXT; JSON decimal strings for bigint seq; page limit 1..100 default50**.
- Recommendation rationale: Giới hạn candidate rõ, không precision loss ở JS; chưa là server policy.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: DTO,validation,backend.
- Implementation impact: Reject unknown request fields, canonical UUID; media limits tách D18.
- Historical01B priority: Initial text P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Validation limits và protocol types.
- Blocking milestone: G0 wire bounds/types/validation; G2 batch caps; G3 media caps; dependent feature: Initial schema then feature-specific bounds. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 wire bounds/types/validation; G2 batch caps; G3 media caps.
- Feature activation gate: Initial schema then feature-specific bounds; separately authorized after relevant acceptance evidence.
- Dependency rationale: Decimal domains and schema/unknown handling must be explicit.
- Related findings: CR-07 CR-08 CR-10 IR-12.
- Related acceptance tests: IT07 IT08 IT10 IT12.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-07 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

- FIX2 R2-03 reference/sections: Draft FIX2 §2.1/§4/§8.1–8.2/§9/§14/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-03 preferred PROPOSED candidate/alternative: Authenticated connection.ready before POST; handshake-header alternative compared. Receiving g/q route metadata; replacement uniqueness, close/closed disposal, connection cleanup. Limits remain proposals.
- FIX2 R2-03 rationale/baseline impact: Removes first-generation circularity and fences old subscriptions without changing durable identity or account epoch. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-03 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-03 baseline/activation: G2/G4; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-03 acceptance: IT02 IT03 IT12 IT15 AT12 AT16 AT22 AT27; runtime NOT RUN.
- FIX2 R2-03 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-04 reference/sections: Draft FIX2 §3 literal-null convention/TypingEvent/§10.4/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-04 preferred PROPOSED candidate/alternative: Type literal null, R, Nullable Yes. Present null accepted; missing/numeric0/numeric sequence/string reject using actual table.
- FIX2 R2-04 rationale/baseline impact: Corrects normative nullability conflict while preserving ephemeral noncheckpoint semantics. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-04 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-04 baseline/activation: Document re-review; G2 typing; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-04 acceptance: IT08 IT12 IT07; runtime NOT RUN.
- FIX2 R2-04 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-05 reference/sections: Draft FIX2 §8.1/TypingPayload/§10.4/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-05 preferred PROPOSED candidate/alternative: B TTL-bounded residual; A recipient-visible retirement oracle remains alternative. Separate origin generation, receiving g/q, typingId/revision/absolute expiry; terminal STOP, any-live-device and bounded-clock fail-closed semantics.
- FIX2 R2-05 rationale/baseline impact: An unordered UUID cannot distinguish reconnect from two devices; no unconditional remote-retirement claim. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-05 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-05 baseline/activation: G2 typing; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-05 acceptance: IT07 IT15 AT14 AT16 AT27; runtime NOT RUN.
- FIX2 R2-05 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D23 — Missing 01A/draft và approval provenance

- Unique decision ID: **D23**.
- Category: Shared cross-domain; documentation,review.
- Decision question: Missing 01A/draft và approval provenance sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S3 handoff stale; source discovery; yêu cầu tác vụ. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Phục hồi bản cũ hoặc reconstruct accessible evidence.
- Available alternatives: Phục hồi bản cũ hoặc reconstruct accessible evidence.
- Recommended option: **PROPOSED — Recover original01A/previous draft if available; otherwise retain MISSING EVIDENCE and never reconstruct unavailable history or approval**.
- Recommendation rationale: Không bịa nội dung hoặc kế thừa approval không truy vết.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: documentation,review.
- Implementation impact: Bản draft review được; không production freeze.
- Historical01B priority: Nonblocking draft follow-up; freeze blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Missing 01A/draft và approval provenance.
- Blocking milestone: G7 historical provenance (nonblocking revision); dependent feature: Contract freeze/sign-off provenance. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G7 historical provenance (nonblocking revision).
- Feature activation gate: Contract freeze/sign-off provenance; separately authorized after relevant acceptance evidence.
- Dependency rationale: Missing01A/old draft does not block confirmed document corrections.
- Related findings: CR-10.
- Related acceptance tests: IT10.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.

### D24 — Translation integration và stale version

- Unique decision ID: **D24**.
- Category: TV3; translation,Messaging.
- Decision question: Translation integration và stale version sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 TR-01/TR-02; S2 §6.3. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-04 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Cache theo message hay message/version/lang; draft preview.
- Available alternatives: Cache theo message hay message/version/lang; draft preview.
- Recommended option: **PROPOSED — Translation cache messageId/version/targetLanguage, ACL tại use; draft chỉ freeze sau người dùng xác nhận send**.
- Recommendation rationale: Không đổi payload/ACK/outbox của message đã gửi.
- Owner: **TV3**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: translation,Messaging.
- Implementation impact: Privacy/consent/provider cần chốt; original không bị thay.
- Historical01B priority: P1.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Translation integration và stale version.
- Blocking milestone: G5 translation/version/ACL/provider; dependent feature: P1 translation. No implementation approval in FIX1.
- Cross-reviewers: TV1 and TV2.
- Baseline design gate: G5 translation/version/ACL/provider.
- Feature activation gate: P1 translation; separately authorized after relevant acceptance evidence.
- Dependency rationale: Derived text invalidates on recall and cannot change frozen send.
- Related findings: IR-16.
- Related acceptance tests: IT01 IT16 AT26.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-04 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

### D25 — INVITED membership hay GroupInvitation riêng

- Unique decision ID: **D25**.
- Category: TV3; Groups,DB,ACL.
- Decision question: INVITED membership hay GroupInvitation riêng sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 B.2/B.6/GROUP-01; S5 V1/GroupService. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-02, D-03 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: INVITED trong members hoặc separate pending invitations; khác biệt có trong nguồn và/hoặc original requirement so với current domain behavior.
- Available alternatives: INVITED trong members hoặc separate pending invitations.
- Recommended option: **PROPOSED — GroupInvitation riêng; chỉ accept transaction mới tạo ACTIVE membership**.
- Recommendation rationale: Phù hợp snapshot; không cấp history/WS cho invitee.
- Owner: **TV3**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: Groups,DB,ACL.
- Implementation impact: Shared schema cần cả ba duyệt; không coi PR chưa merge là APPROVED.
- Historical01B priority: Full P0 blocker.
- Status: **CONFLICT**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang INVITED membership hay GroupInvitation riêng.
- Blocking milestone: G1 invitation/member/period shared shape; dependent feature: GROUP acceptance and messaging integration. No implementation approval in FIX1.
- Cross-reviewers: TV1 and TV2.
- Baseline design gate: G1 invitation/member/period shared shape.
- Feature activation gate: GROUP acceptance and messaging integration; separately authorized after relevant acceptance evidence.
- Dependency rationale: Accept-only ACTIVE and period evidence required before shared migration.
- Related findings: CR-04 CR-09.
- Related acceptance tests: IT04 IT09 AT18.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-02, D-03 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.
- Addendum membership/history boundary: Latest-join default and ACTIVE-only access are TV3-confirmed architecture. Exact membership-transaction history boundary, previous-period exceptions, DIRECT period semantics, receipt visibility after LEFT/REMOVED, per-message audience, all/any denominator, period lifetime and authority revision publication remain OPEN.

- FIX2 R2-01 reference/sections: Draft FIX2 §3/§4/§7/§9.1a/§9.2–9.5/§10.2/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-01 preferred PROPOSED candidate/alternative: Embedded snapshot-bound requester MembershipBaseline; reuse member/interval schemas. Separate resource only on demonstrated capacity/schema need with equivalent bindings. No extra roster grant. Period UUID plus role/authority/history revisions fence every capability.
- FIX2 R2-01 rationale/baseline impact: Makes cold bootstrap/full-resync A=H constructible using specified wire resources. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-01 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-01 baseline/activation: G0/G1/G2; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-01 acceptance: IT02 IT09 IT11 AT12 AT18 AT24; runtime NOT RUN.
- FIX2 R2-01 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D26 — DIRECT pair uniqueness transaction

- Unique decision ID: **D26**.
- Category: TV2; conversation,DB.
- Decision question: DIRECT pair uniqueness transaction sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 CHAT-01; S4 Fake.openDirect. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-02 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Canonical pair unique + upsert hoặc app lock.
- Available alternatives: Canonical pair unique + upsert hoặc app lock.
- Recommended option: **PROPOSED — UNIQUE(lowUserId,highUserId), DB conflict reread sau commit trong authorization boundary**.
- Recommendation rationale: Hai process backend vẫn tạo đúng một DIRECT.
- Owner: **TV2**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: conversation,DB.
- Implementation impact: TV1 authority và schema lock order cần review; không client-generated conversation.
- Historical01B priority: Initial text P0 blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang DIRECT pair uniqueness transaction.
- Blocking milestone: G0 canonical pair/transaction retry strategy; dependent feature: Initial DIRECT create-or-get. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 canonical pair/transaction retry strategy.
- Feature activation gate: Initial DIRECT create-or-get; separately authorized after relevant acceptance evidence.
- Dependency rationale: DB uniqueness and valid loser reread are multi-instance requirements.
- Related findings: IR-14.
- Related acceptance tests: IT14 AT01 AT02.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-02 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

### D27 — WebSocket envelope và event ordering

- Unique decision ID: **D27**.
- Category: TV2; WS,DTO.
- Decision question: WebSocket envelope và event ordering sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 §8/B.8; S2 §5. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-07 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: eventType/data hoặc type/payload; serverSequence reused; khác biệt có trong nguồn và/hoặc original requirement so với current domain behavior.
- Available alternatives: eventType/data hoặc type/payload; serverSequence reused.
- Recommended option: **PROPOSED — type/payload union with immutable mutation identity and recipient projection;10 domain types (original7 plus account3),6 controls (DeliveryComplete, AppliedReport, ControlError, ConnectionReady, SubscriptionClose, SubscriptionClosed); typed stream domains and connection generations**.
- Recommendation rationale: Một event stream cho edits/receipts ngoài message order.
- Owner: **TV2**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: WS,DTO.
- Implementation impact: Không publish typing vào durable sequence; chốt với TV1/TV3.
- Historical01B priority: Full P0 blocker.
- Status: **CONFLICT**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang WebSocket envelope và event ordering.
- Blocking milestone: G0 event identity/projection fields; G2 union/control registry; dependent feature: WS replay/account feed/typing. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 event identity/projection fields; G2 union/control registry.
- Feature activation gate: WS replay/account feed/typing; separately authorized after relevant acceptance evidence.
- Dependency rationale: Extended registry10 domain types plus6 controls (DeliveryComplete, AppliedReport, ControlError, ConnectionReady, SubscriptionClose, SubscriptionClosed) must be reviewed as shared wire.
- Related findings: CR-01 CR-02 CR-03 CR-07 CR-10.
- Related acceptance tests: IT01 IT02 IT03 IT07 IT10.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-07 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.

- FIX2 R2-01 reference/sections: Draft FIX2 §3/§4/§7/§9.1a/§9.2–9.5/§10.2/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-01 preferred PROPOSED candidate/alternative: Embedded snapshot-bound requester MembershipBaseline; reuse member/interval schemas. Separate resource only on demonstrated capacity/schema need with equivalent bindings. No extra roster grant. Period UUID plus role/authority/history revisions fence every capability.
- FIX2 R2-01 rationale/baseline impact: Makes cold bootstrap/full-resync A=H constructible using specified wire resources. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-01 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-01 baseline/activation: G0/G1/G2; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-01 acceptance: IT02 IT09 IT11 AT12 AT18 AT24; runtime NOT RUN.
- FIX2 R2-01 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-03 reference/sections: Draft FIX2 §2.1/§4/§8.1–8.2/§9/§14/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-03 preferred PROPOSED candidate/alternative: Authenticated connection.ready before POST; handshake-header alternative compared. Receiving g/q route metadata; replacement uniqueness, close/closed disposal, connection cleanup. Limits remain proposals.
- FIX2 R2-03 rationale/baseline impact: Removes first-generation circularity and fences old subscriptions without changing durable identity or account epoch. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-03 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-03 baseline/activation: G2/G4; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-03 acceptance: IT02 IT03 IT12 IT15 AT12 AT16 AT22 AT27; runtime NOT RUN.
- FIX2 R2-03 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-04 reference/sections: Draft FIX2 §3 literal-null convention/TypingEvent/§10.4/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-04 preferred PROPOSED candidate/alternative: Type literal null, R, Nullable Yes. Present null accepted; missing/numeric0/numeric sequence/string reject using actual table.
- FIX2 R2-04 rationale/baseline impact: Corrects normative nullability conflict while preserving ephemeral noncheckpoint semantics. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-04 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-04 baseline/activation: Document re-review; G2 typing; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-04 acceptance: IT08 IT12 IT07; runtime NOT RUN.
- FIX2 R2-04 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.

- FIX2 R2-05 reference/sections: Draft FIX2 §8.1/TypingPayload/§10.4/§16.2; independent R2 §F retains original evidence.
- FIX2 R2-05 preferred PROPOSED candidate/alternative: B TTL-bounded residual; A recipient-visible retirement oracle remains alternative. Separate origin generation, receiving g/q, typingId/revision/absolute expiry; terminal STOP, any-live-device and bounded-clock fail-closed semantics.
- FIX2 R2-05 rationale/baseline impact: An unordered UUID cannot distinguish reconnect from two devices; no unconditional remote-retirement claim. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-05 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-05 baseline/activation: G2 typing; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-05 acceptance: IT07 IT15 AT14 AT16 AT27; runtime NOT RUN.
- FIX2 R2-05 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.
- FIX3 R3-02/R3-03 trace: Draft FIX3 §17.1 retains all47 schema mappings in one valid table; D27's recommendation/dependency now name the six existing controls, preserving10 domain types and status CONFLICT. No owner/gate or wire union is changed. **REVISION PROPOSED — AWAITING TARGETED INDEPENDENT VERIFICATION**; IT08 IT09 IT12 IT02 IT15 AT27 document checks only, runtime NOT RUN.


### D28 — Message version và stale mutation

- Unique decision ID: **D28**.
- Category: TV2; messages,cache.
- Decision question: Message version và stale mutation sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 CHAT-04; S4 ServerMessage không version. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Version integer optimistic locking hoặc last-write-wins.
- Available alternatives: Version integer optimistic locking hoặc last-write-wins.
- Recommended option: **PROPOSED — Baseline current version/tombstone/disclosure merge from first DTO; P1 sender/version CAS endpoint activation separately reviewed**.
- Recommendation rationale: Không clock-device ordering; duplicate cùng version phải tương đương.
- Owner: **TV2**; các bên bị ảnh hưởng cross-review trước merge.
- Affected modules: messages,cache.
- Implementation impact: DTO có version từ đầu; edit/revoke implementation gate riêng.
- Historical01B priority: P1.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Message version và stale mutation.
- Blocking milestone: G0 initial version/merge/tombstone semantics; G5 CAS activation; dependent feature: Initial current projection adapter then P1 edit/revoke. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G0 initial version/merge/tombstone semantics; G5 CAS activation.
- Feature activation gate: Initial current projection adapter then P1 edit/revoke; separately authorized after relevant acceptance evidence.
- Dependency rationale: Required version field/storage/reducer cannot be deferred as P1-only.
- Related findings: CR-01 CR-09 IR-15.
- Related acceptance tests: IT01 IT09 IT15 AT25.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.

### D29 — Same-epoch CHECKING/UNAVAILABLE behavior

- Unique decision ID: **D29**.
- Category: Shared cross-domain; Session,Host,transport.
- Decision question: Same-epoch CHECKING/UNAVAILABLE behavior sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S4 SessionRepository; S5 Groups SUSPENDED; S6 coordinator. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval. S9 user-provided TV3 communication confirms architecture D-04, D-10 only; full artifact/ledger and TV1 wire approval not verified.
- Conflicting requirements: Chưa có lựa chọn được khóa; các alternative sau cần authority owner thống nhất: Retain state suspended hoặc invalidate lease mỗi lần refresh.
- Available alternatives: Retain state suspended hoặc invalidate lease mỗi lần refresh.
- Recommended option: **PROPOSED — Suspend admission/render/READ/TYPING, giữ draft/outbox cùng epoch; recheck latest snapshot trước dispatch/mutation**.
- Recommendation rationale: Current Messaging active flag không tự đọc Session V2.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: Session,Host,transport.
- Implementation impact: Cần adapter authority gate; DESIGN3 invalidation không giải quyết suspension.
- Historical01B priority: Production Host/E2E blocker.
- Status: **OPEN**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Same-epoch CHECKING/UNAVAILABLE behavior.
- Blocking milestone: G4 latest snapshot same-epoch suspension; dependent feature: Production Host/auth transport. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G4 latest snapshot same-epoch suspension.
- Feature activation gate: Production Host/auth transport; separately authorized after relevant acceptance evidence.
- Dependency rationale: active flag alone cannot enforce CHECKING/UNAVAILABLE.
- Related findings: IR-15.
- Related acceptance tests: IT15 AT23.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. CORE-04A FIX1 remains unresolved dependency.
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.
- Addendum architecture alignment: D-04, D-10 (TV3-confirmed direction only); Draft §1.1 matrix, §12.1 revocation, §15.1 Host and §15.2 deliverable mapping as applicable. Existing status/gates and scoped approval requirement remain.
- Addendum Host dependency: TV1 owns App Shell/Home/SessionCoordinator/System Back and assembles/retains the TV2 runtime; TV2 owns MessagingController/Router/Presentation/Execution and adapter. Proposed core.integration signatures/package need TV1/TV3 review. CORE-04A FIX1 and TV2-R13-01 are unresolved until verified new revision.

- FIX2 R2-03 reference/sections: Draft FIX2 §2.1/§4/§8.1–8.2/§9/§14/§16.2/§17.1; independent R2 §F retains original evidence.
- FIX2 R2-03 preferred PROPOSED candidate/alternative: Authenticated connection.ready before POST; handshake-header alternative compared. Receiving g/q route metadata; replacement uniqueness, close/closed disposal, connection cleanup. Limits remain proposals.
- FIX2 R2-03 rationale/baseline impact: Removes first-generation circularity and fences old subscriptions without changing durable identity or account epoch. No dependent code/serializer/cache/transport implementation.
- FIX2 R2-03 owner/cross-review: Owner above unchanged; TV2 protocol/runtime, TV1 auth/privacy/transport, TV3 history/membership in their scope.
- FIX2 R2-03 baseline/activation: G2/G4; owner selection and independently verified required acceptance evidence before activation.
- FIX2 R2-03 acceptance: IT02 IT03 IT12 IT15 AT12 AT16 AT22 AT27; runtime NOT RUN.
- FIX2 R2-03 remaining approval: exact wire/mechanism and applicable privacy/history/clock/limits/shared integration remain pending. Existing status, evidence and gates above unchanged.


### D30 — Draft priority P0/P1 và demo scope

- Unique decision ID: **D30**.
- Category: Shared cross-domain; Android,QA.
- Decision question: Draft priority P0/P1 và demo scope sẽ dùng quy tắc và ranh giới nào trước implementation?
- Source evidence: S1 §3/B.13/ROT-01; S2 §5. Nguồn/class/section và khả năng truy cập theo inventory trên; source requirement/implementation không tự là approval.
- Conflicting requirements: Draft P1 list so với P0 persistence/rotation checklist; khác biệt có trong nguồn và/hoặc original requirement so với current domain behavior.
- Available alternatives: Draft P1 list so với P0 persistence/rotation checklist.
- Recommended option: **PROPOSED — Giữ bảo toàn draft/rotation/account isolation trong P0; advanced draft UX có thể P1**.
- Recommendation rationale: Không giảm yêu cầu đã lưu dữ liệu khi process recreation.
- Owner: **TV1/TV2/TV3**; cả ba review, không owner nào tự approve shared contract.
- Affected modules: Android,QA.
- Implementation impact: Chốt acceptance rubric; initial text slice không thay full P0.
- Historical01B priority: Full P0 blocker.
- Status: **CONFLICT**.
- Approval evidence: **Chưa có approval cho quyết định API này**. S3 chỉ ghi explicit relayed approval DESIGN3 foundation; không mở rộng sang Draft priority P0/P1 và demo scope.
- Blocking milestone: G6 P0 scope/durable state rubric; early partition/rotation design; dependent feature: Full P0/demo. No implementation approval in FIX1.
- Cross-reviewers: TV1/TV2/TV3 affected owners; no unilateral shared approval.
- Baseline design gate: G6 P0 scope/durable state rubric; early partition/rotation design.
- Feature activation gate: Full P0/demo; separately authorized after relevant acceptance evidence.
- Dependency rationale: Advanced UX may be later but basic drafts/rotation/account isolation remain P0.
- Related findings: IR-15.
- Related acceptance tests: IT15 AT28 AT30.
- FIX1 implementation impact: Follow revised Draft normative schemas/protocols and gates; no source, shared port or migration edited here. 
- FIX1 resolution posture: Recommendation PROPOSED, pending independent re-review and owner decision; status above unchanged.

## Shared unresolved decisions and approval record

Current proposed protocol resolves document-level ambiguity for re-review, while exact alternatives remain owner decisions. S9 confirms latest-join default/ACTIVE-only architecture, shared backend/pipeline, counters/cursor separation and Messaging runtime/router direction; exact boundary/exception/receipt/authority-publication contracts remain OPEN. TV1/TV2/TV3 still review global transaction strategy, snapshot/coverage feasibility, account atomic fanout/polling choice, schema version/retention/caps and proposed Host ports/resource failure/suspension. No deployed migration/checksum registry or verified new CORE-04A FIX1/TV2-R13-01 revision supplied.

Historical evidence exact paths [unavailable original source audit] and [unavailable previous contract draft] return File not found; MISSING EVIDENCE. No hashes or accepted decisions are invented for absent artifacts. Original wire alternatives crosswalk in Draft §12 is based on accessible MASTER/02/DAY1/local ports/GROUP snapshot only.

Approval evidence needed for any future acceptance: reviewer identity, date/time, exact revised document hashes, decision ID, accepted alternative, conditions and source message/URL. This document has none for full contract. General DESIGN3/foundation approval remains limited; tests/fakes/PRs do not close API decisions. Independent R2/R3 are historical inputs; FIX3 author validation is not independent verification or closure. R3 Day 1 triage remains planning evidence, not Phase 2 authorization. No further independent review or official PR review is executed automatically.

## Validation scope

Each record has source evidence, alternatives, PROPOSED recommendation/rationale, owner/cross-reviewers, baseline/activation/blocking gates, impact/status/approval, findings/tests. All refs are checked against revised Draft/Resolution Report. Actual checks and limitations are recorded in Resolution Report; no visual rendering or production acceptance PASS. Future acceptance remains NOT RUN except explicitly named document checks.
