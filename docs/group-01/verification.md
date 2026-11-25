# Báo cáo triển khai GROUP-01

## Kết quả và giới hạn

Đã triển khai mã nguồn Android Java/XML và backend Spring Boot/PostgreSQL cho tạo nhóm và lời mời.
Backend lưu conversation, OWNER/ACTIVE và PENDING invitations bằng transaction; người được mời chỉ thành MEMBER/ACTIVE sau khi chấp nhận.
Có kiểm quyền, khóa khi xử lý đồng thời, chống tạo nhóm trùng bằng Idempotency-Key, phân trang, giới hạn mỗi request và ngân sách thao tác.
Không đặt giới hạn tổng số thành viên nhóm.

Chưa đạt Definition of Done production end-to-end: main mới đã có Auth UI preview và Home foundation, nhưng chưa có auth/danh bạ thật của TV1 và hội thoại production của TV2.
Android cung cấp integration ports và Retrofit adapter thật; production không dùng fake để giả lập tìm số hoặc đăng nhập.
Khi chưa nối adapter, màn nhóm báo chưa sẵn sàng. Backend mặc định từ chối token và trả 503 cho directory chưa cấu hình.
PR cần review và nối các adapter trước khi đánh dấu issue hoàn tất; không tự merge.

## Cập nhật ngày 24/11/2026

- Đã merge main 1945282 vào nhánh GROUP-01; giữ đúng MainActivity và activity_main.xml của TV1, bỏ entry skeleton của Groups.
- Thêm GroupsAccountScope theo user UUID và epoch; adapter dùng latest-session access projection, không chép Session V2 chưa merge.
- ACTIVE được thao tác; SUSPENDED cùng epoch ẩn nội dung/đóng dialog, giữ draft/retry và trì hoãn callback; EXPIRED đóng scope, hủy requests và xóa dữ liệu.
- install(null)/đổi Dependencies đóng scope cũ, không còn chỉ xóa biến static.
- ViewModel guard callback bằng scope, generation và saved-state owner; callback cũ không ghi vào epoch mới hoặc ViewModel đã cleared.
- RemoteGroupRepository theo dõi/hủy Retrofit Call. PeopleDirectory adapter phải hủy requests riêng khi close, không đóng shared Contacts/client.
- Activity unsubscribe observer khi destroy; scope đóng thì dọn nội dung và finish Activity cũ.
- Thêm GroupsAccountScopeTest, GroupsIntegrationTest và tests logout/refresh/late callback/account switch ở unit và androidTest.
- Bổ sung session-v2-integration.md và backend-baseline-proposal.md để TV1/TV2 review. Chưa tách backend hoặc sửa schema/migration.

Đã chạy trong lần cập nhật này:

| Lệnh | Kết quả thực tế |
|---|---|
| backend: mvn -B verify | 25 đạt, 0 lỗi/bỏ qua; có 13 PostgreSQL integration tests thật |
| gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest | Build APK và test APK đạt; 37 unit tests đạt (30 Groups + 7 baseline/Auth/Home) |
| UI tests mới | Đã biên dịch thành test APK; chưa chạy được do emulator không boot tới trạng thái ADB online |

Lần thử emulator ban đầu bị chặn bởi ổ C còn khoảng 0,9 GB, sau đó bởi Windows commit memory.
Đã thử AVD test riêng trên ổ D, giảm RAM và giải phóng Docker/Gradle do tác vụ khởi động; AVD vẫn offline.
Không sửa hoặc xóa AVD gốc. Kết quả UI ngày 22/11 bên dưới là lịch sử, không phải kết quả của code lifecycle mới.
Không dùng build/test APK thành công để thay cho kết quả chạy UI.
Khi có thiết bị online, cần chạy gradlew.bat :app:connectedDebugAndroidTest trước khi approve phần UI lifecycle.

Chi tiết contract và phạm vi chờ duyệt: [Session V2](session-v2-integration.md), [backend baseline](backend-baseline-proposal.md).

## Mã nguồn của lần triển khai đầu

Các file chung được sửa có giới hạn:

- README.md: cách chạy, tình trạng tích hợp và khai báo AI.
- app/build.gradle.kts: Lifecycle/SavedState, RecyclerView, Retrofit/Gson và dependencies test.
- app/src/main/AndroidManifest.xml: Internet permission, activity nhóm không exported và theme riêng.
- app/src/main/java/com/nexa/chat/MainActivity.java và res/layout/activity_main.xml: nút mở Nhóm trên skeleton hiện tại; không thay hệ thống bốn tab đã thống nhất.

Các thành phần mới:

| Khu vực | File / trách nhiệm |
|---|---|
| Android integration | GroupsIntegration.java: dependency injection và cổng điều hướng chat |
| Model | GroupModels.java: wire DTOs và public profile cho chọn người |
| Data | GroupApi.java, RemoteGroupRepository.java |
| Repository | GroupRepository.java, PeopleDirectory.java |
| UI | GroupsActivity.java, GroupRowsAdapter.java, GroupsViewModel.java |
| Resources | groups_activity.xml, groups_row.xml, groups_avatar.xml, groups_colors.xml, groups_ids.xml, groups_strings.xml, groups_theme.xml |
| Android tests | GroupsViewModelTest.java, RemoteGroupRepositoryTest.java, GroupsFlowTest.java |
| Backend bootstrap | pom.xml, NexaBackend.java, application.yml, compose.yaml, .env.example, .gitignore |
| Shared conversation domain | Conversation.java, ConversationMember.java, ConversationRepository.java, MemberRepository.java |
| Group persistence | GroupInvitation.java, GroupCreationKey.java, InvitationRepository.java, CreationKeyRepository.java |
| Group business/API | GroupService.java, GroupController.java, GroupContracts.java, GroupMapper.java |
| Security/integration/errors | GroupDirectory.java, GroupConfiguration.java, GroupErrors.java, GroupProblem.java, WriteBudget.java, GroupRequestLimit.java |
| Database | V1__shared_conversations_and_group_invitations.sql |
| Backend tests | GroupServiceTest.java, GroupPostgresTest.java |
| Documentation | docs/group-01/README.md, verification.md và screenshots/ |

Chi tiết schema, endpoint, JSON mẫu và cách nối TV1/TV2 nằm trong [hợp đồng triển khai](README.md).
Không đưa hai tài liệu Word nội bộ vào GitHub.

## Kiểm thử lần đầu ngày 22/11/2026

Môi trường kiểm tra ngày 22/11/2026:

- Windows, JDK 21 cho Maven; Gradle 9.6.0 dùng JDK 25 theo cấu hình có sẵn của repo.
- Android SDK 37; emulator Medium_Phone_API_37.0, API 37.
- Docker Desktop, PostgreSQL 16.16 do Testcontainers khởi tạo riêng cho tests.

| Lệnh | Kết quả |
|---|---|
| backend: mvn -B verify | 25 tests: 12 unit + 13 PostgreSQL integration; 0 failures/errors/skipped; build JAR đạt |
| gradlew.bat :app:assembleDebug :app:testDebugUnitTest | Build APK đạt; 10 unit tests đạt (9 GROUP-01 + 1 baseline) |
| gradlew.bat :app:connectedDebugAndroidTest | 5 tests đạt (4 GROUP-01 + 1 baseline), không bỏ qua |
| git diff --check | Không có lỗi whitespace |

Backend tests kiểm tra tên rỗng, invitee trùng/tự mời/không hợp lệ, policy bị từ chối, OWNER/ACTIVE, PENDING,
accept/reject, đã xử lý, quyền truy cập, retry cùng key và key khác payload.
PostgreSQL tests kiểm tra Flyway/JPA schema, dữ liệu lưu thật, unique pending, REST auth/validation,
rollback khi insert membership lỗi, tạo nhóm đồng thời, accept đồng thời, nhiều batch hơn 100 người, phân trang,
OWNER-only invitation management, rate limit và JSON request vượt 64 KiB.
Trigger gây lỗi trong test rollback chỉ tồn tại trong Testcontainer và được dọn sau test.

Android unit tests kiểm tra validation, chọn/bỏ chọn và chống trùng, trạng thái loading/success/error,
khóa bấm lặp, giữ payload/key khi retry, giữ draft/selection, contract Retrofit và xử lý 403.
Espresso kiểm tra entry từ skeleton, chọn bạn và tìm số qua test adapters, chọn nhiều người,
Activity recreation, tạo thành công, PENDING rendering, accept/reject và retry giữ draft.

Các fixture auth/directory/UI nằm riêng trong test source sets. Tests không chứng minh TV1 đã có auth/search production,
không chứng minh messaging/realtime của TV2 và không thay thế demo hai tài khoản thật.

## Ảnh màn hình

Ảnh chụp từ ứng dụng chạy trên emulator bằng Espresso fixture, không phải ảnh mockup hoặc bằng chứng production search.

| Màn hình | Ảnh |
|---|---|
| Danh sách nhóm, empty state | [01-groups-empty.png](screenshots/01-groups-empty.png) |
| Tạo nhóm, chọn từ bạn bè và tìm số | [02-create-selected.png](screenshots/02-create-selected.png) |
| Nhóm vừa tạo, OWNER và số thành viên | [03-group-details.png](screenshots/03-group-details.png) |
| Lời mời PENDING | [04-pending-invitations.png](screenshots/04-pending-invitations.png) |

## Những điểm Hoàng Phúc cần hiểu trước khi merge

1. PENDING invitation không phải membership; mọi API chat sau này phải kiểm ACTIVE ở server.
2. Group dùng chung conversation ID với TV2. Không tạo pipeline message riêng hoặc thêm một hệ thống user/contact khác.
3. Idempotency key theo user và payload; timeout phải retry cùng key. Transaction và unique constraint phối hợp chống trùng.
4. Accept khóa invitation; membership và trạng thái lời mời commit cùng nhau. Test rollback đã chạy trên PostgreSQL.
5. Policy search/block phải do TV1 xác nhận và thực thi qua GroupDirectory. Eligible từ Android chỉ là gợi ý UI.
6. JWT phải được kiểm chữ ký, issuer, audience và thời hạn. Chưa có issuer thì từ chối; không dùng decoder trong tests làm auth production.
7. TV1 cần review schema user ID ban đầu, security config, Gradle, manifest và entry trong MainActivity; chỉ thêm migration mới sau khi đã áp dụng.
8. TV2 cần nối ChatNavigator, membership checks và thống nhất lịch sử nhóm/realtime trước khi demo nhắn tin nhóm.
9. Lời mời bổ sung hiện có API OWNER-only; UI thêm người sau khi tạo, ADMIN workflow, rời/rejoin và sự kiện realtime để task sau.
10. UI đang dùng public profile từ directory khi chọn người; danh sách membership/invitations trả định danh UUID.
    Việc hiển thị profile công khai theo ID cần nối adapter TV1 khi hoàn thiện trải nghiệm production.
11. Page-based pagination có thể đổi khi có ghi đồng thời; refresh trang đầu khi cần.
    HTTP gateway cần chống spam đọc/failed requests; write budget trong feature áp cho thao tác thành công.
12. AI đã hỗ trợ code/tests/docs; người merge chịu trách nhiệm đọc, kiểm tra, hiểu và giải thích được khi vấn đáp.
