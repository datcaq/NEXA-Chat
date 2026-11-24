# Đề xuất tách backend baseline để nhóm duyệt

Ngày 24/11/2026. Đây là danh sách phạm vi và thứ tự PR đề xuất, chưa phải xác nhận nhóm đã duyệt.
Chưa tách code backend, chưa đổi schema/migration và chưa mở backend thứ hai.
PR #6 giữ Draft. Hai tài liệu Word không được sửa hoặc đưa vào Git.

## Trạng thái đã đối chiếu

- main 1945282 đã có Auth UI (#7) và Home UI Foundation (#10); Auth UI vẫn là preview.
- Session V2 ở PR #8 (a5ede4a), chưa nằm trong main tại thời điểm đối chiếu.
- Messaging ở PR #9 đã có account-scoped controller (ccc9ef1); REST/WebSocket và JSON contract chưa được coi là đã duyệt.
- PR #6 có một ứng dụng backend duy nhất và một V1 migration đang gộp schema dùng chung với bảng Groups.

## PR thứ nhất là môi trường backend nền

Nhánh dự kiến feature/backend-baseline, tạo từ main mới nhất sau khi nhóm duyệt danh sách này.
Chỉ đưa bootstrap, môi trường và kiểm chứng hạ tầng vào PR đầu:

| File dự kiến | Nguồn / trách nhiệm |
|---|---|
| backend/pom.xml | Tách từ #6; Spring Boot 3.5.16, Java 21, JPA, Validation, PostgreSQL, Flyway và test dependencies; TV1 duyệt dependency security |
| backend/src/main/java/com/nexa/chat/backend/NexaBackend.java | Bootstrap dùng chung, không khởi tạo Groups/Auth/Chat giả |
| backend/src/main/resources/application.yml | Cấu hình datasource bằng env, Hibernate validate, cấu hình server; không hardcode secret |
| backend/compose.yaml | Một PostgreSQL dev, volume riêng, bind localhost, password từ env |
| backend/.env.example | Chỉ tên biến môi trường |
| backend/.gitignore | Loại target và .env |
| backend/README.md | File mới: chạy môi trường, yêu cầu JDK/Docker, lệnh test và giới hạn baseline |
| backend/src/test/java/com/nexa/chat/backend/foundation/BackendBaselineTest.java | File mới: context bootstrap và datasource thật trên Testcontainers |
| backend/src/test/java/com/nexa/chat/backend/foundation/DatabaseMigrationSmokeTest.java | File mới: Flyway áp dụng/validate/re-run không tạo dữ liệu trùng |
| backend/src/test/resources/db/baseline/V1__baseline_probe.sql | Chỉ migration thử nghiệm trong test resources với location riêng; không đưa bảng nghiệp vụ vào production |

Production migration chưa có trong PR nền nếu schema chung chưa được duyệt.
Không copy GroupPostgresTest thành test nền vì nó phụ thuộc nghiệp vụ Groups.
Không đưa GroupService, GroupController, GroupDirectory, invitation, idempotency hoặc write budget vào baseline.
Security mặc định phải từ chối truy cập chưa xác thực; không dùng permitAll để làm demo chạy.
Cấu hình cụ thể do TV1 review, không mặc định yêu cầu TV1 xây OIDC chỉ vì #6 hiện dùng JwtDecoder theo issuer.

## PR thứ hai là contract và schema dùng chung

Cả ba duyệt bảng và JSON trước khi tạo production migration:

| Phần | Chủ trì | Những điểm còn cần xác nhận |
|---|---|---|
| users | TV1 | UUID identity, hồ sơ, auth/session; không tạo user từ API nhóm |
| conversations | TV2, TV3 review | DIRECT/GROUP dùng chung ID; canonical DIRECT pair, sequence và metadata |
| conversation_members | TV3 và TV2 | ACTIVE/LEFT/REMOVED; OWNER/ADMIN/MEMBER; read cursor, lịch sử khi tham gia/rời |
| security và lỗi API | TV1 | JWT sub UUID, issuer/audience, lifetime, refresh/revoke; ACL; format lỗi chung |
| migration registry | Cả ba | Ai cấp version, thứ tự áp dụng, môi trường đã chạy, checksum và test nâng cấp |

V1 hiện tại dùng last_read_message_id. TV2 đang hướng tới serverSequence/read cursor theo conversation.
Không tự đổi sang lastReadSequence, thêm joinedSequence hoặc khóa schema chỉ dựa trên domain DTO chưa có JSON contract.
Phương án sequence cần xác nhận monotonic cursor theo từng member, bounds hợp lệ và membership/history policy.
Pending invitee không có membership ACTIVE và không được đọc/gửi tin hoặc subscribe WebSocket.

## PR Groups sau nền chung

Giữ riêng GroupService/Controller/Contracts/Mapper, GroupInvitation và repositories, group_creation_keys, group_write_budgets,
GroupDirectory port, ACL và tests nhóm. Nhóm duyệt quyền mời OWNER-only trong GROUP-01 và chính sách lịch sử.
ADMIN/leave/rejoin/moderation thuộc GROUP-02.

GroupConfiguration hiện đang chứa JwtDecoder/SecurityFilterChain toàn ứng dụng.
Sau khi TV1 duyệt, chuyển các bean đó sang package security chung do TV1 quản lý;
bean GroupDirectory mặc định không sẵn sàng vẫn thuộc Groups. Không tạo hai filter chain chồng lấn hoặc hai auth hệ thống.
Contract GroupDirectory phải kiểm existence/privacy/block ở server; client eligibility không phải quyền.

## Quy tắc giữ migration đã áp dụng

Trước khi tách, cả nhóm phải xác nhận V1 của #6 đã được áp dụng ở môi trường chung hay mới chạy trong Testcontainers:

- Nếu đã áp dụng: giữ nguyên tên, nội dung và checksum của V1; chỉ thêm migration tiến về trước đã được duyệt.
- Nếu chưa áp dụng chung: có thể đề xuất lịch migration riêng cho shared schema và Groups, nhưng chỉ triển khai sau khi cả ba đồng ý.
- Migration test-only trong Testcontainer không phải bằng chứng DB dùng chung đã áp dụng.
- Không đánh lại version, xóa schema, repair Flyway hoặc sửa migration đã áp dụng để làm test xanh.

## Thứ tự thực hiện đề xuất

1. Nhóm duyệt phạm vi file PR nền, owner security và tình trạng V1 ở DB chung.
2. TV3 tạo PR nền độc lập từ main; chạy bootstrap/PostgreSQL/Flyway tests thật và gửi TV1/TV2 review.
3. Cả ba khóa contract/schema và registry version; mở PR shared schema/security theo ownership.
4. Auth, Messaging và Groups tiếp tục trên cùng backend; cập nhật #6 dựa trên baseline đã merge.
5. TV1 tích hợp coordinator/Home; TV3 cung cấp scope lifecycle và điểm mở Groups; TV2 nối ChatNavigator.
6. Chỉ nghiệm thu GROUP-01 khi Android dùng auth/directory thật và backend lưu/đồng bộ đúng.

Không đề nghị merge nguyên #6 để lấy baseline. Danh sách trên cần được nhóm duyệt trước khi tách code.
