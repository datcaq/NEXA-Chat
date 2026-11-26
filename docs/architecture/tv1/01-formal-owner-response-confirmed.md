# TV1 Formal Owner Response — D01–D30

**BẢN NHÁP · PROPOSED / PENDING HUMAN APPROVAL · 26/11/2026 (Asia/Saigon)**

Người chuẩn bị: Codex hỗ trợ Quốc Đạt (TV1). “Phản hồi” là khuyến nghị để TV1 duyệt. Mọi mục giữ trạng thái OPEN/CONFLICT của nguồn. Ngày ghi là ngày soạn, chữ ký và nguồn xác nhận của TV1 còn chờ. Dxx là Messaging FIX3, Rxx là decision register TV1 trong PR #16, D-xx của kiến trúc TV3 là namespace khác.

**Nguồn đã đối chiếu.** R = original Decision Register FIX3, X = original PR16 Cross-review, H = Handoff §B, F = Draft FIX3. Đã đối chiếu Resolution Report, R4-LITE, TV1 R01–R26 tại #16 và hai Word. [Pin, SHA và đối chiếu nguồn](formal-owner-source-reconciliation.md).

> **TV1 HUMAN CONFIRMATION — 26/11/2026 (ICT).** TV1 đã xác nhận hướng nghiệp vụ trong phần HC dưới đây. Các response D01–D30 vẫn là **AGREE WITH CONDITIONS / NEEDS EVIDENCE**, không phải approved shared contracts. Thay đổi HC01 và HC05 là policy delta so với các candidate OTP-only và friends-only của FIX3; phải được TV2/TV3 cross-review trước khi sửa API hoặc triển khai.

## D01 — API prefix

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** CONFLICT. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Chọn một prefix /api/v1 cho REST và công bố WSS route trong cùng inventory.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Kiểm tra route thật của #6, versioning, base URL và lỗi khi gọi prefix cũ.
- **Gate:** G0 prefix/schema. Activation: Initial REST/WS routes.
- **Bằng chứng nghiệm thu cần có:** Diff route/config + thử map REST/WSS với một prefix, alias chỉ khi được duyệt.
- **Nguồn ghim:** Handoff §B dòng 48, original R:26, X §F dòng 114; F §4; TV1 R09, R20 tại pin #16.
- **Phương án original:** /api/v1 hoặc /v1. **PROPOSED — /api/v1**.
- **Nghiệm thu gốc:** IT09 IT10. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ khuyến nghị, bổ sung phương án original, exact source lines và ID nghiệm thu.

## D02 — DTO JSON naming và nullability

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** CONFLICT. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Chọn messageId/body/attachmentIds/replyToMessageId; id cho conversation/attachment theo FIX3.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Chốt bảng required/null, enum, tombstone, media [], version và GROUP projection. Tách frozen request khỏi response hiện hành.
- **Gate:** G0 DTO required/nullability; G1 GROUP projection. Activation: Initial REST adapter, later GROUP/media.
- **Bằng chứng nghiệm thu cần có:** Schema diff + golden JSON đối xứng Java/server, tombstone nullable, client cũ/enum lạ và GROUP projection.
- **Nguồn ghim:** Handoff §B dòng 56, original R:53, X §F dòng 115; F §3–4/12; TV1 R09, R10, R15, R17, R23 tại pin #16.
- **Phương án original:** id/messageId; text/body; attachmentId/attachmentIds; replyToId/replyToMessageId. **PROPOSED — messageId, body, attachmentIds, replyToMessageId cho message; id cho conversation/attachment**. Addenda R:81, 89.
- **Nghiệm thu gốc:** IT08 IT09 IT10 IT13 IT15. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Thêm MembershipBaseline nhúng và TypingEvent required literal null. Missing, 0 hoặc string bị từ chối.

## D03 — 403 hay 404 và error envelope

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: domain owner, soạn ngày 26/11/2026. Owner: TV1. Cross-review: TV2 and TV3..
- **Phương án đề xuất:** Dùng canonical 404/NOT_FOUND khi resource hoặc commitment bị giấu; 403 chỉ khi resource đã được phép biết và action bị cấm.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Dùng cùng registry/renderer xuyên filter, advice, ACL, media và lỗi sync; xem D14 về nhánh hidden committed/absent.
- **Gate:** G0 error/authorization registry. Activation: Initial REST and sync controls.
- **Bằng chứng nghiệm thu cần có:** Golden so sánh status/code/body/headers/localization/cache và phân loại client cho C/E, thêm absence/nonmember/forbidden.
- **Nguồn ghim:** Handoff §B dòng 64, original R:97, X §F dòng 116; F §5.2/14; TV1 R09, R11, R13, R16 tại pin #16.
- **Phương án original:** 403 minh bạch hoặc 404 che resource; details tùy chọn. **PROPOSED — 404 cho resource không thuộc quyền nhìn; 403 cho action bị cấm trên resource đã thấy**. Addenda R:125, 131.
- **Nghiệm thu gốc:** IT12. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Bổ sung C/E canonical 404 cho hidden committed/absent retired-P1. A/D và F/G giữ điều kiện disclosure riêng.

## D04 — CredentialProvider và JWT validation

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: domain owner, soạn ngày 26/11/2026. Owner: TV1. Cross-review: TV2 and TV3..
- **Phương án đề xuất:** TV1 chọn RS256, JWT sub=server User UUID, sid=server session ID, audience đề xuất `nexa-api` (phải đối chiếu config backend); CredentialProvider single-flight theo UUID+epoch, tối đa một replay 401 đủ điều kiện. Access TTL 15 phút, refresh 30 ngày, skew 30 giây chỉ là **pilot candidate**, chưa freeze.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Xác nhận issuer/audience/keys, replay policy, clock budgets và kết quả terminal/transient theo matrix. Không đổi public Session V2.
- **Gate:** G0 credential/authority handoff; G4 Host. Activation: Auth-backed REST/WS and E2E.
- **Bằng chứng nghiệm thu cần có:** Test sai signature/issuer/audience/sub/sid/expiry, refresh cùng lúc, logout giữa refresh, ambiguous rotation và một replay.
- **Nguồn ghim:** Handoff §B dòng 72, original R:135, X §F dòng 117; F §2/14/15.1; TV1 R03, R04, R05, R06, R07, R08, R10, R20, R21 tại pin #16.
- **Phương án original:** issuer/audience/expiry, refresh/revoke và adapter chưa có. **PROPOSED — JWT sub UUID server-issued; CredentialProvider single-flight; một replay 401 có giới hạn**. Addenda R:162.
- **Nghiệm thu gốc:** IT15 AT22 AT23. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Gắn CredentialProvider/current scope với ConnectionReady và receiving g/q trước POST subscribe.

## D05 — Friendship và block policy

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: domain owner, soạn ngày 26/11/2026. Owner: TV1. Cross-review: TV2 and TV3..
- **Phương án đề xuất:** TV1 chọn hỗ trợ **Message Requests từ người lạ** (PENDING, Accept/Decline/Block/Report theo chính sách sẽ review), song **không cấp quyền mở/gửi DIRECT đầy đủ trước khi người nhận chấp nhận request**; sau Accept, DIRECT yêu cầu quan hệ được phép và không có block hai chiều. Một intro text giới hạn (nếu có) chỉ là dữ liệu request, không tự tạo Message/Conversation history. Trường hợp lời mời kết bạn gửi chéo vẫn cần chấp nhận rõ ràng; không auto-accept. TV2/TV3 cần review tương thích D05/D26.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Chốt crossed requests, unblock/unfriend, quotas và serialization theo matrix; TV3 giữ quyền quyết định GROUP.
- **Gate:** G0 current relationship serialization. Activation: Initial DIRECT/send.
- **Bằng chứng nghiệm thu cần có:** Hai hướng race block/unfriend ↔ create/send/request/accept, không tạo hàng trùng hoặc commit sau điểm chặn.
- **Nguồn ghim:** Handoff §B dòng 80, original R:170, X §F dòng 118; F §2/5.1/11; TV1 R11, R12, R13, R14 tại pin #16.
- **Phương án original:** Friends-only hoặc message requests; block một/hai chiều, group exemption. **PROPOSED — Initial DIRECT chỉ accepted friendship, không block hai chiều; group policy tách riêng**.
- **Nghiệm thu gốc:** AT04 IT14. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ khuyến nghị, bổ sung phương án original, exact source lines và ID nghiệm thu.

## D06 — DIRECT history khi block/unfriend

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: domain owner, soạn ngày 26/11/2026. Owner: TV1. Cross-review: TV2 and TV3..
- **Phương án đề xuất:** TV1 chọn giữ lại **lịch sử DIRECT đã được phép xem** sau block/unfriend, chặn tương tác mới và kiểm tra ACL hiện hành mỗi lần history/replay/media/download. Không tự xóa dữ liệu, không suy diễn cùng policy cho GROUP hoặc Message Requests chưa được chấp nhận.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Ký rõ phạm vi old period, receipts/typing, download và capability cũ. Không suy sang GROUP.
- **Gate:** G0 history/disclosure policy; G3 privacy. Activation: DIRECT history/download/replay.
- **Bằng chứng nghiệm thu cần có:** Kiểm tra history/replay/ACL trước và sau block, URL cũ và both-order delivery/revoke; thêm hidden/absent parity.
- **Nguồn ghim:** Handoff §B dòng 88, original R:197, X §F dòng 119; F §5.2/8.2/13; TV1 R09, R13, R14, R16, R19 tại pin #16.
- **Phương án original:** Giữ lịch sử cũ hoặc revoke toàn bộ quyền; thao tác mới khác đọc cũ. **PROPOSED — Giữ history đã có quyền, chặn tương tác mới theo D05; không tự xóa**.
- **Nghiệm thu gốc:** IT01 IT16. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ khuyến nghị, bổ sung phương án original, exact source lines và ID nghiệm thu.

## D07 — Shared Host Integration và resource-hook failure

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** TV1 lắp Host/factory, TV2 sở hữu account runtime/lease, TV3 giữ Groups boundary; recovery fail-closed.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Chốt một port theo IG04, current-snapshot và termination theo IG02–03, không dùng Coordinator tests thay cho bằng chứng transport.
- **Gate:** G4 Host resource policy. Activation: Production Host/E2E.
- **Bằng chứng nghiệm thu cần có:** Rotation/back/active-tab, lỗi module tùy chọn, hook throw/reentrant, account switch và APK production.
- **Nguồn ghim:** Handoff §B dòng 226, original R:224, X §F dòng 120; F §15.1; TV1 R20, R21, R22, R23 tại pin #16.
- **Phương án original:** Port chung chưa frozen; optional resource failure/throw/cleanup failure. **PROPOSED — TV1 composition root retains/injects one TV2-owned MessagingExecution/runtime and adapter; independent leases, observable fail-closed resource recovery; proposed §15.1 ports require TV1/TV3 review**.
- **Nghiệm thu gốc:** AT17 AT23 IT15. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** #13 đã official APPROVED và E03 CLOSED. Giữ gate production adapter, fail-closed và physical termination.

## D08 — Backend/schema ownership và migration registry

- **Khuyến nghị:** NEEDS EVIDENCE. **Nguồn:** OPEN. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** TV1 đồng ý đề xuất **TV3 điều phối shared Flyway/migration registry**, TV1 giữ Auth/User/Friend và security ownership, TV2 giữ Conversation/Message, TV3 giữ Groups/Membership; mỗi thay đổi cần affected-owner approval. Đây là phản hồi TV1 cho conflict D08, **SHARED APPROVAL PENDING**. Deployed DB lineage/actual Flyway history vẫn UNKNOWN; không chọn migration version, không sửa V1/DDL.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Flyway V1 UNKNOWN. Cần history/checksum được làm sạch và manifest baseline; thống nhất transaction strategy trước dependent writes.
- **Gate:** G0 registry/global transaction strategy; G1 shared schema. Activation: Any backend migration/transaction.
- **Bằng chứng nghiệm thu cần có:** Proof manifest, history/checksum và cả fresh/upgrade PostgreSQL cùng race/deadlock plan.
- **Nguồn ghim:** Handoff §B dòng 96, original R:252, X §F dòng 121; F §11/12/17; TV1 R03, R10, R13, R15, R19, R24, R25 tại pin #16.
- **Phương án original:** Registry, môi trường đã áp dụng, checksum chưa biết. **PROPOSED — Một backend; TV1 điều phối registry/security, domain owner đề xuất migration tiến**.
- **Nghiệm thu gốc:** IT09 IT14. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Original đã xác minh. Flyway V1, manifest extraction, registry và global lock/admission strategy vẫn thiếu evidence.

## D09 — Join/rejoin và history bounds

- **Khuyến nghị:** NEEDS EVIDENCE. **Nguồn:** OPEN. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV3. Cross-review: TV1 and TV2..
- **Phương án đề xuất:** Cross-review hướng ACTIVE/latest-join, ID period ổn định, tách revision authority/history/role theo FIX3.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** TV3 xác nhận inclusive/exclusive bounds, ngoại lệ, DIRECT semantics, backfill và điều kiện publication.
- **Gate:** G1 period/history; G0 shared fields if used. Activation: GROUP/receipt/replay or reviewed DIRECT-only extension.
- **Bằng chứng nghiệm thu cần có:** Leave/rejoin, pre-join leakage, version changes, inactive period, out-of-order fence.
- **Nguồn ghim:** Handoff §B dòng 104, original R:279, X §F dòng 122; F §9.1a/10/12; TV1 R13, R15, R16, R17, R19 tại pin #16.
- **Phương án original:** All history hoặc từ join; một membership row hay epochs nhiều lần join. **PROPOSED wire mechanics — Stable membershipPeriodId, separate role/authority/history revisions and visible intervals representing TV3-confirmed latest-join default/ACTIVE-only direction. Exact boundary, exceptions, DIRECT period/receipt/authority-publication policy remains OPEN; no wire approval inferred**. Addenda R:308, 316, 322.
- **Nghiệm thu gốc:** IT04 IT09 IT11. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Ghi rõ snapshot-bound MembershipBaseline nhúng, period và authority/history revisions, không mở roster. TV3 còn phải chốt bounds/ngoại lệ.

## D10 — GROUP receipt visibility và aggregation

- **Khuyến nghị:** NEEDS EVIDENCE. **Nguồn:** OPEN. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV3. Cross-review: TV1 and TV2..
- **Phương án đề xuất:** Receipt theo period và authorized roster; giữ aggregate GROUP pending.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** TV3 chọn audience và all/any/denominator qua join/leave; TV2 chốt schema receipt và redaction.
- **Gate:** G1 receipt visibility/interval semantics. Activation: GROUP receipts/unread/aggregation.
- **Bằng chứng nghiệm thu cần có:** Đổi thành viên/role, roster redaction, multi-device và aggregate không rò participant.
- **Nguồn ghim:** Handoff §B dòng 112, original R:326, X §F dòng 123; F §10/12; TV1 R15, R16, R17 tại pin #16.
- **Phương án original:** any/all recipients; denominator lúc gửi hay hiện tại; privacy. **PROPOSED — Period-specific member receipts and explicit snapshot evidence; no GROUP-wide READ/denominator choice until TV3 resolves audience, visibility and all/any policy**. Addenda R:355.
- **Nghiệm thu gốc:** IT04 IT11. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Xác nhận any/all và denominator thời điểm gửi hoặc hiện tại còn mở. Requester baseline không cho phép roster.

## D11 — WebSocket authentication/renewal

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: domain owner, soạn ngày 26/11/2026. Owner: TV1. Cross-review: TV2 and TV3..
- **Phương án đề xuất:** TV1 xác nhận hướng native WSS Bearer tại upgrade, generation mới khi credential thay đổi và reconnect có giới hạn; yêu cầu auth/expiry/sid revocation fence, không đưa bearer vào URL. Exact close/status/timeout và quyền TV2/TV3 vẫn pending.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Chốt pre/post-ready, authoritative sid revoke/expiry, frame fence và no bearer URL; bảng lỗi/close reason duyệt chung.
- **Gate:** G2 connection/auth renewal/control. Activation: WS production.
- **Bằng chứng nghiệm thu cần có:** Sai credentials HTTP trước 101, revoke giữa 101/ready và khi READY, expiry, reconnect/backoff và frame trễ.
- **Nguồn ghim:** Handoff §B dòng 268, original R:363, X §F dòng 124; F §8.1/14; TV1 R03, R05, R06, R07, R09, R18, R19 tại pin #16.
- **Phương án original:** Authorization handshake, short-lived ticket hoặc subprotocol; renewal frame hay reconnect. **PROPOSED — Native WSS Authorization handshake, reconnect có giới hạn khi credential đổi; không token trong URL**. Addenda R:391, 399.
- **Nghiệm thu gốc:** IT02 AT27. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Authenticated Ready trước POST, receiving g/q và disposal. B TTL-bounded chỉ cho residual typing, tách khỏi session revoke. Clock/TTL còn chờ.

## D12 — Durable replay architecture

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Transactional outbox + recipient-authorized projection; conversation replay theo FIX3 và đề xuất account feed từ durable per-user stream.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Chốt S/H/R/A, atomic merge+A, authority fence và retention/coverage. Polling chỉ thay account feed nếu chứng minh coverage tương đương.
- **Gate:** G0 durable mutation write/order/projection; G2 snapshot/replay/account. Activation: Initial transactional events, then replay/account feed.
- **Bằng chứng nghiệm thu cần có:** Crash commit/publish/ACK, duplicate/reorder, filtered coverage, filtered recipient, resync và membership/revoke race.
- **Nguồn ghim:** Handoff §B dòng 120, original R:407, X §F dòng 125; F §8–9/11; TV1 R13, R15, R16, R19, R24, R25 tại pin #16.
- **Phương án original:** Durable per-conversation event log hoặc transactional CDC với ordered resume token. **PROPOSED — Transactional commit-safe conversation log with recipient-authorized projection, S/H/R/A coverage protocol and independent account change feed; bounded authoritative polling remains an account-feed alternative**. Addenda R:435, 443.
- **Nghiệm thu gốc:** IT01 IT02 IT03 IT09 IT11 IT16. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Đối chiếu conversation log với ordered CDC. Durable log/outbox đề xuất; ghi rõ coverage, MembershipBaseline và account feed.

## D13 — Message sequence allocation và watermark

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV2. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Sequence riêng message/event, counter row lock đến commit, cho phép gaps theo semantics; watermark commit-safe.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Cùng rank D08; watermark là coverage, không mặc định max từ thứ tự cấp ID ngoài transaction.
- **Gate:** G0 commit-safe message/event allocation. Activation: Initial send/counters.
- **Bằng chứng nghiệm thu cần có:** Rollback/concurrent writers và hai thứ tự commit/replay; sequence không hồi quy hay làm mất event.
- **Nguồn ghim:** Handoff §B dòng 128, original R:451, X §F dòng 126; F §6/11; TV1 R15, R17, R24, R25 tại pin #16.
- **Phương án original:** DB sequence cấp trước commit hoặc counter row serialize transaction. **PROPOSED — Per-conversation counter lock giữ đến commit; eventSequence tách; gaps cho phép**.
- **Nghiệm thu gốc:** AT09 IT14. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ khuyến nghị, bổ sung phương án original, exact source lines và ID nghiệm thu.

## D14 — Frozen original payload equality và dedup retention

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** CONFLICT. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV2. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Key theo account, frozen original schema/path/bytes, committed visible reuse ACK gốc; permanent reservation sau purge là candidate.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Current authorization trước disclosure; committed comparison trước mutable new-send checks. Không suy key existence từ known retired header. Định nghĩa retention và ACK projection.
- **Gate:** G0 frozen equality/key privacy; G3 media branch. Activation: Initial send/ACK and later media/recovery.
- **Bằng chứng nghiệm thu cần có:** C/E parity, visible identical/different, send READY mất sau commit, ambiguous timeout và purge/reservation.
- **Nguồn ghim:** Handoff §B dòng 136, original R:478, X §F dòng 127; F §5/13/14; TV1 R09, R13, R14, R16, R20 tại pin #16.
- **Phương án original:** Domain equality có mutable timestamps so với send request bất biến. **PROPOSED — Account-scoped key and exact frozen original schema/path/bytes; current authorization first, committed comparison before new-send-only checks; no key reuse after purge**. Addenda R:505, 511.
- **Nghiệm thu gốc:** IT05 IT15 IT16. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ immutable originMembershipPeriodId trong X-Membership-Period khi operation gắn period. Cấm rebind/rekey; metadata-only cần phê duyệt riêng.

## D15 — Opaque pagination và snapshot

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV2. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Bốn loại token SNAPSHOT/TRAVERSAL/CONVERSATION_RESUME/ACCOUNT_RESUME, opaque và gắn context.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** TV2 xác nhận TTL, key rotation, binding UUID/period/revision, page shape và resync; mọi use check ACL.
- **Gate:** G0 token/page taxonomy; G2 account/snapshot handoff. Activation: Pagination then replay/discovery.
- **Bằng chứng nghiệm thu cần có:** Wrong type/account/generation/cursor, expiry và membership changes; snapshot coverage ổn định.
- **Nguồn ghim:** Handoff §B dòng 144, original R:515, X §F dòng 128; F §7/9; TV1 R15, R16, R17, R19 tại pin #16.
- **Phương án original:** Sequence công khai hoặc opaque scoped cursor; expiry. **PROPOSED — Separate SNAPSHOT, TRAVERSAL, CONVERSATION_RESUME and ACCOUNT_RESUME tokens; endpoint-specific list/message pages; new forward range from snapshot capability only**. Addenda R:543, 551.
- **Nghiệm thu gốc:** IT02 IT03 IT06 IT13. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Gắn cursor/snapshot với exact authority/period và receiving generation. Không dùng raw sequence thay token.

## D16 — Delivery ingress và durable device evidence

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV2. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Delivery dựa trên device ingress bền vững theo period/revision; typing ephemeral theo connection generation.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** TV3 duyệt receipt visibility, TV2 chốt device identity, ingress và TTL; socket flush không đủ evidence.
- **Gate:** G1 period delivery shape; G2 delivery/typing session protocol. Activation: Delivery receipts and typing.
- **Bằng chứng nghiệm thu cần có:** Multi-device duplicate, hidden recipient, device stale, suspension, no replay typing và TTL expiry.
- **Nguồn ghim:** Handoff §B dòng 152, original R:559, X §F dòng 129; F §8/10; TV1 R15, R16, R17, R18, R19 tại pin #16.
- **Phương án original:** WS delivered command hoặc REST delivery cursor; RAM receive hay local commit. **PROPOSED — Period/history-scoped durable device delivery ingress plus connection-bound typingId/revision sessions; no socket-flush receipt or typing replay**. Addenda R:586, 594.
- **Nghiệm thu gốc:** IT04 IT07 IT11. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** B TTL-bounded residual typing là candidate, A retirement oracle là phương án còn mở. Tách origin generation/receiving g/q, terminal STOP.

## D17 — READ message ID hay sequence cursor

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** CONFLICT. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** READ wire membershipPeriodId/historyRevision + lastReadSequence decimal string trong interval đủ quyền; lưu cả ID+sequence tại adapter.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Giải quyết V1 message ID, bounds, conversion và audience của TV3 trước write.
- **Gate:** G0 READ shape; G1 period compatibility. Activation: Initial READ, then GROUP receipts.
- **Bằng chứng nghiệm thu cần có:** Below/above interval, replay/late device, rejoin, monotonicity và mapping ID–sequence.
- **Nguồn ghim:** Handoff §B dòng 160, original R:602, X §F dòng 130; F §6/10/12; TV1 R15, R16, R17 tại pin #16.
- **Phương án original:** ID-only, sequence-only, or both server message ID and sequence; local markRead(ServerMessage) supplies both, which does not choose wire. Period/history fencing required whichever alternative. **PROPOSED — Wire membershipPeriodId/historyRevision plus decimal-string lastReadSequence and eligible interval evidence; retain both server ID and sequence in local adapter evidence**.
- **Nghiệm thu gốc:** IT04 IT09 IT10 IT11. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ khuyến nghị, bổ sung phương án original, exact source lines và ID nghiệm thu.

## D18 — Media lifecycle, endpoint và ACL

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** CONFLICT. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Giữ attachmentIds có thứ tự và [] cho TEXT baseline; upload/finalize READY rồi atomic bind cho new send.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Duyệt ACL/download/scan/expiry/quota và retry committed trước READY. Bao gồm IMAGE/FILE/basic VIDEO trong P0.
- **Gate:** G0 initial[]/type/schema; G3 media lifecycle. Activation: Full P0 media.
- **Bằng chứng nghiệm thu cần có:** Cross-account and historical ACL, temp expiry, malware/reject, same committed ACK và real file/media tests.
- **Nguồn ghim:** Handoff §B dòng 168, original R:630, X §F dòng 131; F §3/5/13; TV1 R09, R13, R16, R24, R26 tại pin #16.
- **Phương án original:** upload-init/complete hoặc media/uploads; single/multiple attachments. **PROPOSED — Baseline ordered attachmentIds with[] TEXT; media uploads/finalize READY and atomic new-send binding; committed identical retries bypass mutable READY checks**. Addenda R:658, 664.
- **Nghiệm thu gốc:** IT05 IT09 IT16. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Upload-init/complete hoặc media/uploads còn mở. Check current MessageDTO/immutable ACK disclosure trước new-send READY.

## D19 — Reply/Edit/Recall và endpoint

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** CONFLICT. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Giữ reply ref/version/tombstone ngay baseline; /messages/{messageId}/revoke và sender CAS là candidate.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Không freeze mốc 15 phút, policy reply/edit/revoke và disclosure khi target ẩn; chốt P1 riêng.
- **Gate:** G0 mutation/redaction compatibility; G5 policy/endpoints. Activation: P1 reply/edit/revoke.
- **Bằng chứng nghiệm thu cần có:** Tombstone/null golden, hidden reply, actor/CAS/time boundary và stale replay không hồi sinh nội dung.
- **Nguồn ghim:** Handoff §B dòng 176, original R:668, X §F dòng 132; F §3/6/13; TV1 R09, R13, R16, R26 tại pin #16.
- **Phương án original:** revoke/recall; window; version conflict; moderation. **PROPOSED — /messages/{messageId}/revoke; sender-only edit/revoke; expected version; 15 phút candidate**.
- **Nghiệm thu gốc:** IT01 IT10 IT16 AT25. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ khuyến nghị, bổ sung phương án original, exact source lines và ID nghiệm thu.

## D20 — Retry classification và bounded backoff

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV2. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Chỉ OFFLINE/TIMEOUT đủ điều kiện retry transport với budget; D04 điều phối retry auth tách biệt.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Timeout/cancel giữ UNKNOWN_COMMIT nếu đã dispatch, replay key cũ nếu hợp lệ. Chưa thêm scheduler. Định mức transport/circuit sau approval.
- **Gate:** G0 failure/unknown-commit classification; G4 transport bounds. Activation: Initial REST recovery then production transport.
- **Bằng chứng nghiệm thu cần có:** Never-enqueued vs dispatched, ack mất, terminal/auth 401, 429 budget, logout hủy retry và late callback.
- **Nguồn ghim:** Handoff §B dòng 234, original R:694, X §F dòng 133; F §5/14/15.1; TV1 R05, R06, R07, R09, R20, R21 tại pin #16.
- **Phương án original:** OFFLINE/TIMEOUT hiện có so với tự retry 429/5xx. **PROPOSED — Giữ auto-retry eligibility OFFLINE/TIMEOUT; không thêm scheduler; auth replay riêng do D04**. Addenda R:723, 729, 733.
- **Nghiệm thu gốc:** IT12 IT15 AT08. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Canonical concealed C/E 404 là terminal cho retry đó. Không rekey; unknown commit chỉ reconcile trong quyền hiện hành.

## D21 — Room, process death và outbox recovery

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV2. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Room per-user cache/outbox, tách frozen/current và atomic transaction projection+A; UX recovery duyệt riêng.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Chốt draft retention, durable proof trước WS, restore credential fail-closed và account/epoch isolation.
- **Gate:** G2 durable applied checkpoint/atomic cache design; G6 recovery UX. Activation: WS durable apply then full Room/process recovery.
- **Bằng chứng nghiệm thu cần có:** Crash trước/sau transaction, duplicate/out-of-order, offline recovery, logout/switch, process death và rotation.
- **Nguồn ghim:** Handoff §B dòng 242, original R:741, X §F dòng 134; F §9/15; TV1 R08, R14, R20, R21, R22, R26 tại pin #16.
- **Phương án original:** Persist thế nào, xóa/khóa cache logout, reconcile epoch mới. **PROPOSED — Atomic durable cache/coverage checkpoint design before WS; per-user frozen/current separation and separately authorized process-recovery UX**. Addenda R:770, 778, 784.
- **Nghiệm thu gốc:** IT02 IT09 IT15 IT16 AT28. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Gộp MembershipBaseline, projection và A atomically. Giữ immutable origin; không gửi old-period pending bằng period mới.

## D22 — Validation limits và protocol types

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Khởi điểm UTF-8 16000 bytes nonblank TEXT, bigint decimal string, pagination 1..100 mặc định 50.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Chốt Unicode whitespace, DTO enums/nullability, parser/overflow, caps riêng HTTP/WS/media và privacy quota.
- **Gate:** G0 wire bounds/types/validation; G2 batch caps; G3 media caps. Activation: Initial schema then feature-specific bounds.
- **Bằng chứng nghiệm thu cần có:** Boundary Unicode/multibyte, max/over-limit, invalid enum/type, overflow, oversized frame và rate-limit parity.
- **Nguồn ghim:** Handoff §B dòng 184, original R:788, X §F dòng 135; F §3/4/14; TV1 R09, R10, R12, R17, R18, R20 tại pin #16.
- **Phương án original:** Độ dài body, request bytes, sequence number/string. **PROPOSED — UTF-8 body <=16000 bytes, nonblank TEXT; JSON decimal strings for bigint seq; page limit 1..100 default50**. Addenda R:816, 824, 832.
- **Nghiệm thu gốc:** IT07 IT08 IT10 IT12. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Bổ sung literal-null typing, receiving g/q và clock vào kiểm thử giao thức trước WS.

## D23 — Missing 01A/draft và approval provenance

- **Khuyến nghị:** NEEDS EVIDENCE. **Nguồn:** OPEN. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Khôi phục 01A, draft cũ, scoped approvals và ledger; nếu thiếu thì xin cả nhóm chấp nhận limitation được ghi rõ.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Cần 01A, draft lịch sử, TV3 ledger/ADR D-08 và scoped approvals hoặc chấp nhận limitation ở G7. Hai original đã đúng pin. E03 đã đóng trong review #13.
- **Gate:** G7 historical provenance (nonblocking revision). Activation: Contract freeze/sign-off provenance.
- **Bằng chứng nghiệm thu cần có:** Manifest hash/date/reviewer/source URL hoặc xác nhận có phạm vi về limitation; không tái dựng bằng approval suy diễn.
- **Nguồn ghim:** Handoff §B dòng 276, original R:840, X §F dòng 136; F §1/18; TV1 R24, R25, R26 tại pin #16.
- **Phương án original:** Phục hồi bản cũ hoặc reconstruct accessible evidence. **PROPOSED — Recover original01A/previous draft if available; otherwise retain MISSING EVIDENCE and never reconstruct unavailable history or approval**.
- **Nghiệm thu gốc:** IT10. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Hai artifact đúng pin và E03 đã đóng phạm vi #13. Historical 01A/draft, TV3 ledger/ADR D-08 và scoped approvals vẫn thiếu ở G7.

## D24 — Translation integration và stale version

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV3. Cross-review: TV1 and TV2..
- **Phương án đề xuất:** Translation cache theo messageId/version/targetLanguage, kiểm ACL tại use, giữ original; preview trước send.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Chốt provider/consent/quota/privacy, labels/locale theo spec, stale responses sau edit/revoke; chỉ freeze draft sau xác nhận.
- **Gate:** G5 translation/version/ACL/provider. Activation: P1 translation.
- **Bằng chứng nghiệm thu cần có:** Version race, hidden/deleted message, language fallback, no stale apply sau account switch.
- **Nguồn ghim:** Handoff §B dòng 284, original R:866, X §F dòng 137; F §13/15.2; TV1 R09, R11, R16, R26 tại pin #16.
- **Phương án original:** Cache theo message hay message/version/lang; draft preview. **PROPOSED — Translation cache messageId/version/targetLanguage, ACL tại use; draft chỉ freeze sau người dùng xác nhận send**.
- **Nghiệm thu gốc:** IT01 IT16 AT26. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ khuyến nghị, bổ sung phương án original, exact source lines và ID nghiệm thu.

## D25 — INVITED membership hay GroupInvitation riêng

- **Khuyến nghị:** NEEDS EVIDENCE. **Nguồn:** CONFLICT. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV3. Cross-review: TV1 and TV2..
- **Phương án đề xuất:** Ưu tiên GroupInvitation riêng, activation membership period chỉ trong accept transaction.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Cần TV3 xác nhận enum, uniqueness, decline/expiry/reinvite, lineage V1, privacy/block policy riêng của Groups.
- **Gate:** G1 invitation/member/period shared shape. Activation: GROUP acceptance and messaging integration.
- **Bằng chứng nghiệm thu cần có:** Accept/decline/leave/rejoin, duplicate/expiry và concurrent accept/active membership.
- **Nguồn ghim:** Handoff §B dòng 192, original R:893, X §F dòng 138; F §11/12; TV1 R13, R15, R16, R24, R25 tại pin #16.
- **Phương án original:** INVITED trong members hoặc separate pending invitations. **PROPOSED — GroupInvitation riêng; chỉ accept transaction mới tạo ACTIVE membership**. Addenda R:922.
- **Nghiệm thu gốc:** IT04 IT09 AT18. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Accept tạo ACTIVE period trong transaction, gắn requester snapshot với period/authority. TV3 cần chốt exact policy.

## D26 — DIRECT pair uniqueness transaction

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV2. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Một canonical UUID pair, unique DB constraint, conflict reread có kiểm quyền.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Chốt comparator đồng nhất, lock rank trước conversation counter, không trả conversation nếu block vừa thắng fence.
- **Gate:** G0 canonical pair/transaction retry strategy. Activation: Initial DIRECT create-or-get.
- **Bằng chứng nghiệm thu cần có:** Both-order create và block/unfriend race, one pair/one conversation, conflict reread kiểm lại ACL.
- **Nguồn ghim:** Handoff §B dòng 200, original R:930, X §F dòng 139; F §4/11; TV1 R10, R13, R14, R23, R24 tại pin #16.
- **Phương án original:** Canonical pair unique + upsert hoặc app lock. **PROPOSED — UNIQUE(lowUserId,highUserId), DB conflict reread sau commit trong authorization boundary**.
- **Nghiệm thu gốc:** IT14 AT01 AT02. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ khuyến nghị, bổ sung phương án original, exact source lines và ID nghiệm thu.

## D27 — WebSocket envelope và event ordering

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** CONFLICT. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV2. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** FIX3 type/payload, immutable identity, projection theo recipient, 10 domain types và 6 controls (DeliveryComplete, AppliedReport, ControlError, ConnectionReady, SubscriptionClose, SubscriptionClosed), generation riêng.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Ràng buộc G0 identity trước write, G2 union/six controls trước activation; unknown control dừng an toàn, coverage không tự tăng.
- **Gate:** G0 event identity/projection fields; G2 union/control registry. Activation: WS replay/account feed/typing.
- **Bằng chứng nghiệm thu cần có:** Golden controls/domain envelopes, subscription close/reconnect/auth revocation, replay duplicate/order/fence.
- **Nguồn ghim:** Handoff §B dòng 208, original R:957, X §F dòng 140; F §8–9; TV1 R09, R15, R16, R18, R19, R20 tại pin #16.
- **Phương án original:** eventType/data hoặc type/payload; serverSequence reused. **PROPOSED — type/payload union with immutable mutation identity and recipient projection;10 domain types (original7 plus account3),6 controls (DeliveryComplete, AppliedReport, ControlError, ConnectionReady, SubscriptionClose, SubscriptionClosed); typed stream domains and connection generations**. Addenda R:985, 993, 1001, 1009.
- **Nghiệm thu gốc:** IT01 IT02 IT03 IT07 IT10. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Ghi 6 controls, separate sequence domains, Ready/g/q, literal null và candidate TTL typing. Dùng registry FIX3 thay snapshot cũ.

## D28 — Message version và stale mutation

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: cross-reviewer, soạn ngày 26/11/2026. Owner: TV2. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Wire current version/tombstone từ G0, apply theo version và quyền; mutation CAS tại G5.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Tách frozen equality, user/epoch và history revision; stale content không được hồi sinh sau tombstone/revoke.
- **Gate:** G0 initial version/merge/tombstone semantics; G5 CAS activation. Activation: Initial current projection adapter then P1 edit/revoke.
- **Bằng chứng nghiệm thu cần có:** ACK/cached replay/current update out-of-order, no resurrection, CAS conflict, privacy-redacted equal version.
- **Nguồn ghim:** Handoff §B dòng 216, original R:1018, X §F dòng 141; F §6/13/15; TV1 R09, R16, R17, R19, R26 tại pin #16.
- **Phương án original:** Version integer optimistic locking hoặc last-write-wins. **PROPOSED — Baseline current version/tombstone/disclosure merge from first DTO; P1 sender/version CAS endpoint activation separately reviewed**.
- **Nghiệm thu gốc:** IT01 IT09 IT15 AT25. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ khuyến nghị, bổ sung phương án original, exact source lines và ID nghiệm thu.

## D29 — Same-epoch CHECKING/UNAVAILABLE behavior

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** OPEN. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** CHECKING/UNAVAILABLE cùng UUID+epoch giữ scope/draft/outbox, ngưng admit/apply/render READ/TYPING; resume bằng latest snapshot.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Serialize invalidation với admission/apply, mình chỉ giữ buffer nếu reauthorize; terminal/account change retire và clear view. IG02–03 bắt buộc.
- **Gate:** G4 latest snapshot same-epoch suspension. Activation: Production Host/auth transport.
- **Bằng chứng nghiệm thu cần có:** Race giữa hidden tab, refresh, rotation, dispatch, apply/render; UUID/epoch change, charged retirement và resumption.
- **Nguồn ghim:** Handoff §B dòng 250, original R:1044, X §F dòng 142; F §15.1; TV1 R20, R21, R22, R23 tại pin #16.
- **Phương án original:** Retain state suspended hoặc invalidate lease mỗi lần refresh. **PROPOSED — Suspend admission/render/READ/TYPING, giữ draft/outbox cùng epoch; recheck latest snapshot trước dispatch/mutation**. Addenda R:1073.
- **Nghiệm thu gốc:** IT15 AT23. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Approval #13 cập nhật premise Coordinator. G4 vẫn cần latest snapshot ở adapter/view, receiving generation và physical proof.

## D30 — Draft priority P0/P1 và demo scope

- **Khuyến nghị:** AGREE WITH CONDITIONS. **Nguồn:** CONFLICT. Vai trò TV1: đồng owner, cần cả ba, soạn ngày 26/11/2026. Owner: TV1/TV2/TV3. Cross-review: TV1/TV2/TV3 affected owners.
- **Phương án đề xuất:** Bảo toàn 14 ngày, TEXT-only DIRECT là demo giới hạn; rotation/durable/partition và scope P0 giữ nguyên.
- **Điều kiện, xử lý tối thiểu và phụ thuộc còn lại:** Các mốc 7/11/14 chỉ dùng khi còn thời gian, không giảm full P0. Chốt acceptance rubric và fit schedule với cả nhóm.
- **Gate:** G6 P0 scope/durable state rubric; early partition/rotation design. Activation: Full P0/demo.
- **Bằng chứng nghiệm thu cần có:** Device/backend evidence cho toàn P0, module-specific E2E và boundary riêng của phần demo.
- **Nguồn ghim:** Handoff §B dòng 258, original R:1081, X §F dòng 143; F §1/17; TV1 R14, R21, R22, R25, R26 tại pin #16.
- **Phương án original:** Draft P1 list so với P0 persistence/rotation checklist. **PROPOSED — Giữ bảo toàn draft/rotation/account isolation trong P0; advanced draft UX có thể P1**.
- **Nghiệm thu gốc:** IT15 AT28 AT30. Chưa chạy trong lượt đối chiếu.
- **So với bản nháp 01:** Giữ khuyến nghị, bổ sung phương án original, exact source lines và ID nghiệm thu.


---

## HUMAN-CONFIRMED TV1 POLICY ADDENDUM (26/11/2026)

**Approval scope:** Quốc Đạt (TV1) xác nhận lựa chọn nghiệp vụ và ưu tiên ở đây; đây không phải chữ ký shared owner cho D01–D30, không approve production endpoints hoặc migration. TV2/TV3 cần duyệt phần giao thoa. Người thực hiện cần giữ lịch sử proposal cũ thay vì âm thầm thay FIX3.

| ID | TV1 xác nhận | Phần vẫn pending và tác động |
| --- | --- | --- |
| HC01 Authentication | **OTP + mật khẩu**: OTP dùng xác minh số khi đăng ký; tạo mật khẩu an toàn; đăng nhập thường bằng phone E.164 + password; OTP/step-up cho reset/change/high-risk khi phù hợp. | **Thay đổi AUTH-01/AUTH-02 inventory:** không dùng OTP-only login nữa. Password hash Argon2id hoặc BCrypt phù hợp; provider, hash cost, exact wire/DTO, login endpoint, password reset và rate limits cần thiết kế/cross-review. Không tự tạo endpoint. R01/R02, D04/D22. |
| HC02 Recovery | Step-up trên session hợp lệ và quyền số cũ; mất SIM yêu cầu proof độc lập đủ mạnh hoặc từ chối. | Recovery proof, phone recycling, support procedure, audit và revoke scope chờ policy/security review. |
| HC03 JWT/session | RS256, `sub` UUID, `sid` server ID; **audience chọn `nexa-api` ở phía TV1**; local epoch độc lập, refresh rotation/single-flight/revoke. Token `15 phút / 30 ngày / 30 giây` chỉ là giá trị thử nghiệm. | TV3 decoder/config và TV2 WSS cần xác nhận chung `nexa-api` trước production. Issuer/key custody, skew, grace, rotation race/reuse and revocation evidence chưa approved. D04/D11/D29. |
| HC04 Friend request | Hai lời mời kết bạn chéo **không tự chấp nhận**; cần explicit acceptance. | Friend state transitions và race với block/cancel/chấp nhận phải được TV1/TV2 review. |
| HC05 Message Requests | Cho phép người lạ gửi **lời yêu cầu nhắn tin**, người nhận có thể Accept/Decline/Block/Report (phạm vi lựa chọn UI/protocol tiếp tục review). **Không được tự mở unrestricted DIRECT, tạo Message durable bình thường, gửi attachment hoặc phát READ/TYPING trước khi Accept.** | Đây là **policy change** so với friends-only D05 FIX3; cần quyết định request có message preview hay chỉ intro, khi Accept có cần thành Friend hay chỉ quyền chat, expiration/spam/quota/abuse, schema, notification, identity và lịch sử. Không tự thêm REST endpoint/DTO/migration. D05/D06/D26 và R11–R14 cần owner review; TÊN TRẠNG THÁI chờ chốt. |
| HC06 History/block | Giữ lịch sử đã được phép xem; block/unfriend chặn tương tác mới; history/replay/media/download phải recheck ACL tại thời điểm dùng. | Cần chọn sau-block disclosure phạm vi nào và xử lý Message Requests, receipts và group separately. D06/D09/D14. |
| HC07 E.164 discovery | Exact authenticated lookup; **mặc định ẩn/opt-in** để được tìm thấy; reveal tối thiểu UUID + public profile được quyền xem. | Discovery quota, hidden/absent/blocked parity, logging, normalization, anti-enumeration, consent UX; không dùng phone làm Messaging principal. |
| HC08 Privacy | Canonical concealed **404** cho hidden committed vs absent retired-period key; 403 chỉ khi resource visible nhưng action không được phép; không tự refresh trên 403/404. | Header/body/null/localization parity và current full-ACK disclosure acceptance pending D03/D14. |
| HC09 Navigation/transport | TV1 owns App Shell/Composition Root; TV2 Messaging runtime/router; TV3 Groups caller. Native WSS bearer, latest snapshot fences; preserve existing Session V2. | Chỉ một port/package/signature theo agreed Host contract; IG01–IG06, physical terminal evidence còn pending. D07/D11/D20/D29. |
| HC10 D08 Backend | TV1 **đồng ý đề xuất TV3 làm registry coordinator**, không chuyển quyền sở hữu Auth/User/Security hoặc quyền approve migration cho TV3 một mình. | D08 vẫn **NEEDS EVIDENCE / SHARED APPROVAL PENDING**, full ADR D-08, target DB lineage/Flyway checksum và transaction lock order chưa chốt. |

### Targeted change-impact and owner routing

- **AUTH inventory:** sửa candidate OTP-only login thành password-based login và OTP đăng ký/step-up, phân biệt verified challenge với password credential. Rà soát tác động R01–R08, D04, D22 và test chống enumeration; không suy ra endpoint mới là approved.
- **Messaging:** Message Requests không đồng nghĩa permission DIRECT mới. TV2 phải quyết định protocol/DTO/canonical pair, D05/D26 eligibility, spam/no-bypass và cross-review FIX3 trước khi triển khai.
- **Groups:** TV3 làm rõ Message Request chỉ cho DIRECT, không vô tình chuyển thành quyền GROUP; chốt D09/D10/D25 riêng.
- **D08:** TV3 registry coordination chỉ là **TV1 owner response**; cần explicit TV2+TV3 concurrence, deployed Flyway lineage và affected-owner approval.
- **Privacy:** discovery **opt-in** khác với có thể chủ động gửi request. Sender chỉ được tạo request khi server có đúng eligibility/rate-limit; không dùng ẩn danh tính tìm kiếm để suy ra số điện thoại tài khoản.

### Review status and implementation stop gate

TV1-response recommendation tally remains **25 AGREE WITH CONDITIONS, 5 NEEDS EVIDENCE** (D08/D09/D10/D23/D25) as preliminary owner positions. The unchanged upstream FIX3 register remains 21 OPEN, 9 CONFLICT, 0 APPROVED until separate scoped sign-offs. Exact changes to D05/HC01/HC05 require targeted owner review; NO architecture/production implementation approval is claimed. Do not merge PRs, create migrations, freeze, Foundation Sync, or start Day 2 automatically.
