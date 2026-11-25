# NEXA CHAT — 14-Day Project Checklist & Audit

Ngày audit: **25/11/2026 (Asia/Saigon)**. Vai trò: Hoàng Phúc — TV3. Đây là snapshot audit, không phải quyết định merge hoặc xác nhận dự án hoàn tất.

## 1. Executive Summary

### Phạm vi và kết luận

- Main đã đối chiếu: **`8b730dccc171c6d55fd8ae127448ed2b6c5f23a4`** ([source cố định](https://github.com/datcaq/NEXA-Chat/tree/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4)).
- Checkout được giữ nguyên: `feature/group-01-create-group`, HEAD `d2141e2560d8d6ba198942c944a69a963c133923`. Không rebase, merge, commit, push hay sửa source/migration/contract/Word/Google Docs.
- Đọc trực tiếp [kế hoạch Google Docs](https://docs.google.com/document/d/1w-BIsQo67tqTtsV2gCMIifR2-u8x2k_3GijvW3s7FEA/edit), tab `t.k8l01lk27wc7`; đọc GitHub REST toàn bộ PR, review, source/history và test reports. Không dùng PR body làm bằng chứng duy nhất.
- Không tìm thấy ngày lịch chính thức cho Day 1; **không suy diễn ngày bắt đầu hoặc ngày hiện tại trong kế hoạch**.
- Main có Android skeleton, Auth preview/challenge contracts, Session V2 contracts và Home placeholder. Chưa có Auth thật → authenticated Home → Chat/Groups tích hợp. Backend/Groups và Messaging nằm trên Draft branches.
- **Chưa có E2E Verified** cho hai account thật, persisted chat/group/history/media hoặc logout/account switch trên app đã tích hợp.
- 42 item là 14 ngày × 3 owner, mỗi item giữ nguyên toàn bộ deliverable gốc. Không tính subtask foundation như hoàn thành toàn bộ ngày.

| Status | Số item trong 42 |
|---|---:|
| [x] DONE | 0 |
| [~] PARTIAL | 20 |
| [!] BLOCKED | 13 |
| [ ] TODO | 9 |
| [N/A] OUT OF SCOPE | 0 |

0 DONE ở cấp deliverable nguyên ngày không phủ nhận code foundation đã hoàn thành. Translation/Dual Mode còn là P1 có điều kiện, không bị loại khỏi kế hoạch để tăng tỷ lệ.

### PR inventory — HEAD/base và official review

Có **12 PR (#4–#15): 7 merged, 5 open Draft**. 9 PR có APPROVED đúng HEAD; #13 có CHANGES_REQUESTED; #6/#9 không có official review. Base là SHA do PR API trả tại snapshot, không mặc định bằng main hiện tại. Các open PR được API báo mergeable tại lúc kiểm tra; điều này không thay approval hoặc acceptance. Reviewer có approval là Tender0603; self-audit TV3 không thay independent review cho PR6.

| PR | Branch | HEAD | Base | Trạng thái | Official review |
|---|---|---|---|---|---|
| [#4](https://github.com/datcaq/NEXA-Chat/pull/4) | `chore/android-project-setup` | `863d6b8d4d9840b0d3b7b65fc0afece9e59cbf1a` | `493f77329c57d81e0f29f3ea086f8f547d960c01` | MERGED 2026-11-17T08:00:26Z | APPROVED @ 863d6b8 |
| [#5](https://github.com/datcaq/NEXA-Chat/pull/5) | `chore/ignore-technical-doc` | `d1dfa6256fffecd039ee7064cbd168eac7d9e208` | `2b8ac61a129892cf25df215b3d98620e0a03d7fd` | MERGED 2026-11-24T08:45:13Z | APPROVED @ d1dfa62 |
| [#6](https://github.com/datcaq/NEXA-Chat/pull/6) | `feature/group-01-create-group` | `d2141e2560d8d6ba198942c944a69a963c133923` | `1945282e93a7e8b72e0494af5213ec4f70b3e398` | OPEN / DRAFT | Không có official review |
| [#7](https://github.com/datcaq/NEXA-Chat/pull/7) | `feature/auth-ui` | `28700c80a15ccb1ee2a62ce96bad6f147b8f10a1` | `2b8ac61a129892cf25df215b3d98620e0a03d7fd` | MERGED 2026-11-24T09:10:38Z | APPROVED @ 28700c8 |
| [#8](https://github.com/datcaq/NEXA-Chat/pull/8) | `feature/core-01-session-contracts` | `a5ede4ac151f65648f1c7be7a031397ccbee6e5a` | `2b8ac61a129892cf25df215b3d98620e0a03d7fd` | MERGED 2026-11-24T11:25:05Z | APPROVED @ a5ede4a |
| [#9](https://github.com/datcaq/NEXA-Chat/pull/9) | `feature/messaging` | `ea239b9f66a153ac9f848fd089e5d4dd8b8874e4` | `a89d1ad8cb091cab9699d0ad7dc212c80c322656` | OPEN / DRAFT | Không có official review |
| [#10](https://github.com/datcaq/NEXA-Chat/pull/10) | `feature/core-02-home-ui` | `c5591323b65b97c3541a54d6b54124406942f303` | `2b8ac61a129892cf25df215b3d98620e0a03d7fd` | MERGED 2026-11-24T09:12:41Z | APPROVED @ c559132 |
| [#11](https://github.com/datcaq/NEXA-Chat/pull/11) | `feature/core-03-auth-repository` | `8bbb17b7b97b965f938b0d8b2231dab7742bdddb` | `a89d1ad8cb091cab9699d0ad7dc212c80c322656` | MERGED 2026-11-24T16:51:59Z | APPROVED @ 8bbb17b |
| [#12](https://github.com/datcaq/NEXA-Chat/pull/12) | `feature/core-03b-auth-viewmodel` | `0225d6b5e47479a5103ec0616e8c92aaee323415` | `62b49198943351f2e086514700631dbcdc3a7be3` | MERGED 2026-11-24T16:55:36Z | APPROVED @ 0225d6b |
| [#13](https://github.com/datcaq/NEXA-Chat/pull/13) | `feature/core-04a-session-coordinator` | `1c459c517cbfea06d1c071554a05c894157e157e` | `a89d1ad8cb091cab9699d0ad7dc212c80c322656` | OPEN / DRAFT | CHANGES_REQUESTED @ 1c459c5 |
| [#14](https://github.com/datcaq/NEXA-Chat/pull/14) | `feature/contact-01a-contacts-ui` | `b448991c3176618cdb8eeed46ebacb38693d7dc8` | `a89d1ad8cb091cab9699d0ad7dc212c80c322656` | OPEN / DRAFT | APPROVED @ b448991 |
| [#15](https://github.com/datcaq/NEXA-Chat/pull/15) | `fix/home-tab-regression` | `f3456179e01ec4d49c5088da07fd12f061a25910` | `8b730dccc171c6d55fd8ae127448ed2b6c5f23a4` | OPEN / DRAFT | APPROVED @ f345617 |

PR #9 body có ghi independent review/DESIGN3 nhưng review API rỗng: không tính là official APPROVED. #14/#15 được approve vẫn chưa merged, chưa đóng deliverable tích hợp. #13 [review TV2-R13-01](https://github.com/datcaq/NEXA-Chat/pull/13#pullrequestreview-6050049996) còn yêu cầu sửa.

### Evidence registry và giới hạn kiểm chứng

Các link source dưới đây cố định theo SHA. Log/snapshot/probe mới nằm ngoài repository tại `D:/Nexa-Chat/test-artifacts/audit-2026-11-25/`; chúng là bằng chứng local, chưa phải artifact đã publish trên GitHub. Các số test bên dưới **không cộng thành số case độc lập của dự án** vì nhiều baseline trùng nhau.

| ID | Source được đọc | Bằng chứng thực tế / giới hạn |
|---|---|---|
| E-plan | Google Docs ở trên, đầy đủ bảng Day1–14 | Nội dung đọc trực tiếp; không sửa tài liệu. Day9 bản gốc có media/bảo mật; Day10 có đa ngôn ngữ và ổn định media. Checklist giữ cả hai, không bỏ media Day9. |
| E-main | [main app](https://github.com/datcaq/NEXA-Chat/tree/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app) | Snapshot main: `assembleDebug` và `testDebugUnitTest --rerun-tasks --no-build-cache` PASS, **44 JVM / 7 XML**, failures/errors/skips = 0. Log `main-build.log`; XML `main/app/build/test-results/testDebugUnitTest/`. |
| E-msg | [PR9 source](https://github.com/datcaq/NEXA-Chat/tree/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src) và docs của branch | Cùng lệnh build/unit: **183 JVM / 17 XML**, 0 failures/errors/skips; trong đó 165 Messaging +18 baseline. `pr9-build.log`, `pr9/app/build/test-results/testDebugUnitTest/`. Không có WS/HTTP/persisted E2E. |
| E-coord | [PR13 source](https://github.com/datcaq/NEXA-Chat/tree/1c459c517cbfea06d1c071554a05c894157e157e/app/src) | Build/unit PASS **35 JVM / 6 XML**, 0 failures/errors/skips; `pr13-build.log` và snapshot XML. Probe bổ sung tái hiện hook exception, không sửa TV1 source. |
| E-contacts | [PR14 source](https://github.com/datcaq/NEXA-Chat/tree/b448991c3176618cdb8eeed46ebacb38693d7dc8/app/src) | Build/unit PASS **26 JVM / 6 XML**, 0 failures/errors/skips, gồm 8 Contacts input +18 baseline. `pr14-build.log` và snapshot XML. PR báo 7 Contacts +3 baseline UI; không tự rerun được trong audit này. |
| E-group | [PR6 app](https://github.com/datcaq/NEXA-Chat/tree/d2141e2560d8d6ba198942c944a69a963c133923/app/src), [docs](https://github.com/datcaq/NEXA-Chat/tree/d2141e2560d8d6ba198942c944a69a963c133923/docs/group-01) | Build/unit rerun PASS **37 JVM / 7 XML**, 0 failures/errors/skips (30 Groups +7 baseline). `pr6-build.log`; XML thực tại `D:/Nexa-Chat/source/app/build/test-results/testDebugUnitTest/`. PR6 base cũ hơn main nên tổng không phải 44. |
| E-db | [PR6 backend](https://github.com/datcaq/NEXA-Chat/tree/d2141e2560d8d6ba198942c944a69a963c133923/backend) | **Maven verify PASS ngày 25/11/2026**, Java21, Spring Boot3.5.16, Docker29.4.3; **25 tests =12 GroupService +13 GroupPostgres**, failures/errors/skips=0. PostgreSQL Testcontainers thực, không skipped do thiếu Docker. Log `pr6-backend-verify.log`, XML `D:/Nexa-Chat/source/backend/target/surefire-reports/`; hoàn tất 08:58:40 +07. Tests dùng test-only identity/directory, không phải Auth thật. |
| E-ui15 | [PR15 single test diff](https://github.com/datcaq/NEXA-Chat/pull/15/files), [review](https://github.com/datcaq/NEXA-Chat/pull/15#pullrequestreview-6049897781) | Chỉ thay Home instrumentation test, không production fix. Reviewer báo 44 JVM và UI trên API37/gesture nav/1080×2400; 29 executions qua 11 runs **không phải 29 unique cases**. Full foundation suite có 6 case (3 Auth +2 Home +1 Example). Không xác nhận Messaging/Contacts/Groups E2E. Không tự reproduce baseline flaky trong audit. |
| E-probe | `ScopeAuditProbe.java`, `CoordinatorAuditProbe.java`, `scope-probes.log`, `coordinator-probes.log` tại audit artifact dir | Compile actual classes, probe riêng không cộng vào JUnit. External action giữ Groups lock làm close phải chờ; 1024 callbacks khi SUSPENDED được nhận; close clear queue nhưng cleanup RuntimeException thoát ra. PR13 factory/cleanup exception thoát caller; destination vẫn fail-closed CHECK_SESSION/AUTH trong probe. Không chứng minh crash app hay leak cross-account thực tế. |

Build snapshots được tạo bằng git archive vào thư mục artifact riêng; không checkout/rebase hoặc ghép các feature branches để giả lập tích hợp. `junit-summary.json` lưu chi tiết main/PR9/13/14. Gradle wrapper9.6.0, Android SDK37; launcher JDK21 và Gradle toolchain theo project. Backend dùng JDK21.

**Instrumentation audit này:** adb không có thiết bị kết nối; không chạy lại connectedAndroidTest. Test APK compilation/lifecycle test source không được tính PASS. Review PR12 báo 44 JVM +6 UI API37; đây là reviewer-reported, không phải XML UI do lượt audit này tự kiểm tra. PR13 chỉ có build/instrumentation claim chưa đủ actual UI XML. PR6 screenshots và verification cũ không xác nhận các thay đổi lifecycle mới. API26–36/rotation của app tích hợp chưa verified. GitHub check-runs main =0, Actions runs =0 tại snapshot; không có CI artifacts để bổ sung. Các report/body/review phải được đọc đúng phạm vi này.

### Bảy mức bằng chứng

| Phạm vi | Implemented | Tested | Reviewed bởi người khác | Official Approved | Merged | Integrated | E2E Verified |
|---|---|---|---|---|---|---|---|
| #7/#8/#10/#11/#12 foundation | Có, phạm vi preview/contracts/Home | JVM main; UI foundation reviewer-reported | Có | Có | Có | Auth challenge VM nội bộ; chưa authenticated Home | Chưa |
| #6 Groups/backend | Có GROUP-01 trên branch | JVM + PostgreSQL actual; UI giới hạn nêu trên | Chưa có bằng chứng independent review tại PR | Không | Không | Nội bộ module/test; adapters chung chưa có | Chưa |
| #9 Messaging | Có controller/view/fake/domain | 183 JVM actual; không real transport | Body nói đã review, chưa có review record xác nhận | Không | Không | Chưa Home/Session/server | Chưa |
| #13 Coordinator | Có foundation package-private | 35 JVM + probe exception | Có, changes requested | Không | Không | Chưa launcher wiring | Chưa |
| #14 Contacts | Có UI/input foundation | 26 JVM actual; UI claim tách riêng | Có | Có | Không | Chưa API/Home | Chưa |
| #15 UI regression test | Có test delta | UI API37 reviewer-reported | Có | Có | Không | Không production delta | Chưa |

**Các phần hẹp có thể đóng theo phạm vi PR:** skeleton #4; Auth preview #7; Session V2 contract #8; Home UI foundation #10; AuthRepository contract/fake #11; AuthViewModel challenge integration #12. Cả sáu có official approval/merged. Không dùng chúng để đóng toàn bộ Day2/Day3 hoặc Auth thật. #5 repo hygiene cũng merged nhưng không tính thêm một chức năng sản phẩm. #14/#15 chỉ chờ quy trình owner xử lý Draft/merge; không tự merge trong audit.

## 2. Fourteen-Day Progress Checklist

Ký hiệu: [x] DONE; [~] PARTIAL; [!] BLOCKED; [ ] TODO; [N/A] OUT OF SCOPE. Mỗi card là một cell gốc của Google Docs; tiêu chí đóng là toàn bộ deliverable của cell. Evidence IDs dẫn tới registry ở mục 1; thiếu evidence được ghi rõ, không suy ra PASS từ source.

### Day 1: Architecture and shared contracts

- [~] **D01-TV1 — PARTIAL**

  - **Ngày/milestone:** Day 1 — Architecture and shared contracts.
  - **Owner:** TV1.
  - **Deliverable:** Chốt Auth/App Shell, Splash→Auth→Home, E.164/OTP/JWT, User/Friend API.
  - **Source/PR:** E-main; PR #7/#8/#11/#12.
  - **Test evidence:** 44 JVM main; review approval đúng HEAD cho contracts/preview.
  - **Remaining work:** Auth thật, User/Friend API, sơ đồ gate runtime, chính sách OTP/JWT.
  - **Dependency/blocker:** TV1 + backend baseline.
  - **Acceptance criteria:** Contract được 3 owner xác nhận; đường dẫn điều hướng có gate xác thực.
  - **Next action:** Hoàn tất ADR Auth/session và API User/Friend.

- [~] **D01-TV2 — PARTIAL**

  - **Ngày/milestone:** Day 1 — Architecture and shared contracts.
  - **Owner:** TV2.
  - **Deliverable:** Chốt Conversation/Message, clientMessageId, order/read cursor, WS/retry.
  - **Source/PR:** E-msg; PR #9 DAY1_DECISIONS.md.
  - **Test evidence:** 183 JVM PR9; kiểm chứng domain/fake, không phải wire transport.
  - **Remaining work:** JSON field names, REST cursor, WS envelope, receipts/history/block semantics.
  - **Dependency/blocker:** TV2 + TV1/TV3 xác nhận shared contracts.
  - **Acceptance criteria:** Đặc tả REST/WS versioned và shared schema có approval liên quan.
  - **Next action:** Gửi contract wire và bảng mapping DB để chốt.

- [~] **D01-TV3 — PARTIAL**

  - **Ngày/milestone:** Day 1 — Architecture and shared contracts.
  - **Owner:** TV3.
  - **Deliverable:** Rà Groups/backend, roles/invitations và Translation contract.
  - **Source/PR:** E-group; PR #6; backend-baseline-proposal.md.
  - **Test evidence:** 37 JVM Groups branch; backend E-db; không có official review PR6.
  - **Remaining work:** Backend baseline độc lập; quyền ADMIN, history/revocation; Translation spec.
  - **Dependency/blocker:** TV1/TV2 approval schema và privacy/identity.
  - **Acceptance criteria:** Role matrix và Groups/Translation boundary được duyệt.
  - **Next action:** Gửi đề xuất baseline và ma trận quyền để review.

### Day 2: Shared foundation — App Shell, Messaging, Backend

- [~] **D02-TV1 — PARTIAL**

  - **Ngày/milestone:** Day 2 — Shared foundation — App Shell, Messaging, Backend.
  - **Owner:** TV1.
  - **Deliverable:** App Shell/Home 4 tab, navigation, Session interface, AuthRepository/Fake.
  - **Source/PR:** E-main; PR #8/#10/#11/#12; E-ui15.
  - **Test evidence:** 44 JVM; reviewer báo UI API37; không rerun UI audit.
  - **Remaining work:** Gắn Home sau Auth, provider/session thực, module host.
  - **Dependency/blocker:** TV1 runtime + TV2/TV3 adapters.
  - **Acceptance criteria:** 4 tab hoạt động với session thật và module contracts; logout khóa scope.
  - **Next action:** Làm gate và adapter sau khi PR13 hết changes requested.

- [~] **D02-TV2 — PARTIAL**

  - **Ngày/milestone:** Day 2 — Shared foundation — App Shell, Messaging, Backend.
  - **Owner:** TV2.
  - **Deliverable:** ChatRepository/Fake, DTO, khung conversation list/chat.
  - **Source/PR:** E-msg; PR #9.
  - **Test evidence:** 183 JVM, assembleDebug PASS.
  - **Remaining work:** Official review, merge theo owner, Home mount và wire DTO mapping.
  - **Dependency/blocker:** TV2 review; TV1 Host/Session contract.
  - **Acceptance criteria:** Foundation approved và list/chat dùng được qua host lifecycle.
  - **Next action:** Xin official review đúng HEAD và chốt host boundary.

- [!] **D02-TV3 — BLOCKED**

  - **Ngày/milestone:** Day 2 — Shared foundation — App Shell, Messaging, Backend.
  - **Owner:** TV3.
  - **Deliverable:** Backend Spring Boot/PostgreSQL/Flyway được duyệt; migration, seed 2 users/1 group, GroupRepository/Fake.
  - **Source/PR:** E-group; E-db; PR #6 proposal.
  - **Test evidence:** 37 JVM; Maven/PostgreSQL xem E-db.
  - **Remaining work:** Chưa có baseline PR riêng/approval; chưa có seed demo deliverable.
  - **Dependency/blocker:** TV1/TV2 schema/security approval.
  - **Acceptance criteria:** Một backend chung approved/merged, migrate sạch, seed demo có kiểm soát.
  - **Next action:** Tách phạm vi proposal; xác minh flyway_schema_history trước lập migration.

### Day 3: Auth, Chat và Groups UI

- [~] **D03-TV1 — PARTIAL**

  - **Ngày/milestone:** Day 3 — Auth, Chat và Groups UI.
  - **Owner:** TV1.
  - **Deliverable:** Registration/login với OTP đã duyệt, E.164, Auth UI, secure session.
  - **Source/PR:** E-main; PR #7/#11/#12.
  - **Test evidence:** 44 JVM gồm validator, repository contract và AuthViewModel.
  - **Remaining work:** Chỉ start challenge; chưa verify OTP/login/JWT/secure persistence.
  - **Dependency/blocker:** TV1 + Auth backend/OTP policy.
  - **Acceptance criteria:** A/B đăng ký hoặc đăng nhập thật; token bảo quản đúng, lỗi/rotation an toàn.
  - **Next action:** Triển khai real AuthRepository và credential storage.

- [~] **D03-TV2 — PARTIAL**

  - **Ngày/milestone:** Day 3 — Auth, Chat và Groups UI.
  - **Owner:** TV2.
  - **Deliverable:** Conversation list/direct chat và create-or-get DIRECT; có thể dùng fake khi chưa backend.
  - **Source/PR:** E-msg; PR #9.
  - **Test evidence:** 183 JVM foundation/fake; chưa có Messaging UI runtime evidence.
  - **Remaining work:** Host mount; create/get DIRECT REST và persistence chưa có.
  - **Dependency/blocker:** TV2 wire contract; TV1 session/host.
  - **Acceptance criteria:** UI list/direct chat mở được qua host, trạng thái tải/lỗi rõ; fake được ghi nhãn.
  - **Next action:** Nối host theo boundary đã duyệt, giữ transport adapter độc lập.

- [~] **D03-TV3 — PARTIAL**

  - **Ngày/milestone:** Day 3 — Auth, Chat và Groups UI.
  - **Owner:** TV3.
  - **Deliverable:** Groups list/create/detail UI và API create/list.
  - **Source/PR:** E-group; PR #6.
  - **Test evidence:** 37 JVM; E-db; screenshots cũ không chứng minh tích hợp thật.
  - **Remaining work:** Official review, lifecycle fixes, runtime session/directory/host.
  - **Dependency/blocker:** TV3 fixes + TV1 identity/PeopleDirectory.
  - **Acceptance criteria:** Account thật tạo/list/detail nhóm qua API từ Home.
  - **Next action:** Sửa lỗi phạm vi TV3 trên nhánh riêng sau audit, xin review.

### Day 4: Real API integration — Auth, Message, Groups

- [!] **D04-TV1 — BLOCKED**

  - **Ngày/milestone:** Day 4 — Real API integration — Auth, Message, Groups.
  - **Owner:** TV1.
  - **Deliverable:** JWT Auth/profile và Android API/error handling thật.
  - **Source/PR:** E-main; PR #11/#12 chỉ contract/challenge.
  - **Test evidence:** 44 JVM không chứng minh Auth server.
  - **Remaining work:** Chưa server Auth/verifyOTP/JWT issuer/profile API.
  - **Dependency/blocker:** Backend baseline + TV1.
  - **Acceptance criteria:** Auth A/B thật, lỗi API nhất quán, profile gắn UUID đúng.
  - **Next action:** Hoàn thiện Auth API sau baseline.

- [ ] **D04-TV2 — TODO**

  - **Ngày/milestone:** Day 4 — Real API integration — Auth, Message, Groups.
  - **Owner:** TV2.
  - **Deliverable:** Send/history API, PostgreSQL Message và idempotency.
  - **Source/PR:** E-msg; E-group schema.
  - **Test evidence:** Fake/domain tests không phải REST/DB Message tests.
  - **Remaining work:** Không có Message REST/DB implementation trên refs đã xem.
  - **Dependency/blocker:** TV2 wire/schema + baseline.
  - **Acceptance criteria:** Gửi retry cùng clientMessageId không nhân bản; history persisted/order đúng.
  - **Next action:** Viết Message migration mới sau approval, REST adapter và integration tests.

- [!] **D04-TV3 — BLOCKED**

  - **Ngày/milestone:** Day 4 — Real API integration — Auth, Message, Groups.
  - **Owner:** TV3.
  - **Deliverable:** Members/invite/accept, ACL và API→UI với account thật.
  - **Source/PR:** E-group; PR #6.
  - **Test evidence:** E-db kiểm tra ACL/idempotency/concurrency; 37 JVM Android.
  - **Remaining work:** Directory mặc định 503; JWT mặc định reject; chưa Auth/host adapter.
  - **Dependency/blocker:** TV1 identity/privacy + backend approval.
  - **Acceptance criteria:** Hai account thật invite/accept qua UI; server kiểm tra quyền.
  - **Next action:** Chốt PeopleDirectory/Auth adapter với TV1.

### Day 5: Contacts, E.164, Friends API, WebSocket

- [~] **D05-TV1 — PARTIAL**

  - **Ngày/milestone:** Day 5 — Contacts, E.164, Friends API, WebSocket.
  - **Owner:** TV1.
  - **Deliverable:** E.164 search/privacy/results và friend-request API.
  - **Source/PR:** E-contacts; PR #14.
  - **Test evidence:** 26 JVM (8 Contacts input); 7 Contacts UI là báo cáo PR, chưa rerun.
  - **Remaining work:** submit luôn UNAVAILABLE; chưa search privacy/API/friend requests.
  - **Dependency/blocker:** TV1 backend User/Friend APIs.
  - **Acceptance criteria:** Search UUID theo E.164 đúng privacy; gửi request thật.
  - **Next action:** Giữ UI foundation; triển khai ContactsRepository và server API.

- [ ] **D05-TV2 — TODO**

  - **Ngày/milestone:** Day 5 — Contacts, E.164, Friends API, WebSocket.
  - **Owner:** TV2.
  - **Deliverable:** WebSocket A/B realtime và cập nhật list/chat.
  - **Source/PR:** E-msg; PR #9.
  - **Test evidence:** Unit fake events không phải WS evidence.
  - **Remaining work:** Không WS server/client thực hoặc demo 2 thiết bị.
  - **Dependency/blocker:** TV2 WS envelope + Auth.
  - **Acceptance criteria:** A gửi B nhận ngay, list/ACK đúng, reconnect được.
  - **Next action:** Chốt WS event và triển khai transport.

- [~] **D05-TV3 — PARTIAL**

  - **Ngày/milestone:** Day 5 — Contacts, E.164, Friends API, WebSocket.
  - **Owner:** TV3.
  - **Deliverable:** Invite/member/role/status UI và forbidden tests.
  - **Source/PR:** E-group; PR #6.
  - **Test evidence:** E-db ACL; 37 JVM; UI không rerun.
  - **Remaining work:** OWNER-only invite hiện có; ADMIN policy/role actions chưa có; live wiring thiếu.
  - **Dependency/blocker:** TV1 identity; team role matrix.
  - **Acceptance criteria:** UI hiển thị quyền/trạng thái thật; unauthorized API bị chặn.
  - **Next action:** Mở rộng ma trận test theo role matrix đã duyệt.

### Day 6: Main workflows — friend requests, ACK, group members and roles

- [ ] **D06-TV1 — TODO**

  - **Ngày/milestone:** Day 6 — Main workflows — friend requests, ACK, group members and roles.
  - **Owner:** TV1.
  - **Deliverable:** Nhận/accept/reject friend request, friend list, mở chat/profile.
  - **Source/PR:** E-main; E-contacts.
  - **Test evidence:** Chưa test workflow Friend API thật.
  - **Remaining work:** Không có workflow server/client đầy đủ.
  - **Dependency/blocker:** TV1 Friend API; TV2 ChatNavigator.
  - **Acceptance criteria:** A/B thực hiện friend workflow và mở đúng conversation/profile.
  - **Next action:** Xây Friend domain/API và navigation adapter.

- [~] **D06-TV2 — PARTIAL**

  - **Ngày/milestone:** Day 6 — Main workflows — friend requests, ACK, group members and roles.
  - **Owner:** TV2.
  - **Deliverable:** ACK sending/sent/error và realtime hai thiết bị.
  - **Source/PR:** E-msg; PR #9.
  - **Test evidence:** 183 JVM gồm state/retry/fake ordering.
  - **Remaining work:** ACK/domain có foundation; không real WS/two-device.
  - **Dependency/blocker:** TV2 real transport + Auth.
  - **Acceptance criteria:** ACK/error/retry không duplicate trên 2 thiết bị.
  - **Next action:** Áp dụng idempotency/ACK contract vào HTTP/WS.

- [ ] **D06-TV3 — TODO**

  - **Ngày/milestone:** Day 6 — Main workflows — friend requests, ACK, group members and roles.
  - **Owner:** TV3.
  - **Deliverable:** Role change/leave/remove và dùng chung Message/Conversation API.
  - **Source/PR:** E-group schema chỉ có enums/status; E-msg.
  - **Test evidence:** Chưa test workflow role/leave/remove thật.
  - **Remaining work:** Không endpoints/UI lifecycle membership revoke; chưa shared message API.
  - **Dependency/blocker:** TV1/TV2 schema approval; TV2 message API.
  - **Acceptance criteria:** Chỉ đúng quyền đổi role/remove; leave/revoke chặn read/send ngay.
  - **Next action:** Chốt owner transfer/last-owner/history rồi triển khai GROUP-02.

### Day 7: Real one-to-one chat demo

- [!] **D07-TV1 — BLOCKED**

  - **Ngày/milestone:** Day 7 — Real one-to-one chat demo.
  - **Owner:** TV1.
  - **Deliverable:** Demo login A/B → tìm → kết bạn → conversation.
  - **Source/PR:** E-main; E-contacts; E-msg.
  - **Test evidence:** Không E2E thật.
  - **Remaining work:** Auth/Friend backend và host còn thiếu.
  - **Dependency/blocker:** TV1 Auth/Friends + TV2 transport.
  - **Acceptance criteria:** Video/log của 2 account thực hiện hết luồng.
  - **Next action:** Chạy integration sau khi các API sẵn sàng.

- [!] **D07-TV2 — BLOCKED**

  - **Ngày/milestone:** Day 7 — Real one-to-one chat demo.
  - **Owner:** TV2.
  - **Deliverable:** Chat 1–1 realtime persisted; đóng/mở lại còn history.
  - **Source/PR:** E-msg; PR #9 chỉ RAM fake.
  - **Test evidence:** Không persisted realtime E2E.
  - **Remaining work:** Chưa REST/WS/Message DB/local persistence thật.
  - **Dependency/blocker:** TV1 Auth; TV2 backend/transport.
  - **Acceptance criteria:** 2 thiết bị chat, reconnect/reopen lịch sử đúng và không duplicate.
  - **Next action:** Thiết lập demo staging dùng dữ liệu thật.

- [!] **D07-TV3 — BLOCKED**

  - **Ngày/milestone:** Day 7 — Real one-to-one chat demo.
  - **Owner:** TV3.
  - **Deliverable:** Tạo nhóm bằng account thật, mount Home và chốt contract.
  - **Source/PR:** E-group; PR #6; E-main Home placeholders.
  - **Test evidence:** 37 JVM/E-db không phải Home/Auth E2E.
  - **Remaining work:** Chưa scope adapter, PeopleDirectory, Home mount; PR6 chưa approved.
  - **Dependency/blocker:** TV1 host/session + baseline + TV3 fixes.
  - **Acceptance criteria:** A tạo nhóm từ Home, B nhận invitation theo policy.
  - **Next action:** Bàn giao Groups entry contract sau sửa lifecycle.

### Day 8: Profile, pagination, group messaging

- [ ] **D08-TV1 — TODO**

  - **Ngày/milestone:** Day 8 — Profile, pagination, group messaging.
  - **Owner:** TV1.
  - **Deliverable:** Profile name/avatar/bio, Profile tab và edit.
  - **Source/PR:** E-main.
  - **Test evidence:** Chưa profile workflow tests.
  - **Remaining work:** Profile tab placeholder; không profile API/upload UI.
  - **Dependency/blocker:** TV1 + shared upload contract.
  - **Acceptance criteria:** Lưu/reload profile thật; avatar/bio đúng account.
  - **Next action:** Chốt profile/upload API rồi triển khai.

- [~] **D08-TV2 — PARTIAL**

  - **Ngày/milestone:** Day 8 — Profile, pagination, group messaging.
  - **Owner:** TV2.
  - **Deliverable:** History pagination/sync cursor/reconnect/dedupe.
  - **Source/PR:** E-msg; PR #9.
  - **Test evidence:** 183 JVM fake/domain pagination/order/dedupe.
  - **Remaining work:** Không real transport/persistent cursor/local store.
  - **Dependency/blocker:** TV2 REST/WS cursor agreement + Message DB.
  - **Acceptance criteria:** Mất mạng/nạp trang không thiếu/trùng tin, cursor bền vững.
  - **Next action:** Nối domain logic vào real repository và test backend/client.

- [!] **D08-TV3 — BLOCKED**

  - **Ngày/milestone:** Day 8 — Profile, pagination, group messaging.
  - **Owner:** TV3.
  - **Deliverable:** Group Message/WS và chỉ ACTIVE member được dùng.
  - **Source/PR:** E-group; E-msg.
  - **Test evidence:** ACL metadata tests không phải group message/history tests.
  - **Remaining work:** Chưa group Message/WS server/client; revoke chưa có.
  - **Dependency/blocker:** TV2 shared chat API + TV3 membership policy.
  - **Acceptance criteria:** Active member chat được; removed/left bị chặn send/read/subscribe.
  - **Next action:** Phối hợp TV2 API và server authorization tại mọi entry point.

### Day 9: Privacy, block/unblock, logout and session expiry (bản gốc còn có media/bảo mật)

- [~] **D09-TV1 — PARTIAL**

  - **Ngày/milestone:** Day 9 — Privacy, block/unblock, logout and session expiry (bản gốc còn có media/bảo mật).
  - **Owner:** TV1.
  - **Deliverable:** Privacy, block/unblock, logout/session expiry.
  - **Source/PR:** E-main Session V2; PR #13.
  - **Test evidence:** Main session policy JVM; PR13 35 JVM + probe E-probe.
  - **Remaining work:** Chưa runtime logout/revoke/block/privacy API; R13-01 còn mở.
  - **Dependency/blocker:** TV1 failure policy/Auth server + TV3 lifecycle.
  - **Acceptance criteria:** Logout/switch/expiry khóa dữ liệu ngay; block áp dụng server.
  - **Next action:** Sửa TV2-R13-01 và chốt refresh/logout contract.

- [ ] **D09-TV2 — TODO**

  - **Ngày/milestone:** Day 9 — Privacy, block/unblock, logout and session expiry (bản gốc còn có media/bảo mật).
  - **Owner:** TV2.
  - **Deliverable:** Upload image/file/video, type/size/ACL và metadata.
  - **Source/PR:** E-msg; main/backend inventory.
  - **Test evidence:** Không media transport/integration tests.
  - **Remaining work:** Không upload/download/storage/metadata implementation.
  - **Dependency/blocker:** TV2 + common backend + membership ACL.
  - **Acceptance criteria:** Media thật có giới hạn/ACL và metadata gắn message.
  - **Next action:** Chốt upload contract và triển khai media P0.

- [~] **D09-TV3 — PARTIAL**

  - **Ngày/milestone:** Day 9 — Privacy, block/unblock, logout and session expiry (bản gốc còn có media/bảo mật).
  - **Owner:** TV3.
  - **Deliverable:** Test invite/leave/remove/roles và revoke read/send ngay.
  - **Source/PR:** E-group; E-db; E-probe.
  - **Test evidence:** ACL/invite DB tests có; probe lifecycle phát hiện lỗi.
  - **Remaining work:** Thiếu leave/remove/role API, message/history revoke; lock/capacity lỗi.
  - **Dependency/blocker:** TV3 GROUP-02 + TV2 authorization boundary.
  - **Acceptance criteria:** Revoke atomic, stale session/WS không đọc/gửi; negative cases đầy đủ.
  - **Next action:** Bổ sung test revocation và sửa lifecycle trước integration.

### Day 10: Multilingual UI, media and P0 stability

- [~] **D10-TV1 — PARTIAL**

  - **Ngày/milestone:** Day 10 — Multilingual UI, media and P0 stability.
  - **Owner:** TV1.
  - **Deliverable:** VI/EN, chọn locale và lưu setting, màn dùng chung.
  - **Source/PR:** E-main resources.
  - **Test evidence:** Build PASS không chứng minh locale roundtrip.
  - **Remaining work:** Có tài nguyên UI; chưa settings/locale persistence/shared-screen acceptance.
  - **Dependency/blocker:** TV1 settings/Profile shell.
  - **Acceptance criteria:** Đổi VI/EN, reopen/rotation còn lựa chọn và text nhất quán.
  - **Next action:** Hoàn thiện locale setting/persistence và kiểm tra màn chung.

- [ ] **D10-TV2 — TODO**

  - **Ngày/milestone:** Day 10 — Multilingual UI, media and P0 stability.
  - **Owner:** TV2.
  - **Deliverable:** Android media/retry/outbox/reconnect P0 stability.
  - **Source/PR:** E-msg; fake outbox only.
  - **Test evidence:** Fake/domain retry không chứng minh media thật.
  - **Remaining work:** Media Android/storage/transport và persisted outbox thiếu.
  - **Dependency/blocker:** TV2 media/REST/WS + Auth.
  - **Acceptance criteria:** Upload/download/retry/reconnect trên 2 thiết bị không duplicate/leak.
  - **Next action:** Hoàn thiện media sau contract, test loss/reconnect.

- [~] **D10-TV3 — PARTIAL**

  - **Ngày/milestone:** Day 10 — Multilingual UI, media and P0 stability.
  - **Owner:** TV3.
  - **Deliverable:** Group history/member management/error UI; chuẩn bị translation UI.
  - **Source/PR:** E-group; PR #6.
  - **Test evidence:** 37 JVM; E-db cho Group01; chưa history/translation tests.
  - **Remaining work:** Chỉ list/detail/invite errors; chưa message history/role mgmt/translation.
  - **Dependency/blocker:** TV2 history API; TV3 GROUP-02.
  - **Acceptance criteria:** Group history/member UI đủ trạng thái lỗi; translation boundary rõ.
  - **Next action:** Ưu tiên P0 history/member, ghi spec translation riêng.

### Day 11: P0 integration testing

- [!] **D11-TV1 — BLOCKED**

  - **Ngày/milestone:** Day 11 — P0 integration testing.
  - **Owner:** TV1.
  - **Deliverable:** Integration Auth/Contacts/Profile/Settings/tabs/rotation.
  - **Source/PR:** E-main; E-contacts; E-ui15.
  - **Test evidence:** Reviewer UI foundation API37; không full integration test.
  - **Remaining work:** API thật và Home module wiring chưa có.
  - **Dependency/blocker:** TV1/TV2/TV3 integration.
  - **Acceptance criteria:** Auth→4 modules, rotation/expiry lỗi mạng và account switch đều đạt.
  - **Next action:** Tạo ma trận integration theo account/epoch sau wiring.

- [!] **D11-TV2 — BLOCKED**

  - **Ngày/milestone:** Day 11 — P0 integration testing.
  - **Owner:** TV2.
  - **Deliverable:** Integration direct/group/WS/reconnect/dedupe/media.
  - **Source/PR:** E-msg.
  - **Test evidence:** 183 JVM foundation không thay integrated device tests.
  - **Remaining work:** Chưa transport/group/media backend.
  - **Dependency/blocker:** TV2 transport + TV3 group ACL + TV1 Auth.
  - **Acceptance criteria:** 2 thiết bị direct/group/media dưới reconnect; persisted history đúng.
  - **Next action:** Chạy P0 suite trên staging và lưu XML/log/video.

- [!] **D11-TV3 — BLOCKED**

  - **Ngày/milestone:** Day 11 — P0 integration testing.
  - **Owner:** TV3.
  - **Deliverable:** Multi-account Groups/roles/history/media integration.
  - **Source/PR:** E-group; E-probe.
  - **Test evidence:** 37 JVM/E-db; chưa multi-account Android E2E.
  - **Remaining work:** Scope issues + real identity/host/chat/media chưa kết nối.
  - **Dependency/blocker:** TV1/TV2 adapters; TV3 fixes.
  - **Acceptance criteria:** A/B/C roles/isolation/revoke/history/media đều có evidence.
  - **Next action:** Lập suite role×membership×session rồi chạy khi tích hợp.

### Day 12: P1 features — Translation and Dual Chat Mode

- [ ] **D12-TV1 — TODO**

  - **Ngày/milestone:** Day 12 — P1 features — Translation and Dual Chat Mode.
  - **Owner:** TV1.
  - **Deliverable:** Settings/preferences/polish/Dual Mode Home nếu P0 đạt.
  - **Source/PR:** E-main Home placeholders.
  - **Test evidence:** Chưa Dual Mode/Settings acceptance tests.
  - **Remaining work:** Chưa implementation; điều kiện P0 chưa đạt.
  - **Dependency/blocker:** P0 gate + team Dual Mode spec.
  - **Acceptance criteria:** Settings bền vững; Normal/International đúng scope nếu được mở P1.
  - **Next action:** Để P1 sau P0, không dùng polish thay acceptance.

- [~] **D12-TV2 — PARTIAL**

  - **Ngày/milestone:** Day 12 — P1 features — Translation and Dual Chat Mode.
  - **Owner:** TV2.
  - **Deliverable:** Draft/reply/edit/recall có điều kiện; nếu P0 chưa đạt sửa realtime/media.
  - **Source/PR:** E-msg; PR #9 draft foundation.
  - **Test evidence:** Unit foundation; không reply/edit/recall transport tests.
  - **Remaining work:** Draft còn foundation; reply/edit/recall chưa có; phải ưu tiên realtime/media.
  - **Dependency/blocker:** P0 gate; edit/recall backend contract.
  - **Acceptance criteria:** P0 ổn trước P1; draft/reply/edit/recall có policy/server tests nếu nhận scope.
  - **Next action:** Đóng real transport/media trước, giữ P1 conditional.

- [ ] **D12-TV3 — TODO**

  - **Ngày/milestone:** Day 12 — P1 features — Translation and Dual Chat Mode.
  - **Owner:** TV3.
  - **Deliverable:** Manual translation, original+translated/language/Dual Mode nếu P0 đạt.
  - **Source/PR:** PR #6 docs proposal; không translation implementation.
  - **Test evidence:** Không translation API/UI/failure tests.
  - **Remaining work:** Chưa API/provider/UI/cache/error/language/Dual Mode.
  - **Dependency/blocker:** P0 gate + TV2 message representation + TV1 mode setting.
  - **Acceptance criteria:** Giữ original; dịch thủ công đúng ngôn ngữ; lỗi không mất tin; chế độ đúng.
  - **Next action:** Soạn contract proposal, chỉ triển khai khi team mở P1.

### Day 13: Full regression, APK and documentation

- [~] **D13-TV1 — PARTIAL**

  - **Ngày/milestone:** Day 13 — Full regression, APK and documentation.
  - **Owner:** TV1.
  - **Deliverable:** Full E2E/rotation/expiry; APK/README/secrets.
  - **Source/PR:** E-main; E-ui15; audit build logs.
  - **Test evidence:** assembleDebug +44 JVM PASS; reviewer foundation UI API37.
  - **Remaining work:** Chưa full E2E/compatibility/release APK/handover scan.
  - **Dependency/blocker:** P0 integration gate.
  - **Acceptance criteria:** Release candidate APK + regression evidence/README không chứa credentials.
  - **Next action:** Chạy release regression khi P0 đạt, ghi artifact SHA.

- [~] **D13-TV2 — PARTIAL**

  - **Ngày/milestone:** Day 13 — Full regression, APK and documentation.
  - **Owner:** TV2.
  - **Deliverable:** Chat/Media 2 thiết bị, backend/demo data/instructions.
  - **Source/PR:** E-msg docs/tests.
  - **Test evidence:** 183 JVM foundation; không real ChatMedia demo.
  - **Remaining work:** Chưa real backend/media demo/regression, dataset/instructions đầy đủ.
  - **Dependency/blocker:** TV2 transport + common backend.
  - **Acceptance criteria:** 2 thiết bị thực hiện demo lặp lại theo README.
  - **Next action:** Chuẩn bị staging fixtures và kịch bản demo P0.

- [~] **D13-TV3 — PARTIAL**

  - **Ngày/milestone:** Day 13 — Full regression, APK and documentation.
  - **Owner:** TV3.
  - **Deliverable:** Groups/Translation/Dual Mode regression, QA checklist/screens/video.
  - **Source/PR:** E-group docs/screenshots; E-db; E-probe.
  - **Test evidence:** JVM/backend evidence; ảnh cũ chỉ UI riêng.
  - **Remaining work:** Thiếu integrated regression/video; translation chưa có; docs session stale.
  - **Dependency/blocker:** TV3 fixes + P0; P1 chỉ khi được nhận.
  - **Acceptance criteria:** QA role/isolation/history/media PASS; P1 ghi đúng delivered/deferred.
  - **Next action:** Sau audit cập nhật QA report theo SHA và runtime thật.

### Day 14: Final demo and handover

- [!] **D14-TV1 — BLOCKED**

  - **Ngày/milestone:** Day 14 — Final demo and handover.
  - **Owner:** TV1.
  - **Deliverable:** Final APK/Git PR/AuthContactsProfile report, điều phối bàn giao.
  - **Source/PR:** E-main; inventory PR.
  - **Test evidence:** Debug build chưa phải final release acceptance.
  - **Remaining work:** Chưa final integrated APK/demo/signoff.
  - **Dependency/blocker:** Tất cả P0 + owner approvals.
  - **Acceptance criteria:** APK có version/SHA, hướng dẫn/demo và báo cáo được nhóm chấp nhận.
  - **Next action:** Chỉ đóng sau nghiệm thu Day13.

- [!] **D14-TV2 — BLOCKED**

  - **Ngày/milestone:** Day 14 — Final demo and handover.
  - **Owner:** TV2.
  - **Deliverable:** Realtime/reconnect/media, DB logs và final fixes.
  - **Source/PR:** E-msg.
  - **Test evidence:** Chưa two-device production E2E.
  - **Remaining work:** Thiếu real transport/media/persisted demo.
  - **Dependency/blocker:** TV2 delivery + P0 integration.
  - **Acceptance criteria:** Demo realtime/media/reconnect và logs không chứa secret.
  - **Next action:** Lưu bằng chứng end-to-end rồi chốt báo cáo.

- [!] **D14-TV3 — BLOCKED**

  - **Ngày/milestone:** Day 14 — Final demo and handover.
  - **Owner:** TV3.
  - **Deliverable:** Groups/roles/group chat, translation nếu làm, screenshots/presentation.
  - **Source/PR:** E-group; E-msg.
  - **Test evidence:** Branch tests không đủ final acceptance.
  - **Remaining work:** Chưa tích hợp Groups/chat; roles thiếu; P1 chưa triển khai.
  - **Dependency/blocker:** TV1/TV2 integration + TV3 fixes.
  - **Acceptance criteria:** Demo account thật, quyền/revoke đúng; P1 khai đúng trạng thái.
  - **Next action:** Chạy demo nhóm rồi bàn giao evidence/report.

## 3. TV1 Progress

| Mảng | Hiện trạng có bằng chứng | Phần chưa thể đóng / việc TV1 cần làm |
|---|---|---|
| Auth | #7 preview; #11 AuthRepository challenge contract/test fake; #12 VM inject/cancel/state restoration; approved và merged | Default AuthViewModel dùng null repository → preview; submit với injection chỉ startRegistrationChallenge. Không verify OTP/login/token issuance/secure persistence. FakeAuthRepository ở src/test. Chốt OTP demo có kiểm soát hay provider thực, rate limit/expiry/retry rồi triển khai thật. |
| Session | #8 SessionRepository/SessionSnapshot/SessionAccessPolicy/CredentialProvider contracts approved merged; UUID + epoch, CHECKING/UNAVAILABLE và fail-closed policy có tests | Chưa runtime provider/refresh/logout/Keystore. #13 coordinator package-private, không launcher wiring; TV2-R13-01 còn mở. Không mở Home chỉ vì có interface hoặc preview state. |
| Contacts | #14 input/UI foundation approved đúng HEAD, chưa merged | ContactsFragment.submit luôn render UNAVAILABLE; fixtures/results không phải User API. Search privacy/Friend request/accept/reject/list/block vẫn thiếu. Chốt PeopleDirectory adapter phục vụ Groups. |
| Home | #10 bốn tab placeholder, giữ selectedTab; #15 test-only inset/tap regression approved chưa merged | MainActivity/activity_main vẫn khởi tạo AuthFragment; Home chưa gắn authenticated gate hoặc modules. TV1 sở hữu MainActivity/Home, phối hợp mount/unmount/selection/resume/back với TV2/TV3. |
| Profile | Placeholder ở Home | Không có profile read/edit/name/avatar/bio server và UI đầy đủ. |
| Settings | Đã có strings Auth/Home mặc định và values-vi; chưa settings workflow được nghiệm thu | Cần lựa chọn locale VI/EN và persistence, privacy/block/logout, preferences và conditional Dual Mode; kiểm tra rotation/reopen/account scope. |

AuthViewModel có invalidate attempt trước cancel và view watcher được tháo khi destroy: đây là điểm tốt đã có unit evidence, không biến thành Auth production. JWT refresh/logout cần contract issuer/audience/expiry/rotation/revocation và failure policy chính thức.

**TV2-R13-01 — owner TV1, reviewer TV2:** resourceFactory.open hoặc Resources.invalidate có thể ném recoverable exception ra SessionRepository observer/onSessionChanged. Source #13 gọi trực tiếp factory và cleanup (các đường open/obsolete prepared/retire old), chưa có failure containment đầy đủ. Observer contract yêu cầu không throw. Probe tái hiện factory/cleanup exception với actual Coordinator; destination còn fail-closed. Không có bằng chứng P0/P1 crash, cross-account leak hoặc permanent subscription loss; giữ severity review gốc **P2**. TV1 cần quy định recovery cho initial subscription, partial allocation, old/obsolete scope cleanup, giữ invalidation trước mọi side effect và retry an toàn; không catch fatal JVM errors bừa bãi. TV3 không sửa file TV1; Groups phải bảo đảm hook cleanup của mình đáp ứng contract mới.

## 4. TV2 Progress

| Mảng | Đã có trên PR9 | Thiếu để nghiệm thu |
|---|---|---|
| Messaging Foundation | Account-scoped controller/execution, view/router/domain, FakeConversationRepository, ordering/dedupe/pagination/read state/outbox foundation; 183 JVM actual | Official review/approval chưa có trên PR; chưa merged. Fake ở src/main nhưng RAM, không REST/Room/PostgreSQL production. |
| API Contract | ConversationRepository CompletionStage ports cho list/openDirect/history/outbox/send/markRead; message ID + server sequence domain | DAY1_DECISIONS còn chưa khóa JSON content/text/body, attachmentId(s), reply reference, REST cursor, group receipts/history/block, WS envelope. Chốt wire contract trước DB/client transport. |
| Host Integration | MessagingView(controller, controller.getRouter()), setHostState(selected,resumed) và scoped controller là boundary có thể bàn giao | Chưa Home mount/session adapter/ChatNavigator chung; không có production gate. TV1 quyết định shell. |
| REST/WebSocket | Fake events và retry logic có tests | Không real REST/WS server/client, JWT handshake, ACK transport, persisted Message/sequence/cursor, reconnect E2E. |
| Media | Chưa implementation thật được tìm thấy trong refs đã kiểm tra | Upload/download/storage/type/size/ACL/metadata và Android progress/retry; image/file/video P0 chưa hoàn thành. |
| Realtime testing | Domain/fake unit cases mạnh | Foundation instrumentation hiện có không phải Messaging UI test. Không có demo A/B real persisted chat, group WS/media/reopen. |

MessagingExecution có giới hạn leases2/callers4/operations4/settlement workers4/mailbox4 và max workers10; source theo dõi worker/source-invocation quiescence, external repo/future completion ngoài account gate. Đây là nền tảng hữu ích cho TV3 tham khảo **nguyên tắc**, không copy implementation qua owner boundary. Execution.close được thiết kế nonblocking; awaitTermination riêng. Repository port chưa chốt physical HTTP/WS cancellation/timeout. Không suy ra rằng giới hạn task/future đã chứng minh socket/HTTP termination.

## 5. TV3 Progress

### 5.1 Shared Backend Foundation và Database

PR6 có một backend Spring Boot3.5.16, Java21/Maven, JPA/Validation/resource-server JWT, Flyway/PostgreSQL; Compose PostgreSQL16-alpine có healthcheck và password qua env, localhost port5432. Hibernate ddl-auto=validate. Maven verify actual PASS25 (12 service +13 PostgreSQL). Tests kiểm tra migration/schema, constraints, ACL, idempotency và concurrent group requests; không phải real JWT issuer/real identity provider/E2E.

V1 `backend/src/main/resources/db/migration/V1__shared_conversations_and_group_invitations.sql` hiện chứa:
- users chỉ id; conversations; conversation_members;
- group_invitations; group_creation_keys; group_write_budgets;
- role OWNER/ADMIN/MEMBER; member status ACTIVE/LEFT/REMOVED; invitation status PENDING/ACCEPTED/REJECTED/CANCELLED;
- FK/unique membership, index, tối đa một ACTIVE OWNER bằng partial unique index, một PENDING invite/group/invitee.
- `last_read_message_id` hiện chưa giải quyết monotonic server sequence/read cursor của TV2; không có Message schema hoặc canonical DIRECT-pair unique constraint.

**PENDING là invitation status, không phải đã có ACTIVE member.** Unique owner chỉ hạn chế tối đa một; workflow last-owner/transfer/leave vẫn cần policy, không suy ra đầy đủ invariant “luôn có một owner” từ enum/index.

SecurityFilterChain hiện do Groups configuration nắm anyRequest authenticated; issuer/audience boundary có code, nhưng không cấu hình issuer thì JWT decoder reject all. GroupDirectory mặc định 503. Đây là fail-closed foundation, không phải đã tích hợp Auth/privacy/User API. Request body limit64KiB, page/batch cap100 và success-write budget30/phút có ích; không thay client admission bounds.

Các PostgreSQL case đã chạy gồm restAuthPaginationAndValidation, concurrentCreationWithSameKeyCreatesOneGroup, rateLimitIsTransactionalAndShared, duplicatePendingConstraint, rejectionPersistsWithoutMembership, invalidInviteeRollsBackKeyAndConversation, memberInsertFailureRollsBackAcceptance, persistsAndRetryIsIdempotent, acceptAndRejectPersistAtomically, concurrentAcceptanceCreatesOneMembership, oversizedJsonBodyRejected, onlyOwnerCanInspectAndSendInvitations và moreThanOneBatchWithoutGroupMemberCap. Các case này không kiểm chứng role-change/leave/remove hoặc group-message revocation chưa được triển khai.

**Đề xuất tách baseline — chưa thực thi trong audit:**

1. TV3 gửi scope/ADR để TV1 và TV2 review trước: một backend Maven/runtime duy nhất, Java/SpringBoot/Postgres/Flyway/config health/error envelope, ownership global security, shared users/conversation/member schema boundaries.
2. Lập manifest file/hunk dùng chung từ PR6 thay vì copy toàn bộ PR6: build/config/Compose/Flyway bootstrap và shared schema đã được duyệt. Groups controllers/services/invitations/idempotency budgets và Groups-specific tests giữ domain PR. Không tạo backend thứ hai, không chuyển global security ownership âm thầm.
3. **Trước khi tách SQL:** xác nhận V1 đã chạy ở môi trường chung nào chưa bằng flyway_schema_history/checksum; audit không có quyền/evidence truy cập shared DB. Nếu đã applied: giữ nguyên V1, thay đổi bằng migration tăng version được phê duyệt; không sửa/đổi tên/chia lại migration đã applied. Nếu chưa applied ở mọi môi trường liên quan, nhóm có thể duyệt initial migration decomposition; vẫn cần approval, không tự sửa.
4. TV1 chốt JWT issuer/sub/audience và directory/privacy adapter; TV2 chốt DIRECT uniqueness, Message sequence/read cursor/FKs. Không auto-adapt contract chỉ để compile. Baseline cần clean migrate + restart validation + PostgreSQL constraints tests và seed demo **2 users/1 group** riêng có kiểm soát (test fixtures hiện có không thay deliverable seed).
5. Sau approved baseline PR được owner merge, TV3 mới xử lý Groups follow-up dựa trên baseline theo quy tắc Git của nhóm. Không rebase #6 chỉ vì main đổi. `backend-baseline-proposal.md` hiện mới là đề xuất, chưa có independent baseline PR.

### 5.2 GroupsAccountScope — audit theo từng tiêu chí

Source: [GroupsAccountScope](https://github.com/datcaq/NEXA-Chat/blob/d2141e2560d8d6ba198942c944a69a963c133923/app/src/main/java/com/nexa/chat/feature/groups/GroupsAccountScope.java), [RemoteGroupRepository](https://github.com/datcaq/NEXA-Chat/blob/d2141e2560d8d6ba198942c944a69a963c133923/app/src/main/java/com/nexa/chat/feature/groups/data/RemoteGroupRepository.java), [GroupsIntegration](https://github.com/datcaq/NEXA-Chat/blob/d2141e2560d8d6ba198942c944a69a963c133923/app/src/main/java/com/nexa/chat/feature/groups/GroupsIntegration.java).

| Tiêu chí | Kết quả và bằng chứng | Remaining / acceptance |
|---|---|---|
| UUID/sessionEpoch | Constructor nhận UUID và epoch, kiểm tra ownership; VM saved state có owner/generation guard | Chưa adapter Session V2 production. Chỉ trusted authenticated snapshot cấp identity; refresh giữ epoch, switch/logout thay epoch. |
| ACTIVE/SUSPENDED/EXPIRED | Projection qua Supplier; callback suspend/defer, resume drain, expire clear/close có unit tests | Không gọi external supplier/callback dưới state lock; test reentrancy/concurrent transitions. |
| Refresh/account switch/logout | Có logical model và tests stale callback/state reset | Không có runtime SessionCoordinator hookup hoặc real logout. Unit fake không là E2E switch. |
| SavedStateHandle | VM xóa drafts/key/lists khi owner không hợp lệ; stale cleanup không được wipe epoch mới; onCleared ngăn mutations | Rotation lifecycle mới cần run thật; saved state không chứa token và không render data account cũ. |
| External callbacks trong lock | **FAIL — AUD-GRP-01 (P1)**: runIfCurrent line67 và deliver line85 gọi action.run trong synchronized(lock); currentSession.get cũng external trong lock | Tách state admission khỏi external work, recheck logical validity khi commit; không thực hiện callback/I/O/future completion/cleanup hook dưới lock. |
| Immediate logical invalidation / UI nonblocking | **FAIL về bảo đảm hiện tại**: external action giữ lock khiến close chưa set closed. Probe EXTERNAL_ACTION_BLOCKS_CLOSE=true bằng worker close timeout150ms trong lúc action chạy | Invalidate phải bounded/nonblocking với external task đang treo. Probe không chứng minh deadlock hoặc Android crash; đây là lock-induced wait tái hiện được. |
| Deferred capacity | **FAIL — AUD-GRP-02 (P1)**: deferred ArrayList không bound; probe nhận1024 callbacks SUSPENDED | Có capacity/overflow/coalescing policy rõ, không stale replay vô hạn; resume/expire/close tests. close hiện clear queue là điểm tốt. |
| Request admission | **FAIL — AUD-GRP-02 (P1)**: RemoteGroupRepository HashSet calls/add/enqueue không cap | Giới hạn in-flight + pending; overload response/retry có policy, không chặn UI. Server page/budget không thay client bound. |
| Cancellation vs termination | **PARTIAL — AUD-GRP-03 (P2)**: close marks closed, clears tracked calls và Call.cancel ngoài lock | Chưa bằng chứng physical termination/quiescence; callback đã admitted có thể còn chạy. VM guard là logical defense, không xác nhận physical stop. Chốt bounded teardown/ack và kiểm thử race. |
| Cleanup idempotence/ownership | Own feature repos/directories; không close shared HTTP client; all-resource cleanup attempts trước aggregate throw; unit tests có close ownership | Kiểm tra repeated/concurrent close, late resource registration, exactly-once own resources, no shared-resource disposal; tests không đủ chứng minh mọi external cleanup idempotent. |
| Failure containment | **FAIL — AUD-GRP-04 (P2)**: close line209 throw aggregated RuntimeException; static synchronized GroupsIntegration.install đóng old scope đồng bộ và rethrow recoverable failure | Phối hợp TV1 TV2-R13-01; logical invalidation trước cleanup; report lỗi/module recovery không phá observer hoặc bỏ partial allocation. Chưa có adapter thật nên đây là integration risk, chưa phải observed production crash. |
| UI lifecycle | Activity hide fields/dismiss dialogs khi suspended, finish/close expired, unsubscribe có source | Compilation/screenshots cũ không chứng minh instrumentation lifecycle mới; test rotation/background/expiry/switch trên device sau fixes. |
| Cross-account isolation | Epoch/generation guards và fail-closed model có tests, không thấy chứng cứ leak actual trong audit | Không tuyên bố leak-free production khi chưa E2E; test old callback/queued completion/teardown failure/switch lặp với account thật. |

Probe log: EXTERNAL_ACTION_BLOCKS_CLOSE=true; SUSPENDED_DEFERRED_ACCEPTED=1024; DEFERRED_AFTER_CLOSE=0; CLEANUP_EXCEPTION_ESCAPES=true. Đây là 3 probe riêng, không thêm 3 vào JVM test count. TV3 có thể sửa AUD-GRP-01..04 trên nhánh sở hữu sau audit; lượt này chỉ ghi nhận, không thay source hay tests.

### 5.3 Groups API and Membership

| Chức năng | Branch PR6 có gì | Giới hạn |
|---|---|---|
| Create/list/detail/members | REST/API/UI/repository nền, transactional create OWNER +pending invites | Chưa approved/merged/main/runtime identity. List pagination page1–100; cần tích hợp thật. |
| Invitations/accept/reject | Pending records; accept lock/atomic active member +status; reject không tạo membership | Invite bổ sung OWNER-only hiện tại; không suy ra ADMIN có quyền. Directory được revalidate nhưng default503. |
| Idempotency/concurrency | Group creation key theo user/key +request hash chuẩn hóa invitees; lock/reservation và transactional tests | Không phải Message clientMessageId implementation; test-only providers không thay Auth. |
| Authorization | Active membership cho đọc; conceal resource404; unauthorized/ACL DB tests | Không có role change/leave/remove APIs, history/read/send/group WS revocation enforcement. |
| Roles/status | Enums +DB constraints có | Enum LEFT/REMOVED/ADMIN không chứng minh workflow đã làm. Owner transfer/last owner/pending invitation privacy cần approved policy. |
| History/revocation | Chưa implementation Message/History | GROUP-02 phải phối hợp TV2 để enforce read/send/subscribe/attachment access ngay sau revoke, không chỉ ẩn nút UI. |

### 5.4 Cross-module Integration

| Boundary | Hiện trạng | Điều kiện tích hợp |
|---|---|---|
| PeopleDirectory/GroupDirectory | Interfaces; default backend directory unavailable; Android directory port chưa real User/Friend lookup | TV1 cung cấp UUID identity/privacy/block policy và adapter; không giả lập production user. |
| GroupRepository | RemoteGroupRepository +fake/tests branch có; transport cancel/request admission hạn chế ở trên | API URL/auth credential scoped injection, overload/physical termination contract; không giữ stale token/user. |
| GroupsIntegration | Static install/account scope/entry boundary có | Chưa Session V2 adapter; cleanup đồng bộ/exception phải sửa, không tự gọi từ observer hiện tại. |
| ChatNavigator | Groups signature (Activity,String conversationId) | TV2 router/domain UUID và host boundary cần adapter được owner thống nhất; cùng String/UUID ý tưởng không chứng minh wire compatibility. |
| Home navigation | Main 4 placeholders | TV1 mount Groups/Contacts/Messaging đúng tab/selection/resume/back; TV3 không sửa Home/MainActivity. |
| SessionCoordinator | #13 chưa merged/wired, còn request changes | TV1 xử lý TV2-R13-01 + module failure policy; TV3 cung cấp safe invalidation/cleanup boundary. |
| Membership→Messaging | Conversation/member schemas và domain types có ý tưởng chung | Chưa shared Message/API/WS/historical visibility/revoke semantics; TV2 và TV3 chốt trước migration. |

`docs/group-01/README.md`, `verification.md`, `session-v2-integration.md` có nhận định main cũ/Session V2 chưa merged ngày24/11; nay #8/#11/#12 đã merged vào main8b730. **Các đoạn đó stale**, không dùng làm snapshot hiện tại. Audit không sửa tài liệu cũ; cập nhật sau trên công việc riêng đúng phạm vi.

### 5.5 Translation and Dual Chat

Translation API/provider, original+translated representation, language selection, manual translation, failure handling và Normal/International mode **chưa có implementation được tìm thấy trên các refs đã audit**. Proposal/UI placeholder không được tính hoàn tất. P1 chỉ bắt đầu sau P0 gate; không đánh dấu N/A vì vẫn thuộc kế hoạch. Contract cần giữ original message bất biến, bản dịch không ghi đè nội dung gốc, lỗi/provider timeout không mất tin, language/cache/per-user preference rõ; cần TV2 message representation và TV1 mode setting trước tích hợp.

## 6. Shared Contract Matrix

“Định hướng trong trao đổi” không thay official approval đúng contract/HEAD. Approved Session V2 là contract, không đồng nghĩa runtime provider. Mọi thay đổi shared schema cần TV1+TV2 review trước.

| Contract | Owner | Trạng thái đối chiếu | Dependency / approval cần có |
|---|---|---|---|
| Java +XML / SpringBoot +PostgreSQL +Flyway / /api/v1 | Cả nhóm; TV3 backend | Android có trong main; backend trong PR6; là định hướng chung, baseline chưa approved | Baseline scope PR độc lập; TV1/TV2 duyệt global config/security/schema. |
| User UUID | TV1 | Session V2 approved merged; PR6 FK UUID; TV2 domain UUID | Real server user source/lookup và mapping mọi DTO chưa tích hợp. |
| Session epoch | TV1; consumers TV2/TV3 | Contract #8 approved merged: same epoch refresh, new epoch account switch/logout | Module adapters +failure/cleanup policy; no token hoặc private state trong snapshot/saved UI. |
| JWT sub | TV1 | Định hướng UUID; PR6 resource-server boundary parse/validate theo config | Chưa issuer production/OTP/token/refresh/logout; chốt audience/TTL/revocation official. |
| Conversation ID / DIRECT/GROUP | TV2 +TV3 | UUID/type domain tương thích ý tưởng; PR6 schema có conversations | DTO shape/canonical DIRECT uniqueness và create/get contract cần approval; chưa shared backend delivery. |
| ConversationMember / role | TV3 +TV2 | PR6 OWNER/ADMIN/MEMBER; không đủ role workflows | Quyền ADMIN/owner transfer/last-owner và group-chat authorization cần duyệt. |
| Membership status | TV3 | ACTIVE/LEFT/REMOVED; PENDING thuộc invitation | Không gộp PENDING thành member access; approved lifecycle +WS/history revoke policy cần có. |
| Message ID / server sequence | TV2 | Domain/fake phân biệt server ID/sequence; backend Message chưa có | DB constraints/order authority/new migration và DTO contract. |
| Read cursor | TV2 +TV3 | **Xung đột cần giải quyết trước integration:** domain monotonic server sequence vs V1 last_read_message_id | Chốt cursor semantics/receipt DTO/history pages; migration tăng version nếu V1 applied, không sửa âm thầm. |
| clientMessageId | TV2 | Domain/fake idempotency có tests; chưa server Message unique constraint | Chốt scope uniqueness (sender/account/conversation theo contract), retries/ACK và DB tests. |
| Idempotency-Key | TV3 | Group create branch implemented/tested per-user key/hash | Chưa shared approved HTTP policy; không đồng nhất với clientMessageId. |
| REST error format | TV1/TV2/TV3 | Group branch có code/message/requestId; Messaging domain failure riêng | Chưa shared wire error approval; map domain→wire rõ, không gọi khác type nội bộ là lỗi compile. |
| WebSocket envelope | TV2 | Chưa wire contract locked hoặc real transport | Event types/version/id/sequence/ACK/auth/reconnect và group revoke semantics cần 3 owner xác nhận. |
| Membership authorization | TV3 +TV2 | PR6 metadata reads active ACL; Message/WS/media chưa có | Kiểm tra mọi read/send/subscribe/download ở server; immediate revocation và race tests. |
| History visibility | TV3 +TV2 | Chưa chốt before-join/after-leave/removed/read horizon | ADR/privacy approval trước Message migration/API. |
| Resource lifecycle | TV1 +TV2 +TV3 | Session observer nonthrow contract; PR13 violation; Groups lock/bounds/failure issues; Messaging bounded execution foundation | Chốt admission/invalidation/cleanup/physical termination/recovery, official review và negative/race tests. |
| PeopleDirectory/privacy | TV1 +TV3 | Interface/proposal, backend default503 | Real user existence/block/privacy adapter và authorized lookup; không ship fake là thật. |
| Host/ChatNavigator | TV1 host; TV2 navigation; TV3 entry | Boundary proposals khác module, chưa adapter | Selected/resumed/close/back/account epoch compatibility và integration tests do owners duyệt. |
| Translation / Dual Mode | TV3 +TV2 +TV1 | Proposal/P1 chưa implemented | P0 gate; original/translated schema/provider error/lang preference/mode ownership approval. |

Migration coordination bắt buộc: một Flyway history/version namespace chung; xác nhận V1 applied/checksum; không tạo backend hoặc history thứ hai; không thêm Message/seq/read cursor/FK mới khi TV1/TV2 chưa duyệt. Tests sạch local không chứng minh migration đang chạy an toàn trên shared environment.

## 7. P0/P1/P2 Blocker Register

P0/P1 ở bảng là ưu tiên bàn giao/milestone, **không phải phát hiện security exploit/CVSS**. TV2-R13-01 giữ severity gốc P2. Tất cả issues dưới đây chưa được sửa trong audit.

| ID / ưu tiên | Issue | Impact | Owner | Resolution | Dependency |
|---|---|---|---|---|---|
| B01 P0 | Backend baseline chưa approved/merged; shared schema còn trong PR6 | Auth/Chat/Groups không có backend chung đã chốt | TV3, TV1/TV2 review | Duyệt manifest/ADR, migration history, PR baseline riêng và seed demo | Shared schema/security approval |
| B02 P0 | Auth thật/OTP/JWT/secure session/provider thiếu | Không có account thật hoặc authenticated Home | TV1 | Auth API/repo/token storage/refresh/logout/gate | B01 +OTP policy |
| B03 P0 | Message wire contract/REST/WS/persistence và host chưa tích hợp | Không real 1–1 hoặc group chat | TV2; TV1 host; TV3 ACL | Freeze wire API, migrations, transport và module adapters | B01/B02 +contract approvals |
| B04 P0 | Chưa P0 integrated device E2E/history/revoke/media | Không đóng Day7/11/13/14 hoặc dự đoán deadline tin cậy | Cả nhóm | Staging 2+ accounts, device matrix, evidence XML/log/video/reopen | B01–B03, lifecycle fixes |
| B05 P1 | Profile/Friend/privacy/settings/media workflows còn thiếu | P0 sản phẩm chưa đầy đủ dù foundation build được | TV1/TV2; TV3 ACL | API/UI đầy đủ, integration +negative tests | Auth/common backend |
| AUD-GRP-01 P1 | External supplier/callback trong Groups lock; close chờ callback | Không bảo đảm logical invalidation tức thì/UI nonblocking | TV3 | Admission/commit state tách external work, race/reentrant/blocking-callback tests | Session lifecycle contract |
| AUD-GRP-02 P1 | Unbounded deferred ArrayList và in-flight request Set | Không có bounded workload/overload policy khi suspend/burst | TV3 | Capacity/backpressure/coalesce policy, concurrent overload tests | API retry semantics |
| AUD-GRP-03 P2 | Cancel requested chưa chứng minh actual termination | Không bảo đảm teardown/quiescence của physical requests | TV3 +TV2 transport | Quy định timeout/cancel ack, bounded ownership và race evidence | HTTP/WS adapter contract |
| AUD-GRP-04 P2 | Recoverable cleanup exception thoát Groups close/install | Có thể phá callback coordinator nếu nối trực tiếp | TV3 +TV1 | Invalidate trước cleanup; contain/report/recover; thử multiple failing resources | TV2-R13-01 resolution |
| TV2-R13-01 P2 | PR13 factory/invalidate hook exception thoát observer | Vi phạm observer nonthrow; approval đang blocked | TV1; TV2 re-review | Recovery policy initial/obsolete/retired scopes, partial allocation cleanup; failure-path tests | ResourceFactory/Resources contract |
| B06 P1 | PR6/#9 chưa official review; #14/#15 còn Draft | Không thể tính branch work đã bàn giao | Owner mỗi PR +reviewer khác | Review đúng HEAD, xử lý findings; owner theo quy trình chuyển Draft/merge | Không auto-merge theo audit |
| B07 P1 | API compatibility/UI lifecycle runtime evidence thiếu; no CI runs | Không có acceptance device/rotation đầy đủ | Cả nhóm | Instrumentation thật, API matrix, publish artifacts theo SHA | Device và integrated app |
| B08 P1 conditional | Translation/Dual Mode chưa có | Chưa hoàn thành P1 nếu nhóm nhận delivery này | TV3/TV1/TV2 | Chốt scope sau P0, contract/provider/UI/error/tests | P0 gate |

**Top 5 blocker đối với deadline:** (1) baseline/schema chưa chốt; (2) Auth thật chưa có; (3) real messaging wire/transport/persistence/host thiếu; (4) lifecycle approval gates — PR13 và Groups lock/bounds/cleanup; (5) chưa integrated two-account E2E/group revocation/media. Không có evidence Day1 chính thức nên không gán ngày trễ hoặc ETA giả.

## 8. Next Execution Plan

Milestone kế tiếp đề xuất: **“Day2 nền tảng được duyệt và có boundary an toàn để ba owner làm Day3 song song.”** Chưa chuyển mục tiêu thành final demo khi nền tảng chưa được phê duyệt.

| Task ID | Owner | Priority | Prerequisite | Deliverable | Acceptance criteria |
|---|---|---|---|---|---|
| NEXT-TV3-01 | TV3; TV1/TV2 review | P0 | Audit này, inventory/schema hiện tại | Backend baseline ADR +manifest file/hunk; kiểm tra history V1; seed strategy | Một backend; không copy whole PR6; không sửa applied migration; shared security/schema được TV1/TV2 approval trước code change. |
| NEXT-TV1-01 | TV1; TV2 re-review | P2 finding, chặn lifecycle integration | PR13 exact HEAD/finding | Fix TV2-R13-01 và tests recovery | Observer không nhận recoverable throw; fail-closed; partial allocations/obsolete/old cleanup an toàn; official re-review. |
| NEXT-TV3-02 | TV3 | P1 | Boundary lifecycle thống nhất với TV1 | Fix AUD-GRP-01/02 trên branch TV3 | Không external work trong lock; invalidate/close nonblocking; bounded deferred/request admission và overload/race/reentrancy tests. |
| NEXT-TV3-03 | TV3; TV1/TV2 contract | P2 | NEXT-TV3-02; coordinator recovery policy | Cleanup/cancellation ownership spec +fix AUD-GRP-03/04 | Repeated close an toàn; all resources attempted; no observer escape; phân biệt physical termination và logical cancellation bằng evidence. |
| NEXT-TV2-01 | TV2; TV1/TV3 review | P0 | Shared domain documents | REST/WS JSON v1 +sequence/read cursor/idempotency/history spec | Không còn field/cursor/envelope TBD trong scope P0; DB/API mapping duyệt trước migration. |
| NEXT-TV3-04 | TV3; reviewer khác | P1 | TV3 lifecycle fixes +baseline decision | PR6 scoped follow-up/review request +updated QA docs | Correct HEAD tests, findings xử lý, official review; không tự merge hoặc rebase chỉ vì main đổi. |
| NEXT-TV1-02 | TV1 | P0 | Baseline +OTP/JWT decision | Real Auth/session/PeopleDirectory implementation | Account A/B thật; credential scope; refresh/logout failure policy; không mở Home khi unauthenticated. |
| NEXT-TV2-02 | TV2 | P0 | Wire/API/schema approved | Real Message REST/Postgres/WS repo và transport tests | Server idempotency/order/cursor; persisted reopen; no fake substitution; official review PR9 đúng HEAD. |
| NEXT-INTEG-01 | TV1 host +TV2/TV3 adapters | P0 | Coordinator/Groups fixes; actual providers | Home mount/selection/resume/back, ChatNavigator và account lifecycle adapters | Auth→Home; scope invalidated đúng epoch; switch/logout không stale render hoặc shared-resource disposal. |
| NEXT-TV3-05 | TV3 +TV2 | P0 delivery | Membership/history policy +shared Message API | GROUP-02 role/leave/remove và history/send/WS/media revoke | Role matrix; last owner; active-only; revocation race tests bằng real DB/server. |
| NEXT-QA-01 | Cả nhóm | P0 | Real Auth/Contacts/Chat/Groups/transport | Hai account/two-device integration suite +artifacts theo SHA | Day7 demo persisted 1–1; sau đó Day11 group/role/media/expiry/reopen; instrumented results thực, không chỉ build. |
| NEXT-P1-01 | TV3 +TV1/TV2 | P1 conditional | P0 passed và nhóm nhận P1 | Translation/Dual Mode contract, sau đó implementation | Original bất biến; dịch/lang/error đúng; Normal/International/persist đúng; có E2E hoặc ghi deferred minh bạch. |

**TV3 có thể tự làm tiếp:** chuẩn bị baseline manifest/ADR và test matrix; sửa Groups lock/bounds/cleanup trong phạm vi owner trên task sau; chuẩn bị GROUP-02 role/history proposal và cập nhật tài liệu stale. Không phải đợi backend chung để sửa lifecycle thuần Java. **TV3 phải chờ TV1:** real UUID/session/credential/PeopleDirectory/privacy, host và recovery policy. **TV3 phải chờ TV2:** wire Message/WS/sequence/read cursor/history/ChatNavigator boundary. Không chờ vô hạn: đưa proposal cụ thể để owners review, không tự đổi interface/schema.

Thứ tự triển khai: baseline decisions +lifecycle fixes +wire spec có thể làm song song theo owner; chỉ tích hợp sau acceptance của từng boundary. Sau đó baseline/Auth/transport, Home adapters, Day7 real demo, GROUP-02/media và Day11 P0 suite. Translation/Dual Mode xếp sau P0. Không tự gán lịch Day1 hoặc hứa ETA chưa có bằng chứng.

## 9. Final Acceptance Checklist

Các hàng dưới là gate kiểm tra, **không cộng vào thống kê 42 plan items**.

| Gate | Status hiện tại | Evidence / điều kiện đóng |
|---|---|---|
| Build main | [x] DONE trong phạm vi debug build SHA audit | E-main assembleDebug PASS. Không thay release build hoặc module integration. |
| JVM main/feature refs | [x] DONE trong phạm vi các lượt rerun | main44 / PR6 37 / PR9 183 / PR13 35 / PR14 26, XML failures/errors/skips0; không cộng overlapping baselines. PR15 production/unit không đổi main, audit không chạy riêng PR15. |
| Backend local verify | [x] DONE trong phạm vi PR6 SHA | Maven verify25 actual, 13 PostgreSQL Testcontainers, migration/constraints/ACL/concurrency/idempotency PASS; test identity không là Auth thật. |
| Instrumentation foundation | [~] PARTIAL | Reviewer evidence API37, PR15 test-only. Audit không có connected device; cần XML/runtime theo SHA, API26–36 và rotation/lifecycle coverage được nhóm chọn. Không tính assembleAndroidTest là PASS. |
| Integrated UI lifecycle/account switch | [!] BLOCKED | Sửa Groups/Coordinator; host/providers; actual rotation/background/logout/refresh/account-switch tests. |
| Shared backend/schema deployment | [!] BLOCKED | Baseline approved/merged, one Flyway history, migration checksum/history verified, seed demo repeatable. |
| Security acceptance | [!] BLOCKED | Real OTP policy/JWT issuer/audience/expiry/refresh/logout, secure credential persistence, E.164/privacy/block, server ACL mọi read/send/WS/media; negative/revoke tests; no secret in handover artifacts. Audit này không tuyên bố comprehensive security scan PASS. |
| E2E A/B registration/login | [!] BLOCKED | Real Auth, account UUID và session evidence. |
| E2E Contacts/Friends/DIRECT | [!] BLOCKED | Search/request/accept/open direct, A/B realtime/ACK, persisted history sau reopen. |
| E2E Groups | [!] BLOCKED | Create/invite/accept, group messaging, OWNER/ADMIN/MEMBER, left/removed read/send/subscribe/download revoke, pagination và race evidence. |
| E2E media/reconnect | [!] BLOCKED | Image/file/video thật, limits/ACL/errors/retry/outbox/dedupe/reconnect, 2 thiết bị và persisted reopen. |
| P1 Translation/Dual Mode | [ ] TODO conditional | P0 gate rồi team xác nhận scope; original+translated/language/manual/errors/mode có runtime evidence, hoặc báo deferred rõ ràng. |
| Final APK | [!] BLOCKED | APK integrated/release version +SHA/hash, installable, không debug preview được gọi final delivery; hướng dẫn cấu hình không chứa secret. |
| Documentation | [~] PARTIAL | Checklist này mới; docs Groups cũ cần cập nhật snapshot, API/contracts/README/run/test/demo/handover còn thiếu. Không sửa Google Docs hoặc Word gốc trong audit. |
| Final demo/handover | [!] BLOCKED | Day13 full regression; Day14 video/log/DB evidence/presentation +owner signoff, PR status đúng và limitations minh bạch. |

### Audit completion boundary

Đã đối chiếu plan trực tiếp, 12 PR và official reviews, main/feature refs/history, Android/backend/schema/contracts/docs, rerun build/unit/backend và probes. Trong repository, deliverable của lượt này chỉ là file checklist mới; logs/snapshots/probes local ở thư mục test-artifacts ngoài source repo. Không phát hiện CI evidence để thay cho runtime còn thiếu. Không gửi message/comment/approval cho thành viên khác, không thay đổi PR hoặc Google Docs. Các next actions là đề xuất công việc sau audit, chưa được tuyên bố đã thực hiện.

Kết luận nghiệm thu: **foundation có thể bàn giao theo phạm vi hẹp, dự án chưa hoàn tất và chưa có integrated E2E verification**.

## Phase 2A evidence update — 25/11/2026

Snapshot audit và các SHA phía trên được giữ nguyên như lịch sử. Hoàng Phúc đã phê duyệt hướng D01–D10 trong yêu cầu Phase2A, giới hạn implementation ở Groups; không suy ra quyền sửa TV1/TV2, shared schema hoặc backend extraction.

- [~] **TV3 lifecycle hardening trên branch PR6:** external callback/Supplier ra khỏi state lock; bounded serial admission32; request limit16/repo và64 toàn Groups; state machine phân biệt cancellation/termination/settlement; logical close trước async cleanup có diagnostics; safe account replacement/preparation guards. Xem [report](group-01/PHASE2A_LIFECYCLE_REPORT.md).
- **Bằng chứng mới:** Android assembleDebug +testDebugUnitTest +assembleDebugAndroidTest exit0;73 JVM/9 XML PASS,0 failure/error/skips (66 Groups+7 baseline). Tăng36 case so với37 ở starting branch, không cộng reruns. Backend mvn verify25 PASS, gồm13 PostgreSQL Testcontainers; Flyway validate/apply V1 trong test DB PASS. Chi tiết commands/environment/report paths trong report.
- **AUD-GRP-01/02/04:** đã xử lý bằng code/tests trên feature branch, chờ independent review và runtime UI suite; chưa merged. **AUD-GRP-03:** đã có bounded retention/accounting/timeout và terminal evidence, không còn xóa sổ chỉ vì cancel; transport không xác nhận kết thúc vẫn bị giữ capacity/quarantine và báo deadline, không tuyên bố force termination.
- **Instrumentation:** test APK compile PASS; không có adb device online nên runtime NOT RUN. GROUP-01 chưa production-ready hoặc E2E Verified.
- **D02-TV3/D03-TV3/D04-TV3/D07-TV3/D09-TV3/D11-TV3/D13-TV3:** có thêm bằng chứng lifecycle, giữ nguyên trạng thái ngày vì real identity/host/shared backend approval/GROUP-02/history/media/E2E vẫn thiếu. Thống kê42 cell vẫn DONE0/PARTIAL20/BLOCKED13/TODO9, không đẩy cả milestone thành DONE.
- **TV2-R13-01:** vẫn TV1-owned/open dependency; Groups cleanup không còn ném recoverable failure vào observer nhưng chưa thay TV1 factory recovery policy.
- [Membership/role/history policy](group-01/PHASE2A_MEMBERSHIP_POLICY.md) phân biệt current GROUP-01 với periods/rejoin/history chưa implemented và các policy OPEN. [Backend manifest](group-01/PHASE2A_BACKEND_MANIFEST.md) ghi rõ V1 preserved, shared DB state unknown, chưa extraction.
- Next action: TV1/TV2 independent review đúng implementation HEAD; chạy Groups lifecycle instrumentation trên device online; sau đó phối hợp providers/host/contracts. Không tự merge PR6.
