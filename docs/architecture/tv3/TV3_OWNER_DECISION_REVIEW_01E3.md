# CONTRACT-01E.3 — Phiếu rà soát quyết định của TV3

26/11/2026, Asia/Saigon. Người chuẩn bị: Codex. Người có thẩm quyền quyết định TV3: Hoàng Phúc.

**PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION.** Tài liệu này chuẩn bị lựa chọn; không ghi nhận Phúc, TV1 hoặc TV2 đã thông qua lựa chọn mới. Không thay thế nguyên bản Decision Register hoặc sửa shared contract.

## 1. Kết luận để Phúc sử dụng

Phúc có thể xem xét ngay năm gói dưới đây, nhưng việc có đủ thông tin để chọn policy không đồng nghĩa đủ bằng chứng để triển khai. Không có formal owner review mới trên PR17. **Day 1 Exit vẫn NOT READY.**

| Gói | Khuyến nghị của Codex, chưa được thông qua | Đánh đổi / điều còn chờ |
| --- | --- | --- |
| D08 | Chọn hướng TV3 quản trị một registry, domain owner giữ quyền schema, affected owners cùng duyệt | Tăng công phối hợp; cần quyết định chung, custodian và lineage thực tế; chưa thông qua toàn ADR |
| D09 | Giữ stable member + fresh period; đề xuất GROUP latest-period với committed J+1, không kế thừa P1 | Mất quyền xem lịch sử cũ sau rejoin theo mặc định; allocator, empty interval, legacy và DIRECT cần chốt riêng |
| D10 | Chưa chọn A/B hoặc denominator thay Phúc; chuẩn bị bảng lựa chọn độc lập | Privacy-first hạn chế disclosure; historical model tốn ACL/audience storage; aggregate chưa đủ policy |
| D25 | Giữ separate invitation và hành vi409 hiện tại; chỉ đề xuất fresh-period rejoin sau phê duyệt | Rejoin/expiry/cancel cần policy, schema và race proof; không coi enum là API đã có |
| TX | Chưa khuyến nghị chọn A, B hoặc SERIALIZABLE để triển khai | Chưa candidate nào đủ assumptions/evidence; A chỉ là phương án đã được mô tả chi tiết để đánh giá |

Tách ba mức quyết định: (1) Phúc chọn policy trong phạm vi TV3; (2) affected owners thông qua thiết kế giao thoa đúng revision; (3) cho phép triển khai/kích hoạt/deploy sau bằng chứng tương ứng. Chọn mức1 không tự cấp mức2/3.

## 2. Phạm vi đọc và evidence/provenance

### 2.1 Baseline và hồ sơ đã đọc

Local branch `docs/tv3-day01-close`, HEAD `727d01879018b5d59d5902534425be2a630e06ba`; không có tracked modification lúc bắt đầu. Giữ nguyên untracked `backend/` và bốn Phase1 drafts đã tồn tại. Không tạo commit/push hoặc thay đổi PR trong nhiệm vụ này; yêu cầu hiện tại dừng sau khi tạo review document.

Đã đọc toàn bộ sáu tài liệu bắt buộc tại baseline trên:

1. [Owner adoption draft](TV3_CONTRACT_01E2B_OWNER_ADOPTION_DRAFT.md).
2. [ADR D08](ADR-D08-SHARED-FLYWAY-REGISTRY.md).
3. [Decision cards](TV3_SHARED_ARCHITECTURE_CHECKPOINT_DECISION_CARDS.md).
4. [Checkpoint handoff](TV3_SHARED_ARCHITECTURE_CHECKPOINT_HANDOFF.md).
5. [TV3 architecture Decision Register](DECISION_REGISTER.md).
6. [TV3 Formal Owner Response FIX3](TV3_FORMAL_OWNER_RESPONSE_FIX3.md).

Hai hệ ID không đồng nghĩa: architecture D-03 là direction membership periods; Messaging D09 là detailed history decision. Architecture D-09/D-10 là security/composition root; Messaging D09/D10 là history/receipts. Historical TV3 approved directions trong architecture register không thông qua detailed Messaging policy. Full ADR D-08 vẫn chờ quyết định theo correction lịch sử.

### 2.2 Ma trận bằng chứng và quyền suy luận

| Nguồn | Revision / kiểm tra hiện tại | Phân loại | Có thể kết luận / không thể kết luận |
| --- | --- | --- | --- |
| [PR17](https://github.com/datcaq/NEXA-Chat/pull/17) | API HEAD `727d01879018b5d59d5902534425be2a630e06ba`; Draft, unmerged; reviewers datcaq/Tender0603; reviews và issue comments rỗng | Published proposal; pending approval | Bộ checkpoint đã publish; chưa có formal owner sign-off trên PR17 |
| [PR16](https://github.com/datcaq/NEXA-Chat/pull/16) | API HEAD `c48f75eacf3b9f428c1f683e4a612d7ec9638d63`; Draft, unmerged; reviews rỗng; hai issue comments cũ là yêu cầu review | Published owner confirmation có phạm vi + conditional agreement | Xác nhận business policy TV1 theo hồ sơ publish; không phải shared approval |
| [TV1 confirmation](https://github.com/datcaq/NEXA-Chat/blob/c48f75eacf3b9f428c1f683e4a612d7ec9638d63/docs/architecture/tv1/05-human-owner-confirmation-confirmed.md) và [source reconciliation](https://github.com/datcaq/NEXA-Chat/blob/c48f75eacf3b9f428c1f683e4a612d7ec9638d63/docs/architecture/tv1/formal-owner-source-reconciliation.md) | Đọc nội dung Git đúng pin; attribution gốc là user's confirmation được TV1 publish | Published scoped TV1 confirmation | OTP đăng ký/password login; registry coordination; không unrestricted DIRECT trước accept; audience phía TV1 chọn nexa-api nhưng shared concurrence còn chờ |
| [PR9](https://github.com/datcaq/NEXA-Chat/pull/9) | API HEAD `ea239b9f66a153ac9f848fd089e5d4dd8b8874e4`; Draft, unmerged; reviews/issue comments rỗng | Published Messaging revision; missing adoption evidence | Không tìm thấy formal shared adoption trong các review/comment này; branch không tự chứa external contract package |
| Phản hồi TV1/TV2 mới được chuyển trong master task trước | No-intro, chat consent độc lập friendship, TV3 coordinator, route compatibility, option C | User-reported owner response / alignment | Dùng chuẩn bị lựa chọn; chưa truy xuất được nguyên văn như một GitHub review/signature; không nâng thành explicit adoption |
| 01E.2B external report | SHA256 `1A1DA5E921236F7DDB877899A1D5BD41CAA6CA3279C135C5F9B079297D73A89C` recheck MATCH | Attributed TV2 reconciliation / cross-review input | Có báo cáo đúng bytes đã đọc ở checkpoint; các proposal và owner-pending giữ nguyên |
| Checkpoint ZIP | SHA256 `E7B55FD61D564E5F246AFB2EB87D2A6E4D9C39D593AB195333864A33909C170D` recheck MATCH | Delivered artifact integrity | Bytes khớp expected trong yêu cầu; không chứng minh approval |
| FIX3 author Resolution Report | SHA256 `34DBAF6ECC895718F864E9030CC728775093774C14005156C7F0DF44B89417E3` recheck MATCH explicit historical reference | Newly received tại checkpoint trước; author evidence | Không còn thiếu delivery của report này; không sửa câu thiếu input trong handoff cũ hoặc tự lặp lại toàn bộ independent review |
| R4-LITE và PR13 closure | Phạm vi/hash/review URL được lưu trong Formal Response FIX3 | Historical cross-review / scoped approval | R4 targeted document checks và PR13 exact FIX1 closure không phải approval Groups periods/shared transaction/production integration |
| TV3 hiện tại | Sáu tài liệu bắt buộc và review này | Recommendation; explicit owner adoption PENDING | Không có chữ ký Phúc cho lựa chọn chi tiết mới |
| 01E.2C | Chưa thấy trong PR9 body/review/comments đã kiểm tra; không có path phù hợp trong fetched Messaging/TV1 trees đã kiểm tra | Missing evidence, scoped search | Không khẳng định file không tồn tại riêng tư; cần TV2 giao đúng artifact/pin; không viết thay addendum |
| DB lineage | Không có target identity/history/config/custodian evidence | UNKNOWN | Git hash không chứng minh applied version/Flyway checksum hoặc deployed data |

FIX3 upstream register giữ **21 OPEN / 9 CONFLICT / 0 APPROVED**. Cross-review, agreement có điều kiện, user-reported alignment và explicit adoption là bốn loại khác nhau. Approval chỉ có giá trị khi có người ký, đúng revision, lựa chọn, phạm vi và điều kiện; im lặng hoặc mergeable không phải approval.

## 3. Gói D08 — Registry, domain và database authority

### Worksheet D08

1. **Existing behavior:** V1 ở Groups `2e804c033631049683cffd1d7500bb7406658bfa`; một foundation candidate trên nhánh Groups, chưa chứng minh deployed shared DB. Chưa có adopted full ADR hoặc custodian được chỉ định trong hồ sơ. TV1 đã publish hỗ trợ coordinator TV3; TV2 concurrence mới có phần reported.
2. **Proposed behavior:** TV3 quản lý inventory, proposal reservation, dependency/order, naming, raw/Flyway checksum inventory, CI/readiness và hồ sơ approval. TV1 sở hữu Auth/User/Friend/Security; TV2 Conversation/Message/Sequence; TV3 Groups/Membership. Mỗi domain author kiểm soát migration của mình; mọi domain bị ảnh hưởng phải duyệt. Custodian có quyền environment được cấp riêng; coordinator không có quyền tự deploy.
3. **Alternative A:** Một registry chung, một canonical migration chain/history, affected-owner review (hướng Codex khuyến nghị để Phúc xem xét).
4. **Alternative B:** Registry/history tách theo module hoặc một người tự duyệt toàn schema. Trong shared schema, lựa chọn này làm tăng collision/lineage hoặc mất trách nhiệm domain; chỉ nên xem xét cùng thiết kế lại ranh giới DB/transaction được phê duyệt riêng.
5. **Technical consequences:** Phải định nghĩa registry record, dependency graph, reviewer routing, conflict/reservation protocol và CI; cùng một DB không được vô tình có hai migration histories. Preserve V1; forward migration sau này cần kế hoạch fresh/populated upgrade/restart/restore.
6. **Security/privacy consequences:** TV3 không tự phê chuẩn Auth/security/user schema; sanitized lineage không chứa credentials. Quyền vận hành DB, backup/restore và quyền duyệt domain là các quyền độc lập.
7. **Compatibility impact:** Không sửa/rename/split V1 để hợp nhánh. Raw blob `03e3057ee5c6bbaaf0696ed271767991c205eb8c`, SHA256 `BDE9DCD689CBC86D4966A6C0E1327950AE89D67440E6986C0B054BAB19018E60` là identity trong ADR, không phải installed Flyway checksum. Không tạo V2 hoặc chọn số migration trong review này.
8. **Affected owners:** Phúc coordinator/Groups; TV1 domain security/users/social; TV2 Conversation/message; tất cả owner bị tác động; custodian cho environment execution.
9. **Required evidence:** Phúc adoption coordinator scope; attributable TV2 concurrence và affected-owner record; full ADR exact revision và điều kiện TX; registry protocol được duyệt; custodian identity/authority. Trước tuyên bố DB/rollout ready phải có sanitized target, PostgreSQL/Flyway versions, schemas/history location, installed_rank/version/script/checksum/success, drift/validate và backup/fresh/upgrade/restart/restore evidence.
10. **Exact decision requiring Phúc:** “Tôi chọn/chỉnh phạm vi TV3 coordinator theo Alternative A; domain và affected-owner approval không chuyển giao; custodian có quyền vận hành riêng. Full ADR và các condition ghi rõ dưới đây còn pending cho tới khi đủ xác nhận.” **PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION.** Không điền câu này như đã ký.

### Future reservation protocol để quyết định, không thực thi

Domain author nộp candidate không gán số → coordinator refresh canonical/đang-reserved inventory → owner xác định dependencies, compatibility/backfill và affected owners → affected owners duyệt → một thao tác registry có serialization/CAS được cả nhóm chọn mới nhận reservation → ghi author/version/name/parent/commit/hash/approvals → CI và custodian readiness → execution riêng được ủy quyền → append kết quả thực tế. Hai request đồng thời không được tự nhận cùng version. Candidate thay bytes/parent phải review lại reservation applicability. Không khẳng định hiện đã có registry service/CAS implementation.

Điều cản adoption toàn ADR: chưa có Phúc sign-off; chưa đủ attributable shared adoption; allocation serialization/rank/interface và TX chưa chọn; custodian chưa rõ; lineage chưa xác minh. Có thể ký nguyên tắc quản trị với điều kiện rõ trước khi rollout ready, nhưng không ký “DB đã validate” hoặc “ADR hoàn tất mọi gate”. CI/fresh/restore execution là activation evidence sau này, không phải bài test đã chạy để viết worksheet.

## 4. Gói D09 — Period và ranh giới lịch sử

### Worksheet D09

1. **Existing behavior:** Unique member pair và stable member UUID; status ACTIVE/LEFT/REMOVED, joinedAt/leftAt và UUID lastReadMessageId. Current accept từ chối mọi existing member409; chưa có period, Message/history API, rejoin hoặc allocator. Status tồn tại không chứng minh route transition đã có.
2. **Proposed behavior:** Stable member + fresh nonreused period UUID mỗi join/rejoin; role change/refresh không tạo period. GROUP current access đòi ACTIVE và latest authorized period; J là finalized committed message watermark dưới cùng authoritative transition/allocator boundary; inclusive lower bound J+1, close bound phục vụ audit chứ không cho inactive đọc. P2 bắt đầu receipt NONE riêng; không chuyển cursor/key/cache authority P1 sang P2.
3. **Alternative A:** Latest-period `[J+1,open]`; tại snapshot H, eligible committed history là giao với các messages được phép có sequence<=H. Chưa có message mới thì dữ liệu trả về rỗng dù period vẫn mở. Wire giữ future-open interval hoặc encode attested empty cần TV2 chọn.
4. **Alternative B:** Explicit bounded backfill/union intervals hoặc historical exceptions được TV3+TV1 thông qua; DIRECT stable participation hoặc consent periods chọn riêng. Không dùng timestamp/reserved sequence làm giải pháp thay J.
5. **Technical consequences:** Phải có stable FK + period/revision facts, transactional allocator, final admission, snapshot/cursor representation, conservative legacy mapping; role/authority/history revisions phân biệt. New period không chỉ là cập nhật joinedAt. Atomicity phụ thuộc TX, không tự chọn physical strategy chỉ vì chọn J+1.
6. **Security/privacy consequences:** Former OWNER/ADMIN không vượt latest-period ACL. Hidden committed P1 và absent retired-P1 key phải canonical404 tương đương dưới cùng unauthorized P2 context; không ACK oracle/auto-rekey/origin rewrite. Có bytes cache cũ không phải permission; dữ liệu đã tải trước đó không thể bị thu hồi vật lý bằng policy này.
7. **Compatibility impact:** Cần approved forward schema và wire/adapter; không tự cast UUID read ID thành seq. Existing-member409 giữ nguyên cho tới khi rejoin change được duyệt. Empty interval không được hiểu thành unrestricted; server wire chưa được thay trong nhiệm vụ này.
8. **Affected owners:** Phúc GROUP policy; TV2 allocator/model/cursor/replay/receipts; TV1 account/block/privacy và DIRECT consent/history.
9. **Required evidence:** Signed exact lower/upper/empty/legacy choices; TV2 mapping và commit-safe allocator proof; join/send/remove both-order tests, rollback/gap/overflow, old-period hidden/absent parity, two-device stale cursor, role/history revalidation và current ACL ở final response. Tất cả runtime schedules mới là NOT RUN.
10. **Exact decision requiring Phúc:** Chọn stable+fresh period; latest-only hay explicit backfill; J+1 inclusive; close upper có inclusive audit semantics nào; chọn empty representation với TV2; không nhận legacy floor đoán; LEFT/REMOVED rejoin qua lời mời mới hay policy khác; P1 exceptions có/không. DIRECT interaction chỉ ghi ranh giới, TV1/TV2 quyết chi tiết. **PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION.**

### Ví dụ state transition, không phải kết quả test

| Bước | Trạng thái / committed facts giả định | Kết quả theo candidate A |
| --- | --- | --- |
| 1 | Chưa từng join, group J=0 | Accept hợp lệ tạo member M/P1 ACTIVE, from=1; snapshot H=0 có zero eligible messages |
| 2 | Member mới join lúc committed J=41 | P1 from=42; snapshot H=41 rỗng. Không cho sequence41 chỉ vì nó còn là lastMessage |
| 3 | Sau đó seq42 commit, H=42 | Có thể xem42 nếu current ACL vẫn hợp lệ; message tồn tại không tự chứng minh READ |
| 4 | Đổi MEMBER thành role được policy cho phép | Vẫn P1, revision quyền thay đổi; tokens cũ revalidate; không reset receipt bằng new period |
| 5 | Authorized remove tại J=48 | P1 đóng tại48 phục vụ audit, trạng thái REMOVED; current history/receipt/media/translation đều deny |
| 6 | Lời mời mới hợp lệ, rejoin tại J=53 | Cùng M, P2 UUID mới, from=54; không kế thừa cursor/P1 history; current source hiện trả409 thay vì làm việc này |
| 7 | Callback P1 delivered/read=47 đến sau P2 | Không merge vào P2 dù M/user giống nhau; stale P1 không thành evidence P2 |

Wire empty choices: giữ `[42,open]` với H=41 và empty eligible set giúp giữ floor; hoặc response `visibleIntervals=[]` có server attestation và period/floor/snapshot metadata đã được TV2 quy định. Nếu chỉ gửi `[]` và làm mất future floor thì không đủ mô tả quyền. Zero receipt sentinel không phải sequence floor; `[]` không bao giờ nghĩa “toàn bộ lịch sử”. Không phát minh field/API đã được approve.

Legacy: current schema không có Message allocator để khôi phục historical J. Alternative L1 phục hồi từ ledger đầy đủ và chứng minh; L2 owner-approved conservative cutover, không cấp pre-cutover history không chứng minh được; nếu cả hai chưa được duyệt thì giữ dependent history activation bị chặn. Không tạo period lịch sử/floor từ timestamp hay tự ép mọi member đi rejoin để lách migration policy.

### Race schedules cần TV2/TV1 xác nhận

| Race | Trật tự1 | Trật tự2 | Điều phải chứng minh |
| --- | --- | --- | --- |
| Send / join, ban đầu J=41 | Send42 commit trước, join thấy J42→floor43 | Join trước→floor42, send42 commit sau có thể eligible | Cùng serialization boundary, không chỉ hai timestamp |
| Rollback allocation / join | Reserved42 rollback; join không được coi reservation là committed message | Với transactional counter rollback về41 thì floor42; nếu allocator có gaps cần quy tắc finalized watermark rõ | Không nhầm nextval/high reservation với J; không wrap overflow |
| Remove / sender send | Remove commit trước admission→deny, zero new message | Send commit trước→message giữ identity; remove sau chặn new access | Queued ACK/fetch vẫn qua final disclosure; denial không chứng minh send rollback |
| Remove / history page | Remove thắng final gate→deny | Bytes đã được admission trước remove có residual đã định nghĩa | DB check đầu query không đủ bảo đảm socket final handoff |
| Rejoin / P1 retry | P2 không có P1 visibility→hidden committed/absent cùng404 | Exception chỉ được tiết lộ nếu explicit ACL đã được phê duyệt | Key/origin bất biến, unknown commit không thành resend P2 |

DIRECT: Message Request accept có thể cấp chat consent và create/get canonical DIRECT; friendship độc lập theo reported alignment. TV1 phải chọn block/unfriend/unblock/reopen có giữ grant nào, ai xem historical Message/media/receipts nào; TV2 quyết stable participation hay consent-period wire. Không áp GROUP floor, không cấp quyền chỉ vì canonical conversation đã tồn tại, không tự khôi phục consent khi unblock.

## 5. Gói D10 — Receipt privacy

### Worksheet D10

1. **Existing behavior:** Chưa có server receipt store/ACL/aggregate; UUID lastReadMessageId không chứng minh sequence receipt model. Không có production privacy acceptance.
2. **Proposed behavior:** Period/history-scoped durable delivery và genuine viewport READ, READ<=DELIVERED. Viewer permission, subject period và aggregate denominator được chọn độc lập. Không chọn A/B trong tài liệu này.
3. **Alternative A — Privacy-first:** Current ACTIVE viewer có owning-message entitlement mới xét disclosure; own receipt và author-to-recipient visibility còn cần chọn rõ, không mặc định mọi peer được xem. Former subject không nằm trong current roster; historical evidence riêng chỉ được dùng khi có quyền riêng. Aggregate unavailable tới khi audience/denominator được chốt.
4. **Alternative B — Authorized historical:** Viewer được explicit historical ACL có thể xem receipt của former subject gắn member+period; không biến named historical subject thành current roster, không cấp cho current member mới nếu không entitled. Cần lưu eligible audience/evidence và disclosure projection.
5. **Technical consequences:** Per-message projection hoặc range projection chứng minh không lộ hidden later activity; revision-aware snapshot/reducer và deterministic device merge. Audience-at-send cần lưu immutable recipient-period eligibility; current audience cần version và quy tắc label thay đổi theo roster.
6. **Security/privacy consequences:** Receipt cho thấy hoạt động/quan hệ/history; full high cursor có thể lộ hidden messages. WITHHELD marker có thể tự lộ subject nếu subject vốn không được biết. Former-member audit retention không phải UI permission. Account switch phải clear view và reject stale apply.
7. **Compatibility impact:** Cần reviewed DTO/UNKNOWN/NONE/WITHHELD representation và cursor mapping; không tự đổi FIX3 wire/enum. Tạm unavailable aggregate không được coi P0 aggregate đã pass hoặc giảm scope.
8. **Affected owners:** Phúc receipt policy; TV1 privacy/security; TV2 persistence/wire/reducer/device evidence và query cost.
9. **Required evidence:** Signed viewer/subject/action matrix; selected audience/any-all/author/empty policy; fixture coverage hidden/tombstone/former/P1-P2/account switch; durable device + viewport proof; range privacy/cost và deterministic merging. Chưa có các runtime kết quả này.
10. **Exact decision requiring Phúc:** Điền A/B, ai được thấy named receipt, former evidence có/không và ACL nào, audience-at-send/current, any/all/none, author exclusion, empty/unknown/withheld/tombstone behavior. **PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION.** Không có default selection nếu bỏ trống.

### Bảng lựa chọn tách biệt để tránh “approve receipts” quá rộng

| Trục | Lựa chọn / ý nghĩa | Rủi ro / câu cần chốt |
| --- | --- | --- |
| Named viewer | Own-only; author xem eligible recipients; other members dưới explicit ACL | Current ACTIVE và visible message là điều kiện cần, chưa tự cấp peer roster |
| Former subject | A bỏ khỏi current named view; B chỉ historical ACL | User mới/rejoined không tự biết tên/hoạt động period trước |
| Audience-at-send | Tập recipient-period eligible lúc message commit, nếu được chọn | Stable denominator nhưng cần storage/projection; member gửi sau này không được thêm hồi tố |
| Current audience | Tập ACTIVE eligible ở snapshot hiện tại | Remove người chưa đọc có thể biến label thành all-read; cần explicit semantics, không silent switch |
| Any | Ít nhất một eligible recipient có evidence hợp lệ | Không được render như “mọi người”; UNKNOWN có thể ngăn kết luận negative |
| All | Tất cả eligible recipients có evidence hợp lệ | Không bỏ unknown/withheld/removed để tạo kết luận đúng giả; denominator phải xác định |
| Author | Loại author khỏi recipients hoặc tính theo policy rõ | Sending không chứng minh receipt của người khác; nếu tính author cần genuine receipt evidence |
| Empty audience | Unavailable/no-recipient label được chọn riêng | Không vacuous READ; không tự đặt delivered/read chỉ vì tập rỗng |
| Per-message / range | Exact evidence hoặc proven safe range | High cursor/clamp không được bắc cầu hidden gaps hoặc lộ later activity |

NONE là proven absence trong scope được phép, không phải “user chắc chắn chưa nhìn”; UNKNOWN là chưa đủ thông tin; WITHHELD là bị giới hạn disclosure, không phải negative evidence. DELIVERED cần durable eligible device cache; READ cần genuine viewport và eligible prefix. Socket flush, server deliveryComplete hoặc accept invitation không tạo receipt.

Ví dụ giả định: recipient U/P1, history revision r7, device1 có D12/R10, device2 D11/R11. Chỉ khi các prefix cùng scope được chứng minh hợp lệ thì merge ra D12/R11. Device2 P2 hoặc r8 không có reviewed rebase thì không max vào P1/r7. Nếu hidden gap làm prefix không được chứng minh, high number không đủ evidence để merge/hiển thị. Sender A, U đã đọc, V UNKNOWN: any-read có thể đúng nếu viewer được phép biết U; all-read chưa chứng minh. Remove V không được âm thầm đổi denominator thành một người rồi khẳng định cả nhóm đã đọc theo audience-at-send. Phúc chưa chọn any/all trong ví dụ này.

## 6. Gói D25 — GroupInvitation và accept-to-period

### Worksheet D25

1. **Existing behavior:** Separate PENDING invitation; accept đúng invitee kiểm eligibility/current inviter ACTIVE, insert MEMBER ACTIVE và cập nhật ACCEPTED trong một transaction. Processed response409; bất kỳ existing member row409; reject không tạo member. Invite management OWNER-only; accept hiện không recheck inviter OWNER. CANCELLED enum tồn tại nhưng không có cancel endpoint/expiry field/rejoin route; ADMIN enum không cấp management power.
2. **Proposed behavior:** Giữ invitation độc lập; pending không cấp GROUP access. Future approved accept/rejoin mới tạo fresh period/floor/revisions/event facts atomically; explicit current inviter/group/target/revoke checks. Không đổi policy đang chạy trong nhiệm vụ này.
3. **Alternative A:** Giữ scope/hành vi hiện tại: không auto-expiry mới, không cancel action mới, no-rejoin/current existing-member409; processed409. Đây là compatibility baseline, không phải xác nhận indefinitely pending là policy tốt cuối cùng.
4. **Alternative B:** Lifecycle mở rộng được owner duyệt: expiry hoặc no-expiry có policy rõ; cancel actor cụ thể; fresh invitation sau eligible terminal state; inactive member rejoin với new period. TTL/terminal representation/actor/ADMIN rights phải chọn, không được suy ra từ enum.
5. **Technical consequences:** B cần stable member/current period pointer, invite version/revocation generation, effective pending uniqueness, ordered transaction và durable facts. Expiry phải check authoritative server time khi transition dưới lock; thời gian expiry không thay committed sequence cho history floor.
6. **Security/privacy consequences:** Stale invitation không undo removal; pending invitee chỉ xem/respond lời mời riêng được phép. Inviter bị demote/remove, invitee bị block/account revoked phải xét current policy. Không tự chấp nhận client-supplied OWNER/ADMIN.
7. **Compatibility impact:** Giữ INVITATION_PROCESSED409 và MEMBERSHIP_EXISTS409 trừ khi approved contract change. Idempotent200 là alternative cần current disclosure và review riêng. Reinvite sau reject có thể qua existing checks khi chưa có member/pending; không coi đó là một policy rejoin/cancel/expiry hoàn chỉnh đã được duyệt.
8. **Affected owners:** Phúc invitation/role/rejoin; TV1 eligibility/block/account; TV2 Conversation/period/allocator/events/wire; joint schema review.
9. **Required evidence:** Signed lifecycle/role/409 matrix; production directory/revoke contract; accept/remove/cancel/role/reinvite/account races; DB rollback sau mỗi mutation và event publication; one stable member/one current period; preserve V1 + separately approved migration plan. Existing accept/accept và insert-failure tests chỉ là historical initial-accept evidence.
10. **Exact decision requiring Phúc:** Giữ A hay mở phần nào của B; expiry/no-expiry và TTL nếu chọn; original inviter/current OWNER/ADMIN ai cancel; reissue sau rejection/cancel/expiry/remove; LEFT có self-reentry không; current inviter cần OWNER hay ACTIVE tại accept; fresh-period rejoin và processed409 policy. **PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION.**

### Atomic accept-to-period và race outcomes đề nghị kiểm chứng

Authenticated identity/syntax → đọc invite locator chỉ để tìm complete lock set → acquire authority/account/privacy roots rồi group/member/invite theo rank chung → reread locator/invitee/status/version, effective expiry nếu đã duyệt → current inviter authority/eligibility/group/target/revocation generation → preserved ACTIVE409 hoặc approved inactive-rejoin gate → capture committed J → consume invite + member/current period/floor/revisions + durable conversation/account/outbox facts → commit → current-authorized publication/final response. Bất kỳ failure trước commit rollback cả cụm; không publish trước commit. Đây là sequence đề xuất, không phải current invitation-first implementation.

| Cặp race | Kết quả cần chọn/prove dưới shared serialization |
| --- | --- |
| Accept / accept | Một success, retry thấy processed409; không period/member thứ hai |
| Accept / decline | Một terminal winner; loser thấy processed, reject không tạo membership |
| Accept / cancel | Nếu cancel action được duyệt: cancel trước thì accept không cấp quyền; accept trước thì cancel không tự biến thành remove |
| Accept / remove | Remove trước phải invalidate stale invite/generation; accept trước rồi remove phải đóng period mới và deny current access |
| Accept / reinvite | Không hai effective pending; không recycle invitation ID để bypass removal fence |
| Role change / accept | Current role được policy chọn quyết định, không dùng snapshot role lúc gửi invite |
| Account revoke / accept | Cùng TV1 authority/final admission protocol; decoded JWT hợp lệ một lần không đủ |

Với target chưa có membership, “remove trước” phải có persistent authority/tombstone/generation để invalidate stale invite, hoặc policy terminalize invitation rõ. Lock một member row chưa tồn tại không đủ. Nếu chưa định nghĩa cách biểu diễn này, accept/remove invariant chưa có physical proof và B chưa ready.

## 7. Gói TX — Chọn physical transaction khi nào?

### Worksheet TX

1. **Existing behavior:** Local Groups transaction invitation-first + initial member insert; chưa có shared Message allocator/period/outbox/global rank/admission protocol. Android logical invalidation/cleanup không chứng minh server cross-instance revocation.
2. **Proposed behavior:** Một authoritative protocol bao trùm current account/privacy/member authorization, domain write, idempotency, sequence, durable event facts và final disclosure. Chỉ chọn candidate khi assumptions và required evidence được thỏa; hiện **không candidate nào đạt**. Không nâng “A để đánh giá” trong ADR thành “A đã được khuyến nghị để triển khai”.
3. **Alternative A:** Ordered locks + READ COMMITTED; persistent serialization roots, complete sorted affected authority set, group/counter serialization, constraints và bounded whole-command retries; final-response/publication barrier phải định nghĩa riêng.
4. **Alternative B:** Persisted generation fencing + strong completion barrier; mọi instance/worker/queue/response admission tham gia, stale work bị chặn và crashed participant có recovery/containment. **Alternative C:** SERIALIZABLE + correct retries, vẫn phải có constraints, current authority và final disclosure/outbox protocol.
5. **Technical consequences:** A dễ giải thích nhưng lock set lớn/contention; B phức tạp về distributed fencing/liveness; C giảm một số DB anomaly qua abort nhưng tăng retry và không serialize external I/O. Không ghép eventual fanout vào strong-revoke claim mà không proof.
6. **Security/privacy consequences:** DB commit không tự bảo đảm callback/socket không gửi dữ liệu sau revoke. Phải định nghĩa logical denial, last admitted response và thời điểm revoke complete; bytes đã gửi không thể thu hồi. Hidden P1 key, blocked pair và recipient projection không được lộ qua retry/recovery.
7. **Compatibility impact:** Cần all-module contract/schema/transaction participation, propagation và rank agreement; current invitation-first không tự tương thích. Không sửa interface/migration hoặc chọn timeout/retry values tại đây.
8. **Affected owners:** TV1 account/social/sid/privacy; TV2 sequence/message/replay/WSS/outbox; TV3 membership/invitation/foundation; custodian rollout sau này.
9. **Required evidence:** Bảng assumptions/evidence bên dưới; complete affected writer/reader inventory; deterministic cross-instance races, crash/deadlock/timeout/unknown commit, bounded capacity và physical completion. Chưa có integrated results cho bất kỳ candidate.
10. **Exact decision requiring Phúc:** Ghi “defer physical selection pending evidence” hoặc chọn candidate chỉ sau khi TV1/TV2 cung cấp và cùng duyệt proof. Chốt phạm vi linearization/revoke completion, roots/ranks, capacity, recovery và owner chịu từng condition. **PROPOSED — AWAITING HOÀNG PHÚC CONFIRMATION.**

### So sánh kỹ thuật và điều kiện chọn

| Chủ đề | A: ordered locks / READ COMMITTED | B: generations / completion barrier | C: SERIALIZABLE | Bằng chứng thiếu / điều phải chốt |
| --- | --- | --- | --- | --- |
| Lock order | Account roots UUID → social/consent pair → key/pair root → conversation → member/invite → counter/message → receipt/attachment → log/outbox; exact ranks cần review | Vẫn cần order/atomic CAS cho persisted generation và command facts | DB có thể abort dependency cycles; explicit locking vẫn có thể deadlock | Canonical comparator, complete rank inventory, writer compliance |
| Missing rows | Persistent authority/pair roots hoặc reviewed unique/upsert strategy; không lock nonexistent row bằng wishful check | Atomic create generation root/uniqueness, fencing token cấp bền vững | Predicate conflicts không thay unique constraint và valid retry logic | Concurrent first create/revoke/invite, cross-instance canonical pair proof |
| Deadlocks / retry | Sorted complete set, rollback khi phát hiện cần lower rank; bounded waits/retries | CAS contention/barrier stalls cần bound, có thể vẫn deadlock ở underlying DB | Whole transaction retry sau serialization/deadlock; external effects chưa được phát | Opposing schedules, aborted-tx reread đúng, attempts/deadlines/charges |
| Cross-instance | Mọi instance cùng DB locks và agreed publication gate, không process mutex | Persisted generations, mọi instance tham gia barrier, crashed lease recovery phải safe | Isolation chỉ trong DB transactions; worker/socket là ngoài DB | Instance crash/restart/network partition và stale worker proof |
| Idempotency | Unique command identity, frozen key/payload/origin, current disclosure khi reread | Token/generation không thay permanent command identity | Retry giữ cùng original identity, không external duplicate | Changed payload conflict, P1 concealment, no duplicate committed Message |
| Durable outbox | Facts+outbox cùng commit, publish sau commit, recipient ACL projection/dedup | Outbox generation-bound, barrier kiểm trước final admission | Serializable command vẫn cần transactional facts/outbox | Crash before/after commit/publish/ack; dedup/order/replay coverage |
| Unknown commit | Giữ key/origin/uncertainty; không auto-rekey hoặc coi cancel là rollback | Fence đổi không chứng minh command cũ không commit | Serialization failure được xác nhận khác lost commit response | Lost response recovery được phép dưới current ACL, zero hidden ACK oracle |
| Revocation / final disclosure | Lock trong write command chưa đủ; final nonblocking handoff và revoke completion cần protocol | Barrier phải chặn stale admitted work tới completion point được định nghĩa | Successful serializable read có thể kết thúc trước socket write/revoke | Last admitted work/residual, deny-after-completion proof, no DB lock held across socket I/O |
| Bounded capacity | Complete affected-user set có thể lớn; invite batch100 không phải group cap | Fanout acknowledgements/retiring workers cần bound, timeout không được giả complete | Hot group có thể retry storm, cần backpressure | Roster/discovery retry bound, queue/lock/admission budgets và load evidence |
| Rollback / recovery | Invite/member/period/counter/key result/facts cùng rollback; no early event | Persisted token+fact atomicity; orphan intent/barrier recovery | Rollback DB không undo provider/network effects | Fault sau từng write, restore/restart/replay và containment evidence |

Candidate A chỉ đủ điều kiện được khuyến nghị khi complete authority roots/lock set khả thi và bounded, all modules tuân rank, J commit-safe, final gate/revoke semantics được định nghĩa và cross-instance proof đạt. Candidate B chỉ đủ khi persisted generation/atomic writes, completion barrier và crash/recovery/liveness/capacity được chứng minh. C chỉ đủ khi đúng transactional scope/predicate/constraint/retry và cùng final gate proof. Hiện các điều này chưa đủ; **khuyến nghị duy nhất ở bước này là giữ selection pending**, không thông qua một candidate vì “đơn giản hơn”.

Ví dụ unknown commit: accept transaction có thể đã commit nhưng HTTP response mất. Gửi lại cùng invite hiện hành có thể nhận processed409; không suy ra rollback hoặc tự tạo lời mời/member thứ hai. Message send giữ clientMessageId/frozen origin; recovery chỉ tiết lộ khi current ACL cho phép. Không đổi409 sang200 chỉ để che sự không chắc chắn.

## 8. Danh sách điều kiện chưa giải quyết và người quyết định

| Condition | Ai quyết/cung cấp | Gate bị chặn |
| --- | --- | --- |
| Phúc chưa ký exact D08/D09/D10/D25/TX selections | Hoàng Phúc | TV3 detailed adoption, không đồng nghĩa historical direction bị hủy |
| Registry concurrence/full ADR/scope và allocation protocol | TV1/TV2/TV3 | Shared governance/ADR adoption; không được reserve số lúc này |
| GROUP floor/empty/legacy/P1 exceptions, LEFT/REMOVED lifecycle | Phúc + TV2 mapping + TV1 privacy | Period/history shared design và dependent implementation |
| DIRECT consent/block/unfriend/reopen/history | TV1 policy, TV2 model/wire | Không áp GROUP policy thay DIRECT; request integration |
| Receipt named/former/audience/aggregate/author/unknown matrix | Phúc + TV1 privacy + TV2 representability | D10 adoption và receipt activation |
| Expiry/cancel/reinvite/role/409/current inviter và missing-row revoke | Phúc + TV1 current eligibility + TV2 transaction | D25 lifecycle design; không lấy current enums làm quyền |
| Complete authority/final gate/revoke completion/capacity | TV1/TV2/TV3 | Physical candidate selection; later implementation/activation proof |
| 01E.2C exact artifact và scoped TV2 adoption | TV2 | Owner reconciliation; scoped searches không phải evidence nội dung file |
| Original01A/prior draft/preliminary fallback/historical signatures | Artifact custodian + cả ba owner chấp nhận bounded limitation nếu phù hợp | Complete provenance/freeze claims; không cản viết review này |
| Custodian/target history/checksum/drift/restore | Authorized environment custodian, affected owners | Database readiness và Day 1 lineage gate theo handoff; no connection here |
| D22/D23/D24 còn conditions và shared30 chưa ký | Owners tương ứng | Full checkpoint/Day 1 Exit, không được thu hẹp thành chỉ năm gói này |

## 9. Phiếu owner sign-off — để trống tới khi người thật xác nhận

Phúc có thể trả lời bằng các lựa chọn dưới đây và sửa bất kỳ mục nào; không tick mặc định. Partial policy confirmation phải ghi exclusions và affected-owner review còn pending.

```text
Owner: Hoàng Phúc (người xác nhận thực tế): __________
Ngày/giờ/timezone và attributable source: __________
Review document path + exact revision hoặc raw SHA256: __________

D08: chọn A / B / sửa / defer: __________
Coordinator scope; domain approvals; custodian boundary: __________
Full ADR adopted? Nếu chưa, conditions còn mở: __________

D09: stable member + fresh period: __________
GROUP exact lower/upper boundary, J0/overflow: __________
Empty interval + snapshot representation (TV2 concurrence): __________
Legacy mapping / P1 exception / LEFT-REMOVED rejoin: __________
DIRECT excluded pending TV1/TV2 policy: __________

D10: chọn privacy A / historical B / sửa / defer: __________
Named viewer và former subject ACL: __________
Audience-at-send / current; any / all / aggregate unavailable: __________
Author exclusion; empty/UNKNOWN/NONE/WITHHELD/tombstone: __________
Per-message/range; device-period-history merge conditions: __________

D25: expiry/no-expiry/TTL nếu chọn: __________
Cancel actor; reinvite; OWNER/ADMIN/current inviter: __________
Processed409/existing member; fresh-period rejoin và revoke fence: __________
Atomic accept-to-period conditions: __________

TX: defer / A / B / C, chỉ sau assumptions/evidence: __________
Evidence refs và final disclosure/revoke completion/capacity: __________
TV1/TV2 affected-owner signatures và exact scope: __________

Accepted conditions, condition owner, closure criterion: __________
Missing evidence/explicit exclusions: __________
Đây là policy adoption / design adoption / authorization nào: __________
Permitted next action (không mặc định implementation): __________
```

Chưa điền/không có câu xác nhận là **pending**, không phải chấp nhận theo khuyến nghị. TV1/TV2 phải tự ký phần của họ; Phúc không ký thay. Commit tác giả Codex hay owner account không phải bằng chứng human adoption.

## 10. Day 1 Exit và hành động bị cấm

**Day 1 Exit chưa đạt và chưa thể tuyên bố đạt chỉ nhờ review này hoặc một câu đồng ý của Phúc.** Năm gói đã đủ để thảo luận và ghi nhận lựa chọn có phạm vi; shared conditions vẫn phải đóng hoặc có accepted conditions ở đúng gate theo checkpoint record.

- Có scoped responses/adoption từ TV1, TV2, TV3 đúng revision và formal checkpoint record.
- D08–D10 và D22–D25 được giải quyết tới mức bắt buộc; Auth/security, Messaging, membership/history/receipt boundaries có owner rõ.
- Full ADR D08 được adoption; physical strategy có assumptions/evidence đủ để chọn, không đánh tráo later runtime activation với design discussion.
- Actual DB lineage/custodian evidence theo handoff, original register provenance/accepted limitations, conditions/closure owners và shared sign-offs được ghi rõ.
- Implementation/activation/deploy evidence sau đó vẫn độc lập; test dự kiến không được ghi PASS và TEXT-only demo không thay full P0.

Các hành động bị cấm sau review này: sửa Android/backend/Auth/Messaging production source; schema/shared interfaces; edit/rename/split V1; tạo migration/reserve version; connect/migrate/repair/reset shared DB; sửa original register statuses; publish approval/decision thay TV1/TV2/Phúc; thay status/reviewers hoặc approve/merge PR17; official messages thay owner; tự sửa shared contract, tạo CONTRACT-01F hoặc bắt đầu Day2.

## 11. Verification record và điểm dừng

Chỉ tạo `docs/architecture/tv3/TV3_OWNER_DECISION_REVIEW_01E3.md`. Kiểm tra nội dung đủ năm worksheet, mỗi worksheet đủ mười trường; bảng/link nội bộ/UTF-8/whitespace và preserved baseline được thực hiện sau khi ghi. Không chạy build/runtime tests, không dùng design race examples làm test results. Sáu source documents, register, source/migration và PR state không thay đổi.

**STOP sau khi tạo và kiểm tra review document. Chờ Hoàng Phúc xác nhận rõ trước mọi thay đổi shared architecture contract hoặc implementation.**
