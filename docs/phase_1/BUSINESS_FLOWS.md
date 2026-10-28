# Các luồng nghiệp vụ cơ bản

## 1. Luồng đăng ký và đăng nhập

### 1.1. Ứng viên đăng ký tài khoản

**Tác nhân:** Ứng viên.

**Mục đích:** Tạo tài khoản để có thể sử dụng các chức năng dành cho ứng viên.

**Luồng chính:**

- Ứng viên cung cấp email, mật khẩu và họ tên.

- Hệ thống kiểm tra tính hợp lệ của thông tin.

- Hệ thống kiểm tra email đã được sử dụng cho tài khoản ứng viên hay chưa.

- Nếu thông tin hợp lệ, mật khẩu được mã hóa bằng BCrypt.

- Hệ thống tạo tài khoản ứng viên.

- Ứng viên có thể sử dụng tài khoản vừa tạo để đăng nhập.

**Ngoại lệ:**

- Thông tin thiếu hoặc không đúng định dạng thì hệ thống từ chối đăng ký.

- Email đã tồn tại thì hệ thống không tạo tài khoản mới.

**API liên quan:** POST /api/auth/applicants/register

### 1.2. Nhà tuyển dụng đăng ký tài khoản

**Tác nhân:** Nhà tuyển dụng.

**Mục đích:** Tạo tài khoản nhà tuyển dụng và thông tin công ty để đăng tin tuyển dụng.

**Luồng chính:**

- Nhà tuyển dụng nhập thông tin cá nhân và thông tin công ty.

- Hệ thống kiểm tra dữ liệu được cung cấp.

- Hệ thống kiểm tra email nhà tuyển dụng đã tồn tại hay chưa.

- Nếu hợp lệ, hệ thống tạo thông tin công ty.

- Hệ thống tạo tài khoản nhà tuyển dụng và liên kết tài khoản với công ty vừa tạo.

- Việc tạo công ty và tài khoản nhà tuyển dụng được thực hiện trong cùng một giao dịch để đảm bảo tính nhất quán dữ liệu.

**Ngoại lệ:**

- Dữ liệu không hợp lệ thì việc đăng ký bị từ chối.

- Email đã được sử dụng thì không tạo tài khoản.

- Nếu quá trình tạo công ty hoặc nhà tuyển dụng thất bại, toàn bộ giao dịch được hoàn tác.

**API liên quan:** POST /api/auth/employers/register

### 1.3. Người dùng đăng nhập

**Tác nhân:** Ứng viên hoặc Nhà tuyển dụng.

**Mục đích:** Xác thực người dùng để sử dụng các chức năng yêu cầu đăng nhập.

**Luồng chính:**

- Người dùng nhập email và mật khẩu.

- Người dùng lựa chọn đúng loại tài khoản tương ứng.

- Hệ thống tìm tài khoản theo email.

- Hệ thống kiểm tra mật khẩu nhập vào với mật khẩu đã mã hóa.

- Nếu thông tin chính xác, hệ thống tạo JWT chứa định danh người dùng và vai trò.

- JWT được sử dụng cho các request cần xác thực tiếp theo.

**Ngoại lệ:**

- Email hoặc mật khẩu không chính xác thì đăng nhập thất bại.

- Người dùng đăng nhập nhầm loại tài khoản thì hệ thống không xác thực thành công.

**API liên quan:**

POST /api/auth/applicants/login

POST /api/auth/employers/login

## 2. Luồng quản lý hồ sơ ứng viên

### 2.1. Ứng viên xem hồ sơ cá nhân

**Tác nhân:** Ứng viên đã đăng nhập.

**Luồng chính:**

- Ứng viên gửi yêu cầu xem hồ sơ.

- Hệ thống xác thực JWT và kiểm tra vai trò APPLICANT.

- Hệ thống lấy applicantId từ thông tin xác thực.

- Hệ thống truy vấn hồ sơ tương ứng.

- Thông tin hồ sơ được trả về cho ứng viên.

Mật khẩu và các thông tin bảo mật không được trả về.

**Ngoại lệ:**

- Token không hợp lệ hoặc hết hạn thì người dùng phải đăng nhập lại.

- Tài khoản không có vai trò Applicant thì không được phép truy cập.

- Không tìm thấy hồ sơ tương ứng thì hệ thống báo lỗi.

**API liên quan:** GET /api/applicants/me

### 2.2. Ứng viên cập nhật hồ sơ

**Tác nhân:** Ứng viên đã đăng nhập.

**Luồng chính:**

- Ứng viên gửi các thông tin muốn thay đổi.

- Hệ thống xác thực tài khoản.

- Hệ thống kiểm tra dữ liệu mới.

- Các trường được phép như fullName, phone, location được cập nhật.

- Những trường không được gửi lên được giữ nguyên.

- Hệ thống trả lại hồ sơ sau khi cập nhật.

Ứng viên không được tự thay đổi applicantId, email hoặc mật khẩu thông qua chức năng này.

**API liên quan:** PATCH /api/applicants/me

## 3. Luồng tìm kiếm và xem công việc

### 3.1. Tìm kiếm công việc

**Tác nhân:** Người dùng bất kỳ.

**Mục đích:** Tìm các công việc đang tuyển dụng phù hợp với nhu cầu.

**Luồng chính:**

- Người dùng truy cập danh sách công việc.

- Người dùng có thể nhập từ khóa hoặc các điều kiện lọc như:

  - địa điểm;

  - danh mục;

  - cấp bậc;

  - mức lương tối thiểu.

- Hệ thống tìm các công việc phù hợp.

- Chỉ các công việc có trạng thái OPEN được hiển thị.

- Kết quả được phân trang và trả về cho người dùng.

Người dùng không bắt buộc phải đăng nhập để sử dụng chức năng này.

**API liên quan:** GET /api/jobs

### 3.2. Xem chi tiết công việc

**Tác nhân:** Người dùng bất kỳ.

**Luồng chính:**

- Người dùng chọn một công việc từ danh sách.

- Hệ thống tìm công việc theo jobId.

- Nếu công việc đang mở, hệ thống trả về:

  - thông tin công việc;

  - mức lương;

  - địa điểm;

  - cấp bậc;

  - mô tả;

  - thông tin công ty.

- Người dùng có thể sử dụng thông tin này để quyết định có ứng tuyển hay không.

Công việc không tồn tại hoặc đã đóng sẽ không được hiển thị qua luồng public.

**API liên quan:** GET /api/jobs/{jobId}

## 4. Luồng nhà tuyển dụng quản lý công việc

### 4.1. Đăng công việc mới

**Tác nhân:** Nhà tuyển dụng đã đăng nhập.

**Luồng chính:**

- Nhà tuyển dụng nhập thông tin tuyển dụng.

- Hệ thống xác thực JWT và kiểm tra vai trò EMPLOYER.

- Hệ thống lấy employerId từ tài khoản hiện tại.

- Hệ thống kiểm tra dữ liệu công việc.

- Nếu dữ liệu hợp lệ, một công việc mới được tạo với trạng thái mặc định OPEN.

- Công việc được liên kết với nhà tuyển dụng đang đăng nhập.

Các thông tin như mức lương phải hợp lệ và salaryMax không được nhỏ hơn salaryMin.

**API liên quan:** POST /api/employer/jobs

### 4.2. Xem các công việc đã đăng

**Tác nhân:** Nhà tuyển dụng đã đăng nhập.

**Luồng chính:**

- Nhà tuyển dụng yêu cầu xem danh sách công việc của mình.

- Hệ thống xác định nhà tuyển dụng từ JWT.

- Hệ thống chỉ lấy các công việc thuộc nhà tuyển dụng đó.

- Danh sách được phân trang và trả về.

Nhà tuyển dụng không được xem danh sách quản trị công việc của tài khoản khác.

**API liên quan:** GET /api/employer/jobs

### 4.3. Xem và cập nhật công việc

**Tác nhân:** Nhà tuyển dụng đã đăng nhập.

**Luồng chính:**

- Nhà tuyển dụng chọn một công việc đã đăng.

- Hệ thống kiểm tra công việc tồn tại và thuộc tài khoản hiện tại.

- Nhà tuyển dụng có thể xem hoặc sửa thông tin công việc.

- Hệ thống kiểm tra dữ liệu cập nhật.

- Nếu hợp lệ, dữ liệu mới được lưu lại.

Nhà tuyển dụng không được thay đổi chủ sở hữu của công việc.

**API liên quan:**

GET   /api/employer/jobs/{jobId}

PATCH /api/employer/jobs/{jobId}

### 4.4. Đóng công việc

**Tác nhân:** Nhà tuyển dụng đã đăng nhập.

**Luồng chính:**

- Nhà tuyển dụng yêu cầu đóng một công việc.

- Hệ thống kiểm tra công việc thuộc nhà tuyển dụng hiện tại.

- Thay vì xóa bản ghi khỏi database, hệ thống chuyển trạng thái công việc sang CLOSED.

- Công việc không còn xuất hiện trong danh sách công việc public.

- Ứng viên không thể gửi thêm đơn ứng tuyển.

- Các đơn ứng tuyển đã tồn tại vẫn được giữ lại để nhà tuyển dụng tiếp tục xử lý.

**API liên quan:** DELETE /api/employer/jobs/{jobId}

## 5. Luồng ứng viên ứng tuyển và quản lý đơn

### 5.1. Nộp đơn ứng tuyển

**Tác nhân:** Ứng viên đã đăng nhập.

**Luồng chính:**

- Ứng viên tìm và xem một công việc.

- Ứng viên lựa chọn chức năng ứng tuyển.

- Hệ thống xác thực tài khoản và kiểm tra vai trò APPLICANT.

- Hệ thống kiểm tra công việc:

  - tồn tại;

  - đang ở trạng thái OPEN.

- Hệ thống kiểm tra ứng viên chưa từng nộp đơn cho công việc này.

- Ứng viên cung cấp CV và có thể cung cấp thư giới thiệu.

- Hệ thống tạo đơn ứng tuyển với trạng thái ban đầu PENDING.

**Ngoại lệ:**

- Công việc đã đóng thì không thể ứng tuyển.

- Thiếu CV thì không tạo đơn.

- Nếu ứng viên đã ứng tuyển công việc này thì không tạo đơn thứ hai.

**API liên quan:** POST /api/jobs/{jobId}/applications

### 5.2. Xem các đơn đã nộp

**Tác nhân:** Ứng viên đã đăng nhập.

**Luồng chính:**

- Ứng viên mở danh sách đơn ứng tuyển.

- Hệ thống xác định ứng viên từ JWT.

- Hệ thống lấy các đơn thuộc ứng viên hiện tại.

- Danh sách được sắp xếp theo thời gian nộp mới nhất.

- Ứng viên có thể chọn một đơn để xem chi tiết.

Ứng viên chỉ được xem đơn của chính mình.

**API liên quan:**

GET /api/applications/me

GET /api/applications/{applicationId}

### 5.3. Rút đơn ứng tuyển

**Tác nhân:** Ứng viên đã đăng nhập.

**Luồng chính:**

- Ứng viên chọn đơn muốn rút.

- Hệ thống kiểm tra đơn thuộc ứng viên hiện tại.

- Hệ thống xóa đơn ứng tuyển.

- Công việc và tài khoản ứng viên không bị ảnh hưởng.

**API liên quan:** DELETE /api/applications/{applicationId}

## 6. Luồng nhà tuyển dụng xử lý đơn ứng tuyển

### 6.1. Xem danh sách ứng viên của một công việc

**Tác nhân:** Nhà tuyển dụng đã đăng nhập.

**Luồng chính:**

- Nhà tuyển dụng chọn một công việc do mình đăng.

- Hệ thống kiểm tra quyền sở hữu công việc.

- Hệ thống lấy các đơn ứng tuyển liên quan.

- Nhà tuyển dụng có thể xem:

  - thông tin ứng viên;

  - CV;

  - thư giới thiệu;

  - trạng thái đơn.

- Nhà tuyển dụng chọn một đơn để xem chi tiết nếu cần.

Nhà tuyển dụng không được xem application của công việc thuộc tài khoản khác.

**API liên quan:** GET /api/employer/jobs/{jobId}/applications

### 6.2. Xem chi tiết đơn ứng tuyển

**Tác nhân:** Nhà tuyển dụng đã đăng nhập.

**Luồng chính:**

- Nhà tuyển dụng chọn một đơn ứng tuyển.

- Hệ thống xác định công việc mà đơn thuộc về.

- Hệ thống kiểm tra công việc đó có thuộc nhà tuyển dụng hiện tại hay không.

- Nếu đúng, hệ thống trả thông tin chi tiết của đơn.

**API liên quan:** GET /api/employer/applications/{applicationId}

### 6.3. Cập nhật trạng thái đơn ứng tuyển

**Tác nhân:** Nhà tuyển dụng đã đăng nhập.

**Luồng chính:**

- Nhà tuyển dụng xem một đơn ứng tuyển.

- Nhà tuyển dụng chọn trạng thái mới.

- Hệ thống kiểm tra quyền xử lý đơn.

- Hệ thống kiểm tra trạng thái mới thuộc tập giá trị hợp lệ:

  - PENDING;

  - REVIEWING;

  - INTERVIEW;

  - OFFERED;

  - REJECTED.

- Hệ thống cập nhật trạng thái.

- Trạng thái mới được hiển thị cho cả nhà tuyển dụng và ứng viên.
