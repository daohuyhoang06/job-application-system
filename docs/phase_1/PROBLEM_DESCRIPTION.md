# Mô tả bài toán

## 1. Bối cảnh

Hiện nay, nhu cầu tìm kiếm việc làm và tuyển dụng trực tuyến ngày càng phổ biến. Tuy nhiên, nếu thông tin tuyển dụng và hồ sơ ứng tuyển được quản lý qua nhiều kênh rời rạc như email, mạng xã hội hoặc bảng tính, ứng viên sẽ khó theo dõi quá trình ứng tuyển, còn nhà tuyển dụng mất nhiều thời gian tổng hợp và xử lý hồ sơ.

Đề tài xây dựng **Job Application System – Hệ thống tìm kiếm và ứng tuyển việc làm**, cung cấp một nền tảng tập trung để:

- Nhà tuyển dụng đăng và quản lý tin tuyển dụng.

- Ứng viên tìm kiếm công việc phù hợp và nộp hồ sơ.

- Hai bên theo dõi quá trình xử lý đơn ứng tuyển.

- Bảo đảm người dùng chỉ được truy cập dữ liệu đúng với vai trò và quyền sở hữu của mình.

## 2. Lý do chọn đề tài

Nhóm lựa chọn đề tài này vì các lý do sau:

- **Quy trình nghiệp vụ rõ ràng:** Bài toán có đầy đủ các bước từ đăng tin, tìm kiếm, ứng tuyển đến xét duyệt hồ sơ.

- **Có nhiều nhóm người dùng:** Hệ thống cần phân biệt quyền giữa ứng viên và nhà tuyển dụng, phù hợp để áp dụng xác thực và phân quyền.

- **Có khả năng mở rộng:** Hệ thống có thể tiếp tục phát triển thêm các chức năng như gợi ý công việc, phỏng vấn trực tuyến, gửi thông báo, quản lý CV và phân tích dữ liệu tuyển dụng.

- **Phù hợp với mục tiêu học tập:** Đề tài đáp ứng yêu cầu xây dựng REST API, xác thực người dùng, quản lý dữ liệu bằng ORM, tài liệu hóa API, kiểm thử và đóng gói bằng Docker.

## 3. Người dùng của hệ thống

### 3.1. Khách truy cập

Khách truy cập là người chưa đăng nhập vào hệ thống. Nhóm người dùng này có thể:

- Xem danh sách các công việc đang tuyển.

- Tìm kiếm và lọc công việc theo từ khóa, địa điểm, lĩnh vực, cấp bậc và mức lương.

- Xem thông tin chi tiết của một công việc.

- Đăng ký tài khoản ứng viên hoặc nhà tuyển dụng.

### 3.2. Ứng viên

Ứng viên là người có nhu cầu tìm kiếm và ứng tuyển việc làm. Các chức năng chính gồm:

- Đăng ký và đăng nhập.

- Xem, cập nhật thông tin cá nhân.

- Tìm kiếm và xem chi tiết công việc.

- Nộp hồ sơ ứng tuyển kèm CV và thư giới thiệu.

- Xem danh sách và trạng thái các đơn đã nộp.

- Xem chi tiết hoặc rút đơn ứng tuyển.

Ứng viên chỉ được xem và thao tác trên hồ sơ, đơn ứng tuyển thuộc về chính mình.

### 3.3. Nhà tuyển dụng

Nhà tuyển dụng là cá nhân đại diện cho một công ty, có nhu cầu đăng tin và tìm kiếm ứng viên. Các chức năng chính gồm:

- Đăng ký tài khoản cùng thông tin công ty và đăng nhập.

- Đăng tin tuyển dụng.

- Xem, cập nhật, đóng hoặc xóa mềm tin tuyển dụng của mình.

- Xem danh sách ứng viên đã ứng tuyển vào từng công việc.

- Xem chi tiết hồ sơ ứng tuyển.

- Cập nhật trạng thái xử lý đơn ứng tuyển.

Nhà tuyển dụng chỉ được quản lý công việc và đơn ứng tuyển thuộc các công việc do mình đăng.

## 4. Các đối tượng chính

| **Đối tượng** | **Mô tả** |
| --- | --- |
| **Applicant – Ứng viên** | Lưu tài khoản và hồ sơ cá nhân của người tìm việc, gồm email, mật khẩu mã hóa, họ tên, số điện thoại và địa điểm. |
| **Company – Công ty** | Lưu thông tin đơn vị tuyển dụng, gồm tên và phần giới thiệu công ty. |
| **Employer – Nhà tuyển dụng** | Đại diện cho một công ty và chịu trách nhiệm đăng tin, quản lý công việc và xử lý hồ sơ ứng tuyển. |
| **Job – Công việc** | Tin tuyển dụng do nhà tuyển dụng tạo, gồm tiêu đề, mô tả, lĩnh vực, địa điểm, cấp bậc, mức lương và trạng thái tuyển dụng. |
| **Application – Đơn ứng tuyển** | Thể hiện việc một ứng viên ứng tuyển vào một công việc, gồm CV, thư giới thiệu, thời gian nộp và trạng thái xử lý. |

Quan hệ giữa các đối tượng:

- Một công ty có thể có nhiều nhà tuyển dụng.

- Một nhà tuyển dụng có thể đăng nhiều công việc.

- Một công việc có thể nhận nhiều đơn ứng tuyển.

- Một ứng viên có thể nộp nhiều đơn cho các công việc khác nhau.

- Một ứng viên không được ứng tuyển nhiều lần vào cùng một công việc.

## 5. Phạm vi của đề tài

## Trong phạm vi

Hệ thống tập trung vào các nghiệp vụ tuyển dụng cơ bản:

- Đăng ký, đăng nhập cho ứng viên và nhà tuyển dụng.

- Xác thực bằng JWT và phân quyền theo vai trò.

- Quản lý hồ sơ cá nhân của ứng viên.

- Quản lý thông tin công ty khi nhà tuyển dụng đăng ký.

- Đăng, xem, cập nhật và đóng tin tuyển dụng.

- Tìm kiếm, lọc và phân trang danh sách công việc.

- Nộp và rút đơn ứng tuyển.

- Theo dõi trạng thái đơn ứng tuyển.

- Nhà tuyển dụng xem hồ sơ và cập nhật kết quả xử lý.

- Kiểm tra quyền sở hữu dữ liệu.

- Cung cấp tài liệu API bằng OpenAPI/Swagger.

- Đóng gói, triển khai backend và cơ sở dữ liệu bằng Docker.

## Ngoài phạm vi

Phiên bản hiện tại chưa tập trung triển khai:

- Giao diện website hoặc ứng dụng di động.

- Vai trò quản trị viên và trang quản trị hệ thống.

- Gửi email, SMS hoặc thông báo thời gian thực.

- Nhắn tin trực tiếp giữa ứng viên và nhà tuyển dụng.

- Đặt lịch và tổ chức phỏng vấn trực tuyến.

- Gợi ý công việc bằng trí tuệ nhân tạo.

- Đánh giá công ty, báo cáo tin tuyển dụng hoặc chống gian lận.

- Phân tích CV tự động hoặc xếp hạng ứng viên.
