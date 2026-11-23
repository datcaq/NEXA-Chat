# TV2 Ngày 1–2 — PROPOSED
Đã có MASTER SPEC v2.0 và Tài liệu số 02 tại NEXA-Docs. Không sao chép tài liệu gốc vào Git.
Theo chỉ dẫn nhóm hiện tại: kế hoạch 14 ngày; Ngày 1 Chat/Realtime Contract, Ngày 2 DTO, FakeRepository, ChatNavigator, UI nền.
Hai DOCX cũ vẫn mô tả lịch 21 ngày; chỉ dẫn hiện tại được dùng cho phạm vi này.

## Đã chuẩn bị nội bộ
- Domain Conversation có metadata; Message có text, attachment/reply và timestamp nullable.
- Constructor cũ giữ tương thích; timestamp null nghĩa chưa có bằng chứng, không bịa giờ server.
- Message + ServerMessage + ServerAcknowledgement hợp thành tin xác nhận:
  server ID/sequence lấy từ ACK, status lấy receiptStatus; local sending state ở OutboxEntry.
- MessageType TEXT/IMAGE/VIDEO/FILE/VOICE; chưa triển khai upload hay mapping JSON.
- Giữ nguyên state machine và test cũ. ACK muộn reconcile, receipt không lùi.
- FakeConversationRepository trong RAM, một instance/user; có server store riêng, mất ACK,
  replay cùng payload, lỗi có mã, read cursor tăng, lịch sử phân trang và outbox projection.
- OFFLINE giữ PENDING; TIMEOUT giữ FAILED và báo ChatFailure. Chỉ hai mã này retryable.
  401 cần xác thực lại; 403/409 cần giải quyết nguyên nhân, không tự retry.
- Pagination PROPOSED: items tăng dần; null cursor bắt đầu mới nhất; exclusive sequence,
  nextCursor null là hết. Cursor phải thuộc hội thoại. Chưa phải cursor REST.
- ChatPanel có danh sách, loading/empty/error và khung chat/input; host cung cấp nhãn,
  draft, callback gửi, lifecycle. Không tích hợp Home, không bảo đảm rotation/persistence.
- ChatNavigator vẫn do TV2 quản lý, TV1 cung cấp implementation.

## Chờ Quốc Đạt và Hoàng Phúc phê duyệt
- JSON text/body, attachmentId(s), replyToId/replyToMessageId; DTO chung và nullability.
- Session boundary, repository lifecycle/logout, callback thread/cancellation và adapter Home.
- Sequence/cursor REST, read cursor, receipt GROUP, membership/history/block policies.
- REST /api/v1 và WebSocket envelope/event ID/reconnect, ACK identity và error response.
- Thời hạn edit/recall, media limits, quyền tải và điểm tích hợp translation.
Không có contract nào được tuyên bố đã được cả nhóm duyệt.

## Giới hạn
Không backend/REST/WS/Room; fake không chứng minh bảo mật server, multi-device hoặc process death.
Không tự động retry/sync. Không triển khai Groups/Translation/Session.
