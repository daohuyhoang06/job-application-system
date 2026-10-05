Nhóm 1: Tuấn

AUTH

POST    /api/auth/applicants/register : Đăng ký tài khoản ứng viên.

POST    /api/auth/applicants/login**	**: Đăng nhập tài khoản ứng viên

POST    /api/auth/employers/register : Đăng ký tài khoản nhà tuyển dụng/công ty

POST    /api/auth/employers/login**	**: Đăng nhập tài khoản nhà tuyển dụng/công ty

Nhóm 2: Hoàng

EMPLOYER JOBS

POST    /api/employer/jobs: Nhà tuyển dụng đăng một công việc mới.

GET     /api/employer/jobs: Xem danh sách các công việc do nhà tuyển dụng hiện tại đăng.

GET     /api/employer/jobs/{jobId}: Xem chi tiết một công việc do nhà tuyển dụng hiện tại đăng.

PATCH   /api/employer/jobs/{jobId}: Cập nhật thông tin của một công việc đã đăng.

DELETE  /api/employer/jobs/{jobId}: Đóng công việc (`CLOSED`) theo cơ chế soft delete; không xóa Job hoặc Application.

Nhóm 3: Đức

PUBLIC JOBS

GET     /api/jobs**	** : Lấy danh sách công việc; hỗ trợ tìm kiếm

GET     /api/jobs/{jobId}: Xem thông tin chi tiết của một công việc.

APPLICANT APPLICATIONS

POST    /api/jobs/{jobId}/applications**	**: Ứng tuyển vào một công việc.

GET     /api/applications/me: Xem danh sách các đơn ứng tuyển của ứng viên đang đăng nhập.

GET     /api/applications/{applicationId}: Xem chi tiết một đơn ứng tuyển của bản thân.

DELETE  /api/applications/{applicationId}: Rút/xóa một đơn ứng tuyển của bản thân.

Nhóm 4: Bích

EMPLOYER APPLICATIONS

GET     /api/employer/jobs/{jobId}/applications: Xem danh sách ứng tuyển vào một công việc.

GET     /api/employer/applications/{applicationId}: Xem chi tiết một đơn ứng tuyển

PATCH   /api/employer/applications/{applicationId}/status: Cập nhật trạng thái đơn ứng tuyển.

APPLICANT PROFILE

GET     /api/applicants/me**	**: Xem thông tin cá nhân của ứng viên đang đăng nhập.

PATCH   /api/applicants/me**	**: Cập nhật thông tin cá nhân của ứng viên đang đăng nhập.

( Thêm trường CV)

**
