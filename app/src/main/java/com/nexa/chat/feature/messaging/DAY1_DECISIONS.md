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
- TV2-03A: ChatNavigator là port nội bộ do MessagingRouter của account scope implement.
  App Shell sở hữu chuyển tab/System Back; delegate handleBack() và command mở chat vào Messaging.
  Constructor MessagingView(controller, controller.getRouter()) và setHostState(selected, resumed)
  là entry point PROPOSED cho TV2-03B, chưa phải Home integration contract đã duyệt.
  Router giữ route qua recreation; presentation mặc định inactive. Chưa có viewport/READ/TYPING transport.

## TV2-03A-FIX — local behavior
- Controller giữ một DIRECT operation pending/peer/account, reserve cùng gate với navigation intent.
  Mỗi caller nhận ticket/future riêng; superseded intent vẫn nhận kết quả operation, không được điều hướng.
  Cancel/manual complete future của caller chỉ tác động future đó, không hủy operation dùng chung.
  Failure giải phóng entry theo identity để retry; invalidation xóa pending state và route.
- FIX3 theo DESIGN3 đã được TV1/TV3 duyệt: dùng CompletableFuture chuẩn, một completion state.
  Host giữ một MessagingExecution; acquire lease theo user/epoch rồi inject vào Controller.
  Mỗi lease có 4 settlement workers/caller slots, 4 operation slots, một dispatch worker;
  hai mailbox hữu hạn 4. Runtime tối đa 2 lease live/retiring, 10 managed workers.
  Admission và invalidation dùng cùng gate; rejection trước thay đổi intent/draft hoặc gọi repository.
  Caller reservation giữ đến khi complete* trả về, kể cả synchronous callback đang block.
  Manual complete/cancel không giải phóng internal reservation hoặc thay đổi shared operation.
  Invalidate(expectedEpoch) đóng logic ngay; settlement cancellation bất đồng bộ, terminal UI
  tách khỏi normal observer drain. Claimed settlement có thể xong sau invalidation.
  Operation giữ đến invocation trả về và TẤT CẢ source stage terminal; history theo dõi riêng
  listMessages và listOutbox, kể cả nguồn thứ hai throw khi nguồn thứ nhất còn pending.
  Không cancel source CF như bằng chứng transport termination. Runtime close không join UI thread;
  lease chỉ retire sau callers/operations/mailboxes/workers thực sự quiescent.
  Hết hai lease chỉ reject Messaging; host retry phải kiểm tra current epoch, tránh busy loop.
  Production timeout/cancellation handle và App Shell integration thuộc giai đoạn sau.
- Terminal observer executor phải handoff không block (View dùng Handler.post). Reject/throw đóng
  subscription; khi main looper không hoạt động, host phải remove/dispose surface cũ. Chưa có host fallback.
  Local invalidation không chứng minh transport cancellation hoặc server cleanup.
- Restore/reconcile EditText đi qua DraftInputOrigin try/finally; programmatic change không edit draft,
  không tạo genuine typing eligibility. Không có TYPING transport.
- Không đổi semantics title cùng conversation hoặc explicit refresh. Backlog TV2-05/TV2-06:
  TextWatcher thật, ACK/render/draft revision, history coalescing/stale callbacks, refresh storm,
  main-thread logout khi continuation block, GONE cleanup và host executor failure trên Android.

## TV2-03A-FIX4 — targeted R3 fixes
- R3-01: snapshot/validate Message trong account gate trước admission; invalid identifier vẫn
  throw IllegalArgumentException đồng bộ, không tiêu thụ reservation hoặc thay đổi draft/revision.
  Các đường DIRECT/request/retry không có bước domain validation có thể throw trước khi ticket
  được đăng ký sau admission; repository/dispatch failure đi qua settlement của ticket đã sở hữu.
- R3-02: normal observer lỗi dừng normal delivery (isOpen=false), giữ terminal-only subscription
  nếu có onInvalidated. Lỗi normal không tương đương detach, kể cả trước invalidation.
  Explicit close và terminal claim serialize cùng gate: close trước claim hủy terminal;
  close sau claim không thu hồi callback đã claim. Duplicate invalidate không post lần hai.
  Terminal executor rejection đóng subscription; host vẫn phải dispose surface nếu UI không nhận.
- R3-03: worker startup failure đóng lease và propagate lỗi; failed lease không được expose cho
  Controller. Runtime vẫn giữ lease đến khi thread đã start thực sự exit và dispatcher terminated.
  Không giả định năm worker đã start; NEW thread có start thất bại không bị tính như worker sống.
  Không join main thread. ThreadFactory package-local chỉ phục vụ fault injection trong test.

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
