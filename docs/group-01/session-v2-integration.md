# Groups account scope và Session V2

Mã Groups không import hoặc chép các class Session V2 chưa merge của TV1.
GroupsAccountScope là ranh giới vòng đời của feature, không cấp phiên, không xác minh JWT và không thay SessionRepository.
TV1 tạo scope chỉ sau khi xác thực có thẩm quyền, cung cấp user UUID, epoch, latest-snapshot access supplier và executor UI tuần tự.

## Ánh xạ snapshot nhất quán

Supplier phải đọc đúng một SessionSnapshot mới nhất cho mỗi lần kiểm tra:

```java
// Adapter thuộc composition root TV1, chỉ tích hợp khi PR #8 được duyệt/merge.
SessionSnapshot snapshot = sessions.currentSnapshot();
if (!epoch.equals(snapshot.getSessionEpoch())
        || snapshot.getState() == SessionRepository.State.SIGNED_OUT) {
    return GroupsAccountScope.Access.EXPIRED;
}
if (snapshot.getState() == SessionRepository.State.AUTHENTICATED) {
    return userId.equals(snapshot.getCurrentUserId())
            ? GroupsAccountScope.Access.ACTIVE
            : GroupsAccountScope.Access.EXPIRED;
}
return GroupsAccountScope.Access.SUSPENDED; // CHECKING / UNAVAILABLE trong cùng epoch
```

Supplier không được block hoặc gây side effect. Coordinator, state mutation UI và snapshot observer phải dùng cùng executor UI tuần tự.
Không giữ khóa nội bộ của Session implementation trong khi gọi cleanup scope hoặc callback của feature.
Epoch là danh tính scope cục bộ, không phải credential hoặc bằng chứng server đã xác thực.

## Wiring cần TV1 cung cấp

- PeopleDirectory.currentUserId() ánh xạ UUID của scope đã xác thực sang string. Dependencies kiểm tra khớp identity khi được tạo.
- PeopleDirectory.close() chỉ hủy request của adapter Groups, không đóng Contacts module/client dùng chung.
- GroupRepository.close() hủy các Call đang chờ. RemoteGroupRepository không shutdown shared OkHttp dispatcher hoặc connection pool.
- Tạo Dependencies(groupRepository, peopleDirectory, chatNavigator, scope), rồi GroupsIntegration.install(dependencies).
- Subscribe SessionRepository bằng executor UI; mỗi lần được thông báo, đọc lại snapshot mới nhất qua supplier và gọi scope.sessionChanged().
- Đăng ký subscription::close bằng scope.own để unsubscribe khi account scope kết thúc.
- Refresh cùng epoch giữ nguyên dependency object: install cùng object là no-op. Không dựng lại scope/repository chỉ vì token revision đổi.
- Khi logout/đổi tài khoản: invalidation phải có hiệu lực ngay; đóng scope cũ trước khi công bố hoặc gắn scope mới. install(null) hoặc install(newDependencies) đóng scope cũ.

CredentialProvider chỉ nối vào transport của TV1. Token không đi qua ViewModel/UI; 401 có thể đi qua cơ chế refresh/replay giới hạn của transport,
giữ nguyên Idempotency-Key của lần tạo nhóm. HTTP 403 không tự kích hoạt refresh.
Chưa có credential provider implementation hoặc auth backend thật trong thay đổi này.

## Refresh khác logout

| Trạng thái | Hành vi Groups |
|---|---|
| ACTIVE, cùng user/epoch | Cho phép thao tác và áp dụng kết quả |
| SUSPENDED, cùng epoch | Ẩn nội dung và đóng dialog nhạy cảm, không phát request mới; giữ draft, selection, retry key và callback đang chờ |
| Trở lại ACTIVE, cùng epoch | Phát các kết quả đang chờ theo executor UI; hiện lại dữ liệu của cùng scope |
| EXPIRED hoặc close | Vô hiệu hóa ngay, xóa callback đang chờ, hủy request, dọn state và đóng Activity cũ |

Callback đã được transport nhận trước cancel vẫn có thể chạy; ViewModel luôn guard bằng account scope, generation và owner của saved state.
Việc kiểm tra và mutation được tuần tự hóa với invalidation. Không chỉ kiểm epoch một lần lúc khởi tạo.
Observer Activity được unsubscribe khi destroy, kể cả callback đóng scope đang xếp hàng.

## Saved state và process death

SavedStateHandle giữ name/selection/query/screen/group/key kèm user UUID và epoch.
Recreation trong cùng scope giữ draft và key; scope khác xóa dữ liệu cũ.
Cleanup từ ViewModel cũ không được xóa saved state đã thuộc epoch mới.
ViewModel đã cleared không nhận callback muộn và không được sửa state.
Đây không phải Room outbox hoặc nhật ký thao tác bền vững: process mới có epoch mới theo Session V2 nên không hứa khôi phục thao tác chưa rõ kết quả.
Cancel request không rollback transaction đã commit trên server; sau đăng nhập lại phải tải danh sách nhóm từ server.

## Điểm tích hợp Home

MainActivity và activity_main.xml giữ nguyên phiên bản TV1 trên main 1945282; đã bỏ entry skeleton của #6 khi giải quyết merge conflict.
Không sửa HomeFragment. TV1 gắn điểm mở Groups vào home_panel_groups sau xác thực,
thay placeholder đúng một lần và gọi GroupsIntegration.open(activity) trong scope ACTIVE.
GroupsActivity là activity không exported; không tự mở Home, thay tab hoặc bỏ qua Auth.
ChatNavigator vẫn là cổng để TV2/TV1 mở pipeline hội thoại chung.

## Chưa được chứng minh bởi tests feature

Tests dùng authority fixture riêng để tạo ACTIVE/SUSPENDED/EXPIRED, không tạo đăng nhập production.
Chưa chứng minh JWT, refresh network, SMS, revoke hoặc coordinator thật hoạt động.
Backend baseline/schema/security vẫn cần review theo backend-baseline-proposal.md.
