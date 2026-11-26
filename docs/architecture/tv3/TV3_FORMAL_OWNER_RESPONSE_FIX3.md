# TV3 Formal Owner Response — Messaging API Contract V1 FIX3

26/11/2026, Asia/Saigon. **ĐỀ XUẤT ĐỂ HOÀNG PHÚC XEM XÉT — KHÔNG PHẢI OWNER APPROVAL ĐÃ KÝ.** Codex thực hiện source/evidence review và đề xuất disposition; chỉ Hoàng Phúc quyết định policy TV3. Không gửi official GitHub review hoặc tin nhắn thay Phúc.

## 1. Executive Summary

**Final recommendation: REQUEST CHANGES trước shared baseline freeze/implementation; ACCEPT WITH CONDITIONS về hướng cho D09/D10/D24/D25.** Đây là contract/design review, không phải xác nhận triển khai hoặc production security. Có thể tiếp tục CONTRACT-01E ở phạm vi tổng hợp phản hồi/chỉnh tài liệu sau Phúc xem xét; chưa được vào DAY02.

Input FIX3 ngày25/11 gồm Draft2463 dòng, Register1117, R4-LITE259 và Handoff483; xác minh raw SHA256 trong §2. Review trên main `8b730dccc171c6d55fd8ae127448ed2b6c5f23a4`, TV3 handoff `8250b3ca04d899cb977308a979014c70bbfef94b`, Groups `2e804c033631049683cffd1d7500bb7406658bfa`, Messaging `ea239b9f66a153ac9f848fd089e5d4dd8b8874e4`. Các refs đã fetch, không giả định branch cũ vẫn đúng.

Các điểm chặn/điều kiện chính:

- **TV3-F3-01 / MEDIUM / REQUEST CHANGES:** FIX3/R4/Handoff còn ghi CORE-04A FIX1/TV2-R13-01 unresolved. Fresh GitHub xác nhận exact-head approval và finding CLOSED; chỉ cập nhật ledger hiện hành, giữ lịch sử và tách production adapter còn thiếu (§2).
- **TV3-F3-02 / HIGH / REQUEST CHANGES:** Register Messaging D08 đề xuất TV1 điều phối registry, khác TV3 coordination trong PR17/user ownership. Cần một shared owner record thống nhất (§5/§8); không tự chuyển ownership.
- **TV3-F3-03 / HIGH / NEEDS EVIDENCE:** Groups DTO/V1/service chưa có periods/revisions/Message/receipt/authority publication. Đây là missing implementation/compatibility gate đã được FIX3 thừa nhận, không phải exploit hoặc regression đã tái hiện. Cần mapping/schema/admission design được cả ba duyệt, không lấy local scope làm server ACL.
- **TV3-F3-04 / MEDIUM / NEEDS EVIDENCE:** FIX3 Resolution Report không được bàn giao/truy xuất trong phạm vi đã tìm; R3/FIX2 inputs cũng thiếu. Không thể lặp lại complete independent FIX2→FIX3 diff verification từ ZIP này.
- **TV3-F3-05 / MEDIUM / REQUEST CHANGES:** Draft Conversation.title1..120 code points khác Groups Java strip().length()120 UTF-16 code units; pagination30/page khác50/opaque. Phải có compatibility choice/adapter, không đổi source hoặc alias im lặng (§5 D02/D22).

30 Messaging decisions giữ nguyên **21 OPEN /9 CONFLICT /0 APPROVED**. Bảng §3 là đề xuất disposition theo vai trò, không đổi Register gốc. Architecture D-08 là namespace khác Messaging D08. D-08 full ADR vẫn chưa đủ evidence; preserve V1 là constraint trực tiếp, shared/prod DB UNKNOWN.

## 2. Evidence Inventory

### 2.1 Input integrity và cách đọc

ZIP gốc `D:/Nexa-Chat/NEXA-TV2-MESSAGING-FIX3-TV3-HANDOFF.zip`; SHA256 `D5F50B6977987BD7BD79660B0524F3598D2B52B7CF8EE92C223F12ED016B6871`. Giải nén an toàn chỉ bốn basename vào thư mục tạm ngoài repo; không sửa ZIP hoặc file nguồn. Bốn file đọc đầy đủ, strict UTF-8/no BOM, CRLF; physical line references là1-based theo bytes gốc. F=Draft, R=Register, IR4=R4-LITE, H=Handoff trong toàn báo cáo.

| Alias / file | Bytes / lines | SHA256 raw bytes | Integrity |
| --- | --- | --- | --- |
| F: `MESSAGING_API_V1_DRAFT_FIX3.md` | 227587 /2463 | `3E7B57B740BA448C257ECA3445C1075D0B85C665055C69E2E4FB4F7843E9F9CA` | MATCH hash pinned by H |
| R: `MESSAGING_API_V1_DECISION_REGISTER_FIX3.md` | 128705 /1117 | `0E41865166D33ED5E3F06455E4C4BE477814B36A96ADFE9CE7816BFED1CD9B2C` | MATCH hash pinned by H |
| IR4: `MESSAGING_API_V1_INDEPENDENT_REVIEW_R4_LITE.md` | 25079 /259 | `1C5F1F3152D4A37A0C6FE19A468FC2CFC45834C1485E4D0789D94C51F97E6FDD` | MATCH hash pinned by H |
| H: `MESSAGING_API_V1_FORMAL_OWNER_REVIEW_HANDOFF.md` | 60538 /483 | `525E0467B3F5C5D5D252E7918574B9B859773A8430017A3E1581130C8798E87A` | COMPUTED inventory; no independent expected self-hash supplied |

Artifacts được cung cấp ngoài Git: report dùng tên/hash/section/physical line, không tạo link giả tới file chưa publish. Người review cần ZIP đúng hash; các yêu cầu “stop/no further review” trong artifacts mô tả workflow TV2 cũ, không thay thế yêu cầu TV3-CONTRACT-01 hiện tại. Không thay terminology/status/IDs trong nguồn.

Checks thực hiện trong review này:30 unique decision headings và30 unique decision IDs; status21 OPEN/9 CONFLICT;50 JSON blocks parse được (gồm intentional invalid schema fixtures, không coi parse là semantic PASS);47 unique schema trace rows;16 REST rows,10 domain event rows và6 controls;30 AT +16 IT definitions. Trace table continuity và source line references quan trọng được đối chiếu. Không rerun745 historical field probes hoặc tuyên bố full schema validator PASS.

IR4 là báo cáo independent **document-level**, chỉ targeted R3-01/R3-02/R3-03 và FIX2→FIX3 regression. Nó kết luận R3-01 DESIGN DEFECT RESOLVED — OWNER APPROVAL PENDING, R3-02/03 DOCUMENT DEFECT RESOLVED; không phải độc lập production test/security approval. Runtime AT/IT vẫn NOT RUN. IR4's14-input hash/full-diff assertions là attributed evidence; ZIP chỉ đủ4 input nên không tự xác nhận lại14/15 historical matches hoặc exact FIX2 byte equality.

R3-01 current F456–482/2172–2195/2357–2377: hidden committed P1 và absent retired P1 trong cùng P2 authority đều canonical404, zero new writes, no ACK/key oracle/auto-rekey/origin rewrite. IR4C giải thích semantic parity, không chứng minh timing/size indistinguishability. F2399–2447 chứa47 schema traces trong một bảng; R965/977 và F671–690 thống nhất10 domain/6 control. Đây là document consistency có thể xem xét, privacy/transaction/runtime gates còn nguyên.

### 2.2 PR17 và9 tài liệu TV3

[PR17](https://github.com/datcaq/NEXA-Chat/pull/17) OPEN/DRAFT/unmerged, pre-edit HEAD8250b3c; no review submissions, no inline/issue comments; requested reviewers datcaq/Tender0603. Chưa có review mới để ghi đè hoặc coi là đồng thuận.

Đã đọc đủ9 tài liệu tại baseline8250b3c: [PR16 review](PR16_TV3_REVIEW.md), [audit](FOUNDATION_AUDIT.md), [D-01–D-10](DECISION_REGISTER.md), [ownership](OWNERSHIP_MATRIX.md), [readiness](IMPLEMENTATION_READINESS.md), [Groups](GROUPS_CONTRACT_READINESS.md), [backend manifest](BACKEND_FOUNDATION_PREPARATION.md), [Flyway plan](FLYWAY_VERIFICATION_PLAN.md), [TV2 tracker](TV2_DEPENDENCY_TRACKER.md).

**Dated evidence update, không xóa lịch sử:** tracker trước đây ghi full Wire/Register chưa tìm thấy trên GitHub. Nay FIX3 Wire/Register **AVAILABLE AS PROVIDED EXTERNAL INPUT**, conceptual schema/protocol **PARTIAL/PROPOSED**, approval vẫn pending; chưa có các file này trong Messaging Git branch/PR9. Nguồn cũ không bị sửa để giả vờ đã có từ trước.

### 2.3 Fresh GitHub và CORE-04A provenance

[PR13 approval đúng revision](https://github.com/datcaq/NEXA-Chat/pull/13#pullrequestreview-6062016319): TV2 Tender0603 APPROVED `5f8c081c6fadeb135fea8adcc9f22f493c13dce7`, TV2-R13-01 CLOSED. Older CHANGES_REQUESTED at1c459c5 là lịch sử. Current PR13 HEAD vẫn5f8c081, không có revision sau approval, OPEN/DRAFT/unmerged; không có post-merge runtime để claim. Source recoverable factory/invalidation/handoff boundaries có containment, retained handles/quarantine và bounded retries. Approval attests50 JVM/3 UI/15 probes; historical2/3 UI failure và provenance limits vẫn giữ. Không tự claim đã nhận/read closure report `NEXA-TV2-PR13-E03-FINAL-CLOSURE.md` hoặc rerun tests.

**Ba kết luận độc lập:** exact revision được TV2 approve: YES; finding cũ explicitly CLOSED: YES; production Host/Auth/HTTP/WS integration hoàn tất: NO. F§2.1/§15.1/§18, R D07/D29, IR4H và H G cần dated correction/current ledger pointer, không rewrite historical inputs. Các D07/D29/transport gates không được đóng chỉ bằng approval này.

PR9 vẫn ea239b9, Draft/unmerged, no reviews/issue/inline comments. PR16 vẫn fae99ae, Draft/unmerged; PR6 vẫn2e804c0. Không merge/foundation sync hoặc official review trong tác vụ.

### 2.4 Source references và test attribution

Không có class GroupMembership riêng: entity thực là ConversationMember. Không có GROUP Message/history API. Các source IDs sau là fixed GitHub refs, dùng trong matrix/scenarios.

| ID | Source / observed scope |
| --- | --- |
| GS | [GroupService create/invite/respond/requireMember](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupService.java#L41) |
| MC | [ConversationMember fields/current ACTIVE constructor](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/conversation/ConversationMember.java#L9) |
| GI | [separate PENDING invitation](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupInvitation.java#L9) |
| IL | [invitation pessimistic lock](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/InvitationRepository.java#L11) |
| GM | [wire projection gaps](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupMapper.java#L8) |
| GC | [actual /api/v1 groups routes; no Message/history](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupController.java#L13) |
| GA | [account scope, suspension/close/owned resources](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/app/src/main/java/com/nexa/chat/feature/groups/GroupsAccountScope.java#L10) |
| CL | [separate bounded physical/UI lanes](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/app/src/main/java/com/nexa/chat/feature/groups/GroupsCleanup.java#L7) |
| RR | [owned calls and settlement capacity](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/app/src/main/java/com/nexa/chat/feature/groups/data/RemoteGroupRepository.java#L12) |
| GT | [13 Postgres tests +test-only JWT/directory](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/test/java/com/nexa/chat/backend/groups/GroupPostgresTest.java#L22) |
| V1 | [canonical candidate migration](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/resources/db/migration/V1__shared_conversations_and_group_invitations.sql#L2) |

GS182–208 +IL11–13: acceptance lock/transaction; GT97–103 accept/repeat, GT135–155 insert-failure rollback, GT208–232 concurrent accept, GT265–278 OWNER-only invitation checks. Test source and historical external log inspected:25 backend tests0 failures/errors/skips,13 integration tests trên PostgreSQL16 Testcontainers; Flyway validate1/apply1. UI XML historical13/13 (Groups10/Home2/Example1),93 JVM reported in Phase2A.1; these are25Nov foundation/fixture results, not tests rerun for this review or real Auth/WS E2E. No Gradle/Maven/emulator/Docker/DB runtime started.

GA close logically fences and schedules owned cleanup, CL has2 physical+1 terminal notification workers with128 reservations per lane, RR16 per repo/64 global live-or-retiring. STARTED is enqueue claim, terminal callback/never-enqueued proof differs from cancellation, SETTLED charges owned work. Latest session Supplier/UUID epoch are local safety, not server membership/sid authority. Real TV1 credential enqueue gate still unimplemented.

## 3. Ownership Classification D01–D30

Mỗi disposition dưới đây là **PROPOSED reviewer recommendation**, không phải signed owner response. CROSS-REVIEW ONLY có thể nêu compatibility conditions nhưng không đóng decision của owner khác. Không có quyết định thực tế OUT OF SCOPE hoàn toàn vì cả30 đều có Groups/shared impact. R section/line là evidence nguyên bản; source IDs ở§2; direct/shared/cross analysis ở§4–§6.

| ID | Decision | TV2 status | TV3 ownership | TV3 disposition | Evidence | Dependency |
| --- | --- | --- | --- | --- | --- | --- |
| D01 | API prefix | CONFLICT | SHARED CO-OWNER | ACCEPT WITH CONDITIONS | R26–52; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D02 | DTO JSON naming và nullability | CONFLICT | SHARED CO-OWNER | ACCEPT WITH CONDITIONS | R53–96; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D03 | 403 hay 404 và error envelope | OPEN | CROSS-REVIEW ONLY | ACCEPT WITH CONDITIONS | R97–134; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D04 | CredentialProvider và JWT validation | OPEN | CROSS-REVIEW ONLY | NEEDS EVIDENCE | R135–169; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D05 | Friendship và block policy | OPEN | CROSS-REVIEW ONLY | NEEDS EVIDENCE | R170–196; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D06 | DIRECT history khi block/unfriend | OPEN | CROSS-REVIEW ONLY | NEEDS EVIDENCE | R197–223; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D07 | Shared Host Integration và resource-hook failure | OPEN | SHARED CO-OWNER | NEEDS EVIDENCE | R224–251; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D08 | Backend/schema ownership và migration registry | OPEN | SHARED CO-OWNER | REQUEST CHANGES | R252–278; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D09 | Join/rejoin và history bounds | OPEN | TV3 DIRECT OWNER | ACCEPT WITH CONDITIONS | R279–325; §4; GS/MC/GI/V1 | Phúc policy; TV1 privacy; TV2 sequence/schema |
| D10 | GROUP receipt visibility và aggregation | OPEN | TV3 DIRECT OWNER | ACCEPT WITH CONDITIONS | R326–362; §4; GS/MC/GI/V1 | Phúc audience/aggregation; TV1 privacy; TV2 receipts |
| D11 | WebSocket authentication/renewal | OPEN | CROSS-REVIEW ONLY | NEEDS EVIDENCE | R363–406; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D12 | Durable replay architecture | OPEN | SHARED CO-OWNER | ACCEPT WITH CONDITIONS | R407–450; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D13 | Message sequence allocation và watermark | OPEN | CROSS-REVIEW ONLY | ACCEPT WITH CONDITIONS | R451–477; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D14 | Frozen original payload equality và dedup retention | CONFLICT | CROSS-REVIEW ONLY | ACCEPT WITH CONDITIONS | R478–514; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D15 | Opaque pagination và snapshot | OPEN | CROSS-REVIEW ONLY | ACCEPT WITH CONDITIONS | R515–558; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D16 | Delivery ingress và durable device evidence | OPEN | CROSS-REVIEW ONLY | ACCEPT WITH CONDITIONS | R559–601; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D17 | READ message ID hay sequence cursor | CONFLICT | SHARED CO-OWNER | ACCEPT WITH CONDITIONS | R602–629; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D18 | Media lifecycle, endpoint và ACL | CONFLICT | SHARED CO-OWNER | ACCEPT WITH CONDITIONS | R630–667; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D19 | Reply/Edit/Recall và endpoint | CONFLICT | SHARED CO-OWNER | ACCEPT WITH CONDITIONS | R668–693; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D20 | Retry classification và bounded backoff | OPEN | CROSS-REVIEW ONLY | NEEDS EVIDENCE | R694–740; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D21 | Room, process death và outbox recovery | OPEN | CROSS-REVIEW ONLY | NEEDS EVIDENCE | R741–787; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D22 | Validation limits và protocol types | OPEN | SHARED CO-OWNER | REQUEST CHANGES | R788–839; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D23 | Missing 01A/draft và approval provenance | OPEN | SHARED CO-OWNER | NEEDS EVIDENCE | R840–865; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D24 | Translation integration và stale version | OPEN | TV3 DIRECT OWNER | ACCEPT WITH CONDITIONS | R866–892; §4; GS/MC/GI/V1 | Phúc design; TV1 consent/provider; TV2 versions |
| D25 | INVITED membership hay GroupInvitation riêng | CONFLICT | TV3 DIRECT OWNER | ACCEPT WITH CONDITIONS | R893–929; §4; GS/MC/GI/V1 | Phúc lifecycle; TV1 eligibility; TV2 discovery |
| D26 | DIRECT pair uniqueness transaction | OPEN | CROSS-REVIEW ONLY | ACCEPT WITH CONDITIONS | R930–956; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D27 | WebSocket envelope và event ordering | CONFLICT | CROSS-REVIEW ONLY | ACCEPT WITH CONDITIONS | R957–1017; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D28 | Message version và stale mutation | OPEN | CROSS-REVIEW ONLY | ACCEPT WITH CONDITIONS | R1018–1043; §6; F protocol and current source | Affected owners, approved exact contract and relevant G0–G7 gate |
| D29 | Same-epoch CHECKING/UNAVAILABLE behavior | OPEN | SHARED CO-OWNER | ACCEPT WITH CONDITIONS | R1044–1080; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |
| D30 | Draft priority P0/P1 và demo scope | CONFLICT | SHARED CO-OWNER | ACCEPT WITH CONDITIONS | R1081–1117; §5; PR17 ownership/gates | Affected owners, approved exact contract and relevant G0–G7 gate |

Counts:4 direct,12 shared,14 cross;0 owner approvals issued; no original TV2 statuses changed. No ACCEPT unconditional: missing co-owner/evidence gates remain explicit.

## 4. TV3 Direct Owner Decisions

Các policy dưới đây là phương án cụ thể để Phúc chọn, không phải policy vừa được Codex approve. Acceptance cases là future test oracles, NOT RUN; existing source evidence không thay shared schema review.

### D09 — Join/rejoin và history bounds — ACCEPT WITH CONDITIONS

- **Existing behavior/evidence:** MC có stable member id và joinedAt/leftAt, không period/revision/interval. GS respond từ chối mọi existing member row, kể cả LEFT/REMOVED; unique(conversation,user). ACTIVE metadata reads có, rejoin/history/Message chưa có. F§3/§9.1a/§10/§12.1 là đề xuất mới.
- **TV2 proposal:** nonreused membershipPeriodId; tách roleRevision/authorityRevision/historyRevision; H-bound requester baseline; latest-join default, ACTIVE-only, exact boundaries/DIRECT/old-period exceptions OPEN. Conservative old-P1 retry404 theo F§5.2 không tự cấp exception.
- **Proposed decision:** giữ stable membership row cho pair, append một period UUID mới cho mỗi accepted join/rejoin. Không tạo period khi role đổi hoặc refresh token. Chỉ current ACTIVE period được truy cập mới. GROUP join/rejoin transaction serialize cùng Conversation message allocator/authority gate: lấy J là finalized committed message watermark tại điểm join; đề xuất visibleIntervals=[J+1,open], inclusive from, nghĩa chỉ messages được commit sau join. Empty group J=0→from1. Pending/rollback-reserved sequences không được làm floor sai; allocator overflow phải từ chối an toàn, không wrap. Đây là lựa chọn boundary cần Phúc+TV2 duyệt, chưa implemented.
- **LEFT/REMOVED:** đóng period với upper finalized watermark tại transition để audit; current status inactive vẫn deny toàn bộ new resource admission, không dùng upper bound làm quyền đọc khi inactive. Rejoin allocates P2, receipts mới0/0 NONE; không max/restore P1. REMOVED cần lời mời mới, PENDING không mở access. Đề xuất LEFT cũng qua explicit invitation trong slice đầu để tránh self-reentry policy chưa duyệt.
- **Revisions/authority:** roleRevision tăng role transition; authorityRevision tăng khi quyền/action/status thay đổi; historyRevision tăng khi interval set thay đổi. Cùng transaction persist state+revision+durable invalidation; publication phải tham gia final response/fanout admission fence trước khi báo revoke complete. Role-only change không new period; F binding roleRevision invalidates snapshot conservatively, cần TV2 review chi phí rebootstrap. Period UUID luôn fence dù counters reset/trùng.
- **Old ACK/history:** đề xuất không có previous-period exception trong default GROUP. P2 không thấy P1 body/full ACK/receipt/media/translation dù là tác giả hoặc former OWNER/ADMIN; role không vượt history. Hidden committed và absent P1 dùng cùng canonical404 profile F460–482; retain frozen key/origin/unknown outcome, zero new writes; không tự chuyển P1 pending thành P2 send. User chủ động tạo operation mới chỉ sau recovery policy rõ, không âm thầm rekey. Any exception cần signed TV3 policy +TV1 privacy +reviewed exact full-ACK disclosure; metadata-only endpoint chưa tồn tại.
- **DIRECT:** không áp GROUP rejoin policy tự động. Đề xuất một stable participation period từ canonical DIRECT creation, role MEMBER; block/unfriend suspend action/điều chỉnh visibility theo TV1 D05/D06, không tự close/reopen period chỉ vì block. DIRECT lifecycle/history vẫn NEEDS TV1/TV2 decision; UUID chung không làm policy chung. Nếu cần reentry, định nghĩa explicit transition và reset riêng trước implementation.
- **Alternatives:** full/union historical intervals có nguy cơ mở lại pre-join data và sửa approved default; latest join bao gồm message trước join cần chọn boundary khác rõ ràng; không dùng wall-clock joinedAt để order concurrent commits. Stable row+period history giữ FK; period-keyed member row đòi compatibility migration rộng hơn.
- **Consequences/conditions:** thêm period/interval/revision facts, current projection và invalidation/account discovery; review backfill không bịa historical floor; D08 registry/global lock strategy, D12/D13/D15/D17/D21/D25 compatibility bắt buộc. Không chọn A/B physical lock strategy chỉ từ policy này.
- **Acceptance criteria:** AT18/19/27, IT02/04/11/14/15 cả hai schedule join-vs-send/remove-vs-send; old cursor/baseline/token dù counter giống vẫn deny; no new P1 ACK disclosure; role-only no new period; rollback không publish period/access; all current Message/media/receipt paths kiểm ACTIVE+period+history.
- **Required co-owners:** Phúc quyết policy; TV2 sequence/query/receipt/WS/schema; TV1 identity/privacy/authority. Status nguồn D09 OPEN giữ nguyên.

### D10 — GROUP receipts — ACCEPT WITH CONDITIONS

- **Existing behavior/evidence:** V1 UUID last_read_message_id không FK Message, không read/delivered API hoặc receipt rows/events. Local/fake receipt enum không chứng minh GROUP policy. F§10 định nghĩa explicit receipt evidence và snapshot nhưng để audience/denominator OPEN.
- **TV2 proposal:** key(conversation,user,period), historyRevision fence, atomic max same history, READ<=DELIVERED, zero sentinel và KNOWN/NONE/UNKNOWN; H-consistent authorized roster, WITHHELD/REMOVED chỉ cho previously visible identities; không infer GROUP-wide READ.
- **Proposed decision:** giữ per-member/per-period receipts; delivered chỉ durable device cache eligible prefix, READ chỉ actual visible viewport prefix, read advances delivered theo policy, không dùng socket flush/server deliveryComplete làm receipt. 0 là no-evidence sentinel, không lowerBound-1 hay global read. Cursor max chỉ trong cùng period+history revision; changed history rebase phải có explicit coverage, không mượn old max. NONE proven absence khác UNKNOWN; UNKNOWN/WITHHELD/omitted không coi là unread/read hoặc negative evidence.
- **Privacy candidate:** viewer phải current ACTIVE và có quyền xem owning message. Subject thấy receipt của chính current period. Author của message có thể xem per-recipient receipt chỉ của recipient-period được entitled tại send và hiện vẫn current ACTIVE/same period, trong phạm vi message author đang được thấy. Other members mặc định không thấy peer receipt nếu không phải tác giả; roster không tự cấp qua ReceiptSnapshot.complete. Đây là proposed audience, TV1 phải review metadata/relationship leakage và TV2 phải kiểm schema/cost trước chọn. Không gửi full cursor vượt viewer-visible message range vì có thể tiết lộ hidden later activity; clamp/projection hoặc per-message evidence cần revised contract rõ. Không claim FIX3 hiện đã có per-message roster storage.
- **Removal/rejoin:** requester LEFT/REMOVED không được fetch receipts. Subject removed/LEFT: evict live named display; only previously known IDs may receive safe removal/withheld invalidation. Old period retained restricted audit if approved, không được hiện như current P2 evidence. Rejoin subject P2 bắt đầu riêng; old receipt cannot make P2 READ. H-bound roster/visibilityRevision phải nhất quán với message/member baseline và authority recheck.
- **Aggregation chưa đủ evidence để chốt:** option1 any-delivered/read là existential label “ít nhất một người”, không được render “mọi người”; option2 all uses immutable eligible recipient-period audience-at-send, excluding sender, giúp denominator không thay khi leave/rejoin nhưng cần historical audience/privacy storage; option3 all-current eligible audience có thể chuyển thành all-read chỉ vì người chưa đọc bị remove và cần historical UI semantics. Đề xuất tạm per-recipient only +explicit unavailable aggregate, không tự freeze any/all; việc chưa activate aggregation không giảm P0 acceptance khi nhóm yêu cầu aggregate. Empty audience không tự vacuous READ. UNKNOWN/WITHHELD không được lọt denominator để thành all-read.
- **Alternatives/consequences:** public receipt roster dễ lộ membership/history; sender-only scoped projection ít lộ hơn nhưng vẫn cần privacy approval và revised DTO semantics. Immutable audience-at-send là preferred review candidate cho aggregate; không chọn production model khi chưa approved.
- **Conditions/acceptance:** Phúc chọn audience/denominator/tombstone unread policy, TV1 privacy; TV2 projection distinguishes subject historyRevision vs viewer visibility generation, DB transaction max/version/no-op events, multi-device and snapshot H tests. AT13/24, IT04/11/16: remove/rejoin/hidden subjects/missing vs NONE/UNKNOWN, stale snapshot/high old cursor, sender/third-party access, zero/empty denominator và no cross-period max. Missing policy vẫn OPEN.
- **Required co-owners:** TV3 policy, TV1 privacy, TV2 transport/receipt implementation. Không có receipt implementation được chấp nhận từ prose.

### D24 — Translation/version/ACL — ACCEPT WITH CONDITIONS

- **Existing behavior/evidence:** Groups/Message translation runtime/provider/cache chưa tồn tại; F§12.1/§13.3/§15 và R D24 là design. Không có consent/retention/provider proof.
- **TV2 proposal:** cache(messageId,version,targetLanguage), ACL at use, stale result suppressed, frozen send only after user confirmed; no mutation original/ACK/outbox.
- **Proposed decision:** semantic cache identity ít nhất server namespace+conversation/messageId+source message version+normalized targetLanguage; partition authorization/account/tenant and consent policy separately. Sau khi provider/model/config được duyệt, version that transform/policy trong key để không reuse khác semantics; chưa chọn provider hay TTL. Một shared server cache có thể reuse computation nhưng không reuse permission. Local cache phải partition UUID; async operation additionally binds current epoch/period/authority/history/version và source/draft revision. Không gửi token/phone/member list hoặc extra text cho provider.
- **Authorization:** kiểm current ACTIVE/message visibility/current version trước provider dispatch, trước result admission/persist, và mỗi cache read/render/download. Revoke/edit/membership removal/consent withdrawal advance relevant fence, cancel owned work best effort, suppress stale response và quarantine/invalidate derivative. Provider input đã gửi không thể recall; giữ retention/external deletion facts UNKNOWN đến khi có evidence. Rejoin không tự unlock cache P1; P2 phải đủ latest interval và consent/current version. A cached translation không trở thành quyền đọc original.
- **Source disclosure:** chỉ translate body đã được viewer entitled; không use frozen original retained server-side hoặc old log body để dựng bản dịch của revoked/hidden message. Tombstone không translate; edit creates new source version; late v1 cannot replace v2 result even if event already seen. Source excerpts/logs/errors phải content-free; source result sensitive như original. Draft preview gắn draftRevision/account và confirmation, không rewrite FrozenSend sau dispatch.
- **Alternatives:** cache by messageId alone stale after edit/revoke; client-only cache giảm server persistence nhưng không miễn ACL/provider privacy; no-cache first slice có thể chọn nếu phù hợp UX, không biến thành retention guarantee. Provider selection/consent route/persistence/retention remains NEEDS EVIDENCE.
- **Consequences/conditions:** D19/D28 version/tombstone compatibility từ baseline, policy/version/consent invalidation pipeline, no duplicate Message pipeline. Không tự tạo translation API/schema ở đây; Phúc chọn UX/provider sau TV1 privacy evidence, TV2 source-version adapter review.
- **Acceptance:** AT26, IT01/15/16: block provider completion, edit/revoke/remove/rejoin/switch/consent withdrawal before dispatch/result/cache read; zero unauthorized source/result disclosure, no resurrection/frozen payload mutation; independent provider retention/access tests once selected. Runtime NOT RUN.
- **Required co-owners:** TV3 feature/policy, TV1 security/consent/provider privacy, TV2 Message version/replay/reducer.

### D25 — Separate GroupInvitation — ACCEPT WITH CONDITIONS

- **Existing behavior/evidence:** GI is separate PENDING row; V1 member statuses ACTIVE/LEFT/REMOVED (no INVITED/PENDING member). GS create OWNER/ACTIVE +pending invites; invitation-lock respond checks invitee, existing user/eligibility/inviter ACTIVE then atomically inserts MEMBER/ACTIVE and updates invitation. Reject no member; processed repeat409. GT proves concurrent accept one success+processed and rollback on member insert failure within isolated test fixture.
- **TV2 proposal/proposed decision:** retain separate GroupInvitation, not INVITED membership; pending invitee may inspect/respond own invitation only, cannot read chat/history/subscribe. Accept default MEMBER, never user-supplied ADMIN/OWNER. New rejoin accepts create new period while reusing stable member row after approved migration; current source rejects rejoin and is not conformant yet. Group creation current OWNER rule unchanged.
- **Lifecycle recommendation:** retain PENDING→ACCEPTED/REJECTED; cancellation OWNER-only under existing invite-management direction if later activated. Expiry is effective server-time check at locked transition; adding EXPIRED vs preserving terminal reason needs approved enum/API/migration choice, no invented current status. Reissue allocates fresh invitation identity after terminalizing expired/cancelled pending row atomically; retain invite audit history and one effective pending per group/invitee. No TTL selected. Revoked/cancelled invite cannot accept. Duplicate accept retains current409, no idempotent200 invented; if TV2 wants idempotent success it requires explicit authorized response design and compatibility version.
- **Atomicity/order:** future accept serializes group/member/invite/authority using jointly selected rank before membership transition; no blind adoption of current invitation-first lock flow into global send/remove lock order. Validate pending+unexpired+inviter permission+recipient eligibility/group state inside transaction. Accept writes member/period/revisions/invitation/event/account discovery atomically; ACK/result only after commit and current response authorization. Rollback leaves invitation pending and no new member/period/event. Removal wins first invalidates/terminalizes stale period/invite grant; accepting stale invitation cannot undo removal. Accept wins first then remove closes new period; final current access denied. OWNER succession/ADMIN remove rights still unapproved; scenario assumes authorized operator, not grants ADMIN permission.
- **Alternatives/consequences:** INVITED membership needs enum/queries/FKs and risks treating pending as active; separate invitation matches existing schema/history and least privilege. Expiry/reissue/period support still require forward migration and conflict tests; do not edit V1. Cancel/expiry audit reason and actor/time require approved persistence, not silent overwrite of old invitation.
- **Conditions/acceptance:** Phúc lifecycle/role/reissue choices, TV1 authoritative eligibility/block races, TV2 membership/discovery/wire projection; D08 migration/locks. AT18 and IT04/09/14, plus simultaneous accept/accept, accept/reject, expire/cancel/accept and accept/remove both orders, directory outage/block and forced transaction rollback; exactly one member/current period, no pending access or early publish. Existing tests do not prove these future removal/expiry paths.
- **Required co-owners:** TV3 direct policy, TV1 identity/privacy, TV2 Conversation/schema/events; R D25 stays CONFLICT until actual owner process.

## 5. Shared Decisions

| Decision | TV3 concern / Groups impact | Proposed response and exact condition | Required TV1/TV2 concurrence |
| --- | --- | --- | --- |
| D01 prefix | GS/GC already /api/v1; parallel /v1 routes fragment clients | ACCEPT WITH CONDITIONS for /api/v1 direction; one canonical prefix/versioned compatibility plan, no new alias automatically | TV1 Auth/security routes; TV2 REST/WSS base |
| D02 JSON/nullability | GM lacks watermark/myMembership/period/revisions and page vs cursor; MemberView UUID read ID differs wire seq | ACCEPT WITH CONDITIONS; explicit mapping by field, frozen/current model separation, tombstone null preserved; required new fields cannot be fabricated from joinedAt/session epoch. No GroupApi/deserialization change here | TV2 exact DTOs/version adapter; TV1 principal/public user; TV3 member schema |
| D07 Host | GroupsActivity Activity/String port vs proposed UUID ConversationEntryPoint; status/factory still missing | NEEDS EVIDENCE for exact adapter/factory/status channel, admission failure/async error/Back/tab coordination and real latest-state transport fence. FIX1 old finding closed but integration absent | TV1 composition/session/sharedHTTP; TV2 runtime/router/adapter; TV3 entry |
| D08 registry/backend | R recommended TV1 registry coordinator vs TV3 ownership matrix | REQUEST CHANGES: propose TV3 coordinates one registry/baseline, TV1 owns security/users, TV2 Conversation/Message; each domain authors own forward SQL and affected owners approve. Any reassignment must be explicit joint amendment. Also select one global A/B strategy, capacity/lock rank/authority publication and actual target history before code | All3 exact owner record and schema/transaction design; no individual freeze |
| D12 durable replay | Group accept/remove/discovery cannot lose event or leak historical frames | ACCEPT WITH CONDITIONS: same transaction durable facts+conversation/account publication, current recipient projection, S/H/R/A distinct, atomic local checkpoint/cache. Fanout-intent/polling alternative needs complete barrier/coverage proof; cannot mix eventual fanout with strong completion claim | TV2 log/protocol/storage; TV1 authority consistency/instance fencing; TV3 member mutation |
| D17 READ | V1 UUID ID cannot become seq; invisible/prejoin gap must not become read | ACCEPT WITH CONDITIONS: choose period/history+decimal seq after approved mapping, retain server ID evidence adapter-side; same-period max only, READ<=DELIVERED, viewport prefix, no cross-period inference; unknown old data never backfilled guessed | TV2 wire/allocator/receipts; TV1 privacy/device; TV3 eligible intervals |
| D18 media | Removal/rejoin changes owning-message access; public URL bypass | ACCEPT WITH CONDITIONS: current-message ACL at upload/finalize/bind/issuance and each controlled chunk; private proxy preferred review candidate for strongest revocation. Signed URL residual explicit and reviewed; no60s/TTL/provider promise selected. Committed retry skips mutable READY only after current disclosure | TV2 storage/binding/Range; TV1 provider/privacy/URL policy; TV3 membership |
| D19 reply/edit/recall | Former owner/admin cannot infer moderation; quote/translation/replay leak | ACCEPT WITH CONDITIONS: sender-only/version CAS direction, no role-derived moderation, current visible reply, tombstone floors/after-commit fanout.15min/window/repeated-revoke exact policy remain proposed | TV2 mutation/reducer; TV1 privacy; TV3 history/moderation |
| D22 bounds/types | F title120 code points vs GS47 length120 UTF-16 units; group page default30 and64KiB JSON vs Messaging50/cursor/body candidates | REQUEST CHANGES: explicit per-route unit/bounds mapping and Unicode boundary fixtures (e.g. supplementary emoji), cap whole request separately from body text; no implied global change to Groups. Decimal ranges/overflow and pending ingress/capacity must have chosen tested values | All3 serializer/limits and overload/deadline policy; TV2 fixtures, TV1 security |
| D23 provenance | Missing Resolution/FIX2/R3 prevents full reproduction; PR17 new evidence now available | NEEDS EVIDENCE: publish/retrieve exact hashes and scoped approval records; retain MISSING INPUT. Nonblocking this independent owner review, blocking any claim complete historical verification/freeze | TV2 custodian recovery; TV1/TV3 exact signed response scopes |
| D29 same-epoch | GA preserves suspended draft/result ownership but no real TV1 bridge | ACCEPT WITH CONDITIONS: keep epoch, suspend admission/render/READ/TYPING, hold bounded owned work; latest snapshot+vendor enqueue fence, recover matching scope; definitive rejection/logout/new epoch one-way close. Retain unknown physical charges, no shared pool close | TV1 authority/adapter, TV2 transport and runtime, TV3 local lifecycle |
| D30 P0/P1 | Draft/rotation/account safety cannot be deferred by TEXT-only demo | ACCEPT WITH CONDITIONS: basic preservation/isolation stays P0, durable recovery tested separately, advanced UX/P1 explicit; translation later does not remove i18n/media/GROUP requirements | All3 scope rubric; TV1 host, TV2 durable outbox, TV3 Groups/demo |

D08 physical strategy A has potentially large complete affected-user lock sets; B needs cross-instance persisted generation/commit/fanout proof. No blanket rejection of either; require concrete feasibility/capacity/deadlock/rollback plan and deterministic both-order DB tests before choosing. Current GS.respond invitation-first locking is not proof of either future global strategy.

## 6. Cross-review Findings

Không nhận ownership hoặc đổi status của các decisions sau. Các nhận xét tương thích được giới hạn ở TV3; TV1/TV2 vẫn quyết implementation/policy của họ.

| ID / owner | Compatibility observation and evidence | Required evidence / condition |
| --- | --- | --- |
| D03 TV1 | GS requireMember hides404; OWNER action denial403; F5.2 C/E semantic404 consistent direction | TV1 approves canonical message/null/header parity and no key oracle; Groups codes/status not globally remapped here |
| D04 TV1 | GC parses UUID sub; default directory/decoder fail closed; no sid authority | Approved principal/session consistency/source and real negative tests; accountEpoch not JWT |
| D05 TV1 | GroupDirectory existing invitation eligibility; no real friend/block source | Authoritative transaction-consistent block/friend policy, including pending invites; do not import DIRECT friends-only policy into all GROUP traffic |
| D06 TV1 | DIRECT history retention differs GROUP latest-join; F2/12 separates them | TV1 explicit retained-history/download/revoke decision; no GROUP exception inferred |
| D11 TV1 | F8.1 first-ready/g/q and sid recheck are design compatible with local scope fencing | TV1 expiry/skew/required claims/revoke consistency; resolve30s setup vs TV1 addendum10s handshake scope (may be different budgets, no confirmed contradiction); approved bounded refresh/reconnect |
| D13 TV2 | F6/11 counter held to commit fits D09 proposed join floor | Real transaction both-order watermark/rollback/gap/overflow tests and member/allocator lock boundary |
| D14 TV2 | Immutable account key/origin and authorized C/E parity protect P1 history | Validate frozen bytes/retention/no reuse after purge; existing Groups creation idempotency not proof of Message key semantics |
| D15 TV2 | Bound period+revisions tokens cannot reopen P1 after P2 | Current ACL final admission, unknown/expired token safe errors, actor-bound opaque capability secret redaction; no page->replay substitution |
| D16 TV2 | Durable-device eligible delivery differs server transmission; typing ephemeral | TV1 device/clock/privacy and TV3 receipt audience; no max across periods or typing coverage; F10.4 residual TTL not immediate remote cancellation |
| D20 TV2 | F14 unknown commit rules match Groups cancellation limitations | Exact transport classification/deadlines/bounded auth replay and no blanket5xx retry; physical cancel not rollback |
| D21 TV2 | Per-account frozen/current and atomic cache/A compatible local ownership | Actual Room/process-death recovery/source, current reauth then controlled reconciliation; no automatic old P1 resend |
| D26 TV2 | Shared Conversation identity; V1 has no canonical DIRECT pair constraint | PostgreSQL uniqueness/transaction retry/authority recheck, no reread in aborted tx; no half-created members |
| D27 TV2 |10 domain/6 controls verified; account removal carries only previously visible identity | Schema/stream route/authority fence/unknown-schema halt and full AT/IT runtime evidence; no public membership event invented |
| D28 TV2 | F8.2 terminal tombstone/version floors protect history/media/translation | Separate current projection from local Message non-null/frozen timestamps; edit/revoke CAS/replay/lazy-page tests; no empty-string tombstone |

Ngoài TV3-F3-01/02/05 correction requests, các missing runtime capabilities được ghi là dependency/gate, không biến thành confirmed production defects. IR4's targeted correction closure không bị tự re-open; privacy/policy vẫn cần owners.

## 7. Critical Scenarios

Expected behavior trong các future paths là **đề xuất/invariant cần duyệt**, không phải test PASS. Scenario2 “Admin remove” chỉ giả định operator đã được policy cho phép; current ADMIN enum không cấp quyền remove.

### Scenario 1 — Accept invitation

- **Preconditions:** GI PENDING, entitled invitee U, valid authority/directory; no current member. Hai transactions A/B cùng invitation.
- **Timeline:** A locks invitation; B waits. A validates pending/existence/eligibility/inviter ACTIVE, inserts MEMBER/ACTIVE, updates ACCEPTED, commit; B reads committed status and returns409 INVITATION_PROCESSED. Unique member pair is second safeguard. Response externally emitted after transaction proxy commit.
- **Expected:** one member, one accepted outcome, pending grants no group read. If member insertion/commit fails, invitation update and member/write budget rollback together; no success or discovery published. Test asserts actual row states, not only callback count.
- **Transaction boundary:** current GS.respond @Transactional +IL pessimistic lock; accept+member state same DB transaction. Future period/event/account log must join same approved boundary, not async membership creation.
- **Authorization boundary:** validated JWT invitee identity; another actor concealed404; authoritative directory inside current transaction. After commit response still needs current future disclosure gate.
- **Evidence:** GS182–208, IL, GT concurrentAcceptanceCreatesOneMembership/memberInsertFailureRollsBackAcceptance/rejectionPersistsWithoutMembership and historical25-test log. No real JWT/production directory proof.
- **Unresolved:** expiry/cancel/reissue, group state/inviter demotion/removal ordering and future lock rank; existing test does not prove accept-vs-remove.

### Scenario 2 — Remove member vs Send

- **Preconditions:** U current ACTIVE P1; authorized removal operator; valid frozen key; approved shared serializer assumed for design. Current source has neither removal nor Message/send endpoint.
- **Timeline:** both acquire agreed authority/Conversation transaction boundary. Remove wins→status/fence commit before send check→deny with zero new Message/event/binding. Send wins→one committed Message/ACK identity/event/binding retained; remove then closes access. ACK never emitted before DB commit; ACK still queued after removal must pass final current disclosure and may be denied even though send committed.
- **Expected:** no post-remove newly admitted send/fanout/replay/history; no claim cancellation rolled back earlier commit. Sender uncertainty does not free key/rekey. Already transport-admitted bytes may arrive later.
- **Transaction boundary:** persisted membership+sequence/idempotency/Message/event and final-admission publication governed by one selected global strategy; logical branch order not physical locking instruction; no DB lock across sockets.
- **Authorization boundary:** ACTIVE+current period/action at serialized write commit; current recipient ACL at final nonblocking response/frame handoff, not event queue/preflight.
- **Evidence:** F5/6/11.1–11.2/12.1, AT19/IT14 NOT RUN; current GS ACTIVE metadata check only. No claim an actual concurrent removal test passed.
- **Unresolved:** A/B lock/admission strategy, per-instance fence publication/failure recovery and exact authorized remover rights.

### Scenario 3 — Rejoin

- **Preconditions:** P1 REMOVED; new explicit invitation accepted only when currently eligible and approved schema available.
- **Timeline:** lock and terminalize valid new invite, select proposed join watermark J, create fresh P2/update stable member current period, revisions+discovery atomically; only messages committed after join visible; P1 capabilities/cache cannot silently rebase.
- **Expected:** new period UUID; receipt0/0 NONE, separate P1 audit; no old ACK/history disclosure under default; no origin rewrite/auto-retry of P1 pending. Refresh same epoch does not alter server period.
- **Transaction boundary:** future invitation/member/period/counter/history/account publication atomic; rollback grants no P2. Current GS returns409 for existing member, so this path is MISSING today.
- **Authorization boundary:** TV1 current eligibility and TV3 latest-period/current ACTIVE; server decides, Android epoch only local account fence.
- **Evidence:** MC/V1 unique pair, GS199–201; F5.2/9.1a/10/12; AT18/IT04/11 NOT RUN.
- **Unresolved:** exact floor/co-owner approval/backfill, LEFT reentry vs explicit invitation, DIRECT distinct policy and explicit old-period exceptions (none proposed as default).

### Scenario 4 — History access by state

- **Preconditions:** group has committed messages before/after each period; viewer state and role are authoritative.
- **Timeline/expected matrix:** ACTIVE P1 sees only approved current intervals; LEFT and REMOVED receive no new resource/history/media/receipt/translation admissions; rejoined ACTIVE P2 sees latest-period intervals only; former OWNER/ADMIN has no history bypass and inactive role has no access. Pending invite only has own invitation controls. Concurrent role/history change invalidates bound tokens/requires fresh baseline. Old downloaded copies cannot be recalled.
- **Transaction boundary:** consistent period/interval/revision S/H capture; final response admission serialized with removal/revoke; no mixed-H staged baseline.
- **Authorization boundary:** current requester ACTIVE plus owning-message interval at every page/query/replay/download, not cached joinedAt/receipt roster.
- **Evidence:** GS detail/requireMember currently metadata only; GM lacks intervals; F2/7/9.1a/12.1 and IT04/11 are proposals. No current Group history API exists.
- **Unresolved:** periods/backfill and boundary selection; retention is distinct from visibility; disband/former-role exceptions not approved.

### Scenario 5 — Receipt privacy

- **Preconditions:** A sends visible message m; U reads during eligible P1; B other member; complete authorized H-bound snapshot.
- **Timeline:** durable eligible receive advances U delivered; viewport prefix read advances READ and implied delivered atomically. A's proposed sender-scoped projection can show entitled U evidence, B receives none unless separately allowed. U removed→new U ingress denied and named live display evicted; existing A/U evidence kept only as approved historical resource, no new identity disclosure. U rejoins P2→new0/0; delayed P1 cursor never updates P2.
- **Expected:** monotonic within same period/history; UNKNOWN/WITHHELD/omission never READ, NONE only proven0/0; no GROUP-wide all/any result inferred. If aggregate later chosen, immutable send-audience vs current audience documented; removal cannot silently manufacture all-read.
- **Transaction boundary:** current authority check+receipt max/version/event writes same boundary; snapshot receipt/member/page H coherent; visibility invalidation dominates stale cached high cursor.
- **Authorization boundary:** viewer current ACTIVE and message/history entitlement, subject/audience privacy projection independently checked; ReceiptSnapshot.complete is not full roster permission.
- **Evidence:** F10.1–10.3, R D10/D17, IT04/11/AT24 NOT RUN; V1 has no such receipt store.
- **Unresolved:** audience choice, per-message projection feasibility, denominator and empty audience, tombstone unread policy, subject history vs viewer visibility revision mapping.

### Scenario 6 — Media authorization

- **Preconditions:** U can view owning message/attachment; provider storage private. Removal may race authorization and URL issuance.
- **Timeline:** if removal commits before final URL/proxy issuance, deny; if URL issued first then remove, stop future authorized API issuance and controlled proxy chunks. Storage may still honor old signed URL until explicit expiry/provider revocation; authorization-at-issuance does not imply continuous storage enforcement. Rejoin does not regrant old P1 attachment outside P2 floor. Revoke blocks new refs/body/content admissions and records tombstone floor.
- **Expected:** no fresh grant after removal; no public bucket/attachment-id shortcut; preserve explicit URL residual and previously downloaded byte limits. Upload finalized/bound data does not grant download access by uploader identity alone.
- **Transaction boundary:** approved Message/attachment ownership/binding+current ACL; final issuance/chunk handoff admission outside long DB/network locks, current fence checked.
- **Authorization boundary:** current owning-message ACL on init/finalize/new-send bind/URL issuance/every controlled Range; storage capability is distinct enforcement surface.
- **Evidence:** F12.1/13.1, IT05/16/AT20 NOT RUN; no production media source/provider/URL revocation test available.
- **Unresolved:** proxy vs signed URL risk, expiry/duration/revocation support, provider/scan/MIME/cap policy; no60s guarantee selected.

### Scenario 7 — Translation authorization

- **Preconditions:** source m v1 visible to U P1, translation cached/provider job in flight; removal/edit/revoke/rejoin may occur.
- **Timeline:** dispatch only after current ACL/consent/version check. Removal before result admission suppresses result and forbids cache access; retained cache bytes are not permission. Rejoin P2 requires fresh entitlement to m (default old P1 denied). Edit v2 invalidates v1 result; revoke tombstone forbids old text or derived translations. Account switch drops old callbacks under local epoch plus server generation fences.
- **Expected:** no stale/source text disclosure through provider/cache/render/logs, no mutation frozen original. Cancel request cannot erase provider input already submitted; provider retention/deletion unknown until evidence.
- **Transaction boundary:** persisted source-version/visibility floor with invalidation publication; async provider work off DB locks; result/cache write admission checks current version+authority, never only original request snapshot.
- **Authorization boundary:** provider dispatch, result delivery and every cache use independently authorized; user/account/period/consent context bound, shared computation cache not authorization.
- **Evidence:** F12.1/13.3/15; R D24; AT26/IT01/16 NOT RUN. Translation implementation absent.
- **Unresolved:** provider/consent/version normalization/retention/TTL/data deletion and privacy approval; do not select these by assumption.

### Scenario 8 — Account/session switch

- **Preconditions:** A owns Groups scope/repo/requests and draft; A transitions CHECKING same epoch or logout→B/new epoch with late callbacks/blocked cleanup.
- **Timeline:** same-epoch temporary uncertainty suspends protected admission/render and retains bounded A state. Definitive invalidation closes A logical gate promptly before B dependency publication; owned HTTP cancel/physical cleanup runs async; terminal UI lane independent from blocked normal cleanup. Late A callback rechecks scope/VM generation/saved-state owner and cannot mutate B. Unknown physical calls remain charged until terminal+owned tasks settlement.
- **Expected:** no new A dispatch/mutation after local close, no B state wipe by A cleanup, no shared pool/TV2 runtime shutdown. Claimed old callback can finish but cannot obtain new authority. Session revoke on server sid separately fences protected server admissions/connection; membership revoke affects one conversation without necessarily logging out user.
- **Transaction boundary:** client serialized admission/invalidation/result reducer and retained-resource ownership; server sid/membership transactions independent. No Android epoch field on wire, no epoch as DB membership revision.
- **Authorization boundary:** latest authenticated TV1 snapshot at actual vendor enqueue+result application AND current server principal/ACL; GA supplier lookup alone not atomic TV1 transport authorization.
- **Evidence:** GA/CL/RR, Phase2A.1 JVM/UI fixtures and inspected historical13-case XML; F2/15.1/D29; future IT15 actual-auth/transport NOT RUN. Current fixture tests do not prove physical production HTTP termination or server revoke.
- **Unresolved:** real TV1 credential/host adapter/shared enqueue gate, TV2 physical contract, sid consistency/connection revoke and production A/B E2E. Approved Coordinator FIX1 is necessary foundation, not sufficient integration proof.

## 8. Flyway D-08 — riêng với Messaging D08

| Layer / check | Verified evidence | Verdict |
| --- | --- | --- |
| Git historical V1 | d2141e2,45c90a7,2e804c0 đều resolve cùng blob `03e3057ee5c6bbaaf0696ed271767991c205eb8c` | MATCH; exact Git refs in§1/§2; backend unchanged across phase refs |
| Raw candidate file | Current Groups Git blob at full HEAD, raw bytes hashed (main branch has no backend/V1 checkout) | SHA256 `BDE9DCD689CBC86D4966A6C0E1327950AE89D67440E6986C0B054BAB19018E60` MATCH |
| Content/schema | V1 six tables, unique member pair, at-most-one active owner, separate PENDING invite and partial unique pending | No periods/messages/delivered cursors; owner index is not at-least-one invariant |
| Isolated test migration |25Nov Maven log validate1/applyV1;25 tests0 skips incl13 Postgres16 | Historical ephemeral test-only verification; not current target schema history dump |
| Shared development DB | No authorized sanitized identity/history/version/checksum packet | UNKNOWN; no connection attempted |
| Production DB | No access/backup/history/deploy evidence | UNKNOWN; no verification claim |
| Flyway checksum/applied version | Raw SHA256/Git blob are artifact hashes, not Flyway SQL checksum; test log version1 only | Shared/prod checksum/applied version UNKNOWN |
| Upgrade/restart/backup/restore | No dedicated target upgrade/restart/restore evidence | NOT RUN/UNKNOWN; no inference from fresh test container |

No V1 edit/rename/split/delete, V2, clean/repair/reset, second history, or shared DB connect/mutation. D-08 full ADR still NEEDS EVIDENCE; constraint to preserve V1 remains mandatory. Proposed [verification plan](FLYWAY_VERIFICATION_PLAN.md) stays applicable.

**Proposed registry/workflow (not newly approved):** TV3 coordinates one version ledger; TV1 owns users/auth/security SQL, TV2 Conversation/Message/sequence, TV3 member/period/invitation. Registry entry contains requested version (unallocated until accepted), exact script name/path/hash, author/table owner, impacted interfaces/backfill/compatibility/locking, target env alias+owner, parent history/schema facts, reviewers/approval refs and execution/restore evidence. Atomic reservation avoids duplicate version claims. All affected owners approve shared SQL; environment operator explicitly authorizes target after sanitized flyway_schema_history (installed_rank/version/script/checksum/success), schema validation and backup/restore rehearsal. Fresh+populated upgrade+restart+failure tests precede deployment; owner may choose restore/forward correction, never checksum repair to hide drift. No version is assigned by this report.

## 9. Missing Evidence

1. **MISSING INPUT — MESSAGING_API_V1_FIX3_RESOLUTION_REPORT.md**, expected SHA256 `34DBAF6ECC895718F864E9030CC728775093774C14005156C7F0DF44B89417E3`,24584 bytes/264 lines per H/IR4. Searched ZIP, all fetched remote branch trees, git log --all exact filename pattern, PR9 changed files36 and body/reviews/inline/issue comments. No matching artifact/path/attachment/ref found; no Git revision can be assigned. Branch absence is scoped search evidence, not proof private files never exist. TV2 must supply exact bytes or immutable downloadable source matching hash; no report reconstructed/read claim.
2. FIX2 Draft/Register/Resolution and independent R3 not in ZIP/repo search result; cannot independently reproduce IR4's full14-input comparison/745 prior probes from these4 files. Original01A/previous draft/full historical approval evidence remain missing; H also references IR3 absent. The two missing relative-link targets are FIX3 Resolution Report and independent R3; FIX2 Draft/Register/Resolution are additional unavailable historical references. None is silently repaired. Each missing internal link remains explicit; report references by name/hash instead of a false repository URL.
3. **CORE-04A status is VERIFIED, not missing:** exact source/review/finding closure above. Separate final E03 closure artifact and raw historical APK provenance not retrieved; no claim read. Production Host/physical termination/real credential runtime evidence remains missing; no post-merge state because PR13 unmerged.
4. Shared/dev/prod environment inventory, flyway_schema_history SQL checksum/version, upgrade/restart/backup+restore evidence; no guessed migration version.
5. TV1 approved JWT audience/issuer/times/sid consistency, UserDirectory/Friends/block/provisioning/private projection, real transport admission and secure runtime; PR16 proposals alone insufficient.
6. Phúc's detailed D09/D10/D24/D25 signed choices; shared owner agreement on registry/transaction/fanout/ports/schema; DIRECT period/history policy and receipt roster/denominator; provider consent/retention and media URL revocation support.
7. Real Message/receipt/history/WS/Room/provider code and AT/IT runtime evidence; test fixtures/domain code not production compliance. No new runtime test executed because task is documentation review; future test oracles expressly NOT RUN.

Missing inputs limit historical reproduction/freeze; independent policy/source review above remains meaningful and complete within supplied evidence. Do not demand all later P1 activation tests to merely review documents, but do not declare dependent baseline gates passed without required decisions.

## 10. Required Actions by Owner

| Owner | Action | Evidence required | Blocking level |
| --- | --- | --- | --- |
| TV1 | Confirm Auth/Identity/TrustedPrincipal/sid/JWT audience and directory/social authority; review receipt/history/provider privacy and common error profile | Exact contract/source refs, signed scoped choices and security/race acceptance plan; real runtime tests before activation | G0 authority; G2/G4 security/Host; relevant G3/G5 privacy |
| TV2 | Supply missing Resolution/R3/FIX2 provenance or explicitly bounded limitation; update current FIX1 closure references without rewriting history | Hash-matching artifacts, dated correction ledger +PR13 review URL | Historical-verification/freeze gate; stale-status correction before next handoff |
| TV2 | Reconcile registry coordinator, Group DTO/title/paging differences; provide exact schema/adapter/serialization/admission/fanout plan and host factory/status channel | Revised exact-ref contract +field mapping, Unicode fixtures, current source compatibility and planned deterministic tests | G0/G1 before dependent code; G2/G4 activation |
| TV3 / Phúc | Select/amend proposed D09 join floor/LEFT reentry, D10 audience/aggregate, D24 consent/version/cache, D25 invitation lifecycle; keep ADMIN/expiry/succession open unless separately chosen | Signed per-ID response, selected option/conditions/date/hash and TV1/TV2 cross-review | G1 periods/receipts; G5 translation; no approval from this draft |
| Joint | Resolve D01/D02/D07/D08/D12/D17/D18/D19/D22/D23/D29/D30 exact scope, schema/transaction strategy, single registry and migration lineage | All required owners exact revision/sign-off, sanitized env facts, reserved version only later, fresh/upgrade/restart/restore plan | Shared baseline/freezing and relevant activation gates |
| Each implementation owner | Execute agreed runtime acceptance after separate authorization | Real DB barriers/waits/row counts, frame/admission traces, Room/cache/epoch/device evidence and0 skipped required tests | Feature activation/E2E, not documentation authoring |

## 11. DAY01 / DAY02 Readiness

| Layer | Current verdict | Exit evidence still needed |
| --- | --- | --- |
| Documentation readiness |4 supplied docs read/integrity checked,30 decisions/scenarios reviewed; missing historical package inputs explicit | TV2 Resolution Report and owner acceptance of bounded provenance; no claim full historical reproduction |
| Contract readiness | NOT FROZEN;21 OPEN/9 CONFLICT; TV3 recommendations unsubmitted to owner authority | Phúc signed direct choices +affected owner exact-hash shared/cross responses and resolved conditions |
| Shared architecture readiness | PARTIAL direction only; registry ownership/physical strategy/authority/host adapters unresolved | Approved exact owner/schema/interface/transaction/admission packet |
| Database readiness | Git V1 MATCH; test DB historical evidence only; shared/prod UNKNOWN | Authorized sanitized history and upgrade/restart/restore evidence; approved forward-only plan |
| Implementation readiness | NOT READY for dependent shared implementation/DAY02 | Applicable G0/G1 and other baseline gates satisfied plus explicit new scoped implementation authorization; activation evidence separately required |

**DAY01 EXIT chưa đạt:** blocking owner approvals chưa có; PR17 vẫn Draft/no reviews at preflight. CONTRACT-01E có thể là bước owner consolidation/revision sau Phúc đọc phản hồi, không mặc định implementation/contract freeze. P0 Auth/Contacts/GROUP/realtime/IMAGE/FILE/basicVIDEO/i18n/rotation/durable recovery giữ nguyên. Không cần giả vờ E2E xong mới review thiết kế; không lấy document review làm E2E PASS.

## 12. Formal Response

**Proposed TV3 response for Hoàng Phúc to approve/amend, not an official approval sent to TV2:**

Tiến Đạt, mình đã review đủ4 tài liệu FIX3 và30 quyết định, đối chiếu PR17/Groups2e804c0 và GitHub exact-head reviews. Đề xuất ACCEPT WITH CONDITIONS cho D09 periods/latest-join+ACTIVE-only (join floor serialize với message counter, không carry P1), D10 per-period evidence với receipt privacy cần chốt audience/aggregate, D24 versioned derived cache+ACL/consent tại dispatch/result/use, D25 GroupInvitation riêng/accept transaction→MEMBER ACTIVE, pending không có chat access và processed repeat409 hiện tại.

Trước CONTRACT-01E consolidation, nhờ cung cấp FIX3 Resolution Report đúng hash; cập nhật current FIX1 status theo review6062016319 (old finding CLOSED, production integration vẫn open); thống nhất TV3 registry coordination với TV1 security/TV2 schema ownership; bổ sung mapping Groups DTO/Unicode title/paging và exact Host factory/status/admission plan. Shared12 decisions cần ba owner đồng thuận đúng revision. TV1 xác nhận principal/sid/directory/block/privacy/error/transport; Phúc chọn chi tiết direct policy trước official response. Chưa freeze30 decisions, chưa DAY01 EXIT/DAY02, chưa SQL/source changes. Các future scenario tests vẫn NOT RUN.

### Validation record

Review adds only this TV3 Markdown. Document checks passed: 12 required numbered sections, 30 unique matrix rows (4 direct/12 shared/14 cross), 8 complete scenario records and valid table arity; 20 ACCEPT WITH CONDITIONS/8 NEEDS EVIDENCE/2 REQUEST CHANGES are recommendations only. Original9 TV3 docs and4 TV2 inputs remain unchanged; historical missing/failed evidence preserved. Links use local existing TV3 docs or immutable verified GitHub source refs; external inputs cited by raw hash/line because not published in Git. Decision/section counts, refs/paths/Markdown table arity, complete staged diff and whitespace checks must pass before commit. No source/schema/migration/interface/Word change, no official approval/review/message, merge or Ready-for-Review transition. Final commit SHA/remote alignment is recorded in delivery and PR17 body, not a self-referential hash here.
