# Kế hoạch hoàn thiện Employer Jobs Backend

## 1. Mục tiêu

Hoàn thiện nhóm API để nhà tuyển dụng quản lý các công việc do chính mình đăng:

- Tạo công việc mới với trạng thái mặc định `OPEN`.
- Xem danh sách công việc của tài khoản đang đăng nhập, có phân trang.
- Xem chi tiết, cập nhật và đóng công việc thuộc quyền sở hữu.
- Lấy `employerId` từ JWT, không tin tưởng định danh do client gửi lên.
- Chặn Employer A đọc hoặc thay đổi job của Employer B.
- Có validation, lỗi chuẩn hóa, Swagger và kiểm thử cho các luồng chính.

## 2. Phạm vi

### Trong phạm vi

```text
POST   /api/employer/jobs
GET    /api/employer/jobs
GET    /api/employer/jobs/{jobId}
PATCH  /api/employer/jobs/{jobId}
DELETE /api/employer/jobs/{jobId}
```

Các API đều yêu cầu JWT có role `EMPLOYER`.

### Chưa làm trong giai đoạn này

- Public Job listing và search tại `/api/jobs`.
- Xem hoặc cập nhật các Application của một job.
- Draft job, lịch tự động mở/đóng job và thời hạn tuyển dụng.
- Lịch sử thay đổi chi tiết và khôi phục các phiên bản cũ của job.
- Upload file hoặc media cho job.

## 3. Quyết định thiết kế

### Xác thực và ownership

- `employerId` được lấy từ `AuthenticatedUser` do JWT filter đặt trong `SecurityContext`.
- Request tạo/cập nhật không có trường `employerId`.
- Repository tìm chi tiết bằng cặp `jobId + employerId`.
- Job không tồn tại và job thuộc Employer khác đều trả `404` để không làm lộ tài nguyên.
- `/api/employer/**` tiếp tục được bảo vệ tập trung bằng Spring Security với role `EMPLOYER`.

### Phân trang

`GET /api/employer/jobs` nhận:

- `page`: mặc định `0`, không âm.
- `size`: mặc định `20`, từ `1` đến `100`.
- Sắp xếp cố định theo `createdAt DESC` để contract ổn định và tránh cho client truyền tên cột tùy ý.

### Validation

- `category`, `title`, `description`, `location`: bắt buộc khi tạo, không chỉ chứa khoảng trắng.
- `positionLevel`: một trong `INTERN`, `FRESHER`, `JUNIOR`, `SENIOR`.
- `salaryMin`, `salaryMax`: nếu có phải không âm.
- Nếu cả hai mức lương tồn tại thì `salaryMax >= salaryMin`.
- PATCH chỉ cập nhật trường khác `null`; ít nhất một trường phải được gửi.
- Không cho PATCH thay đổi `employerId`, `createdAt` hoặc `updatedAt`.

### Trạng thái và soft delete

- Job mới luôn có trạng thái `OPEN`.
- PATCH có thể chuyển trạng thái giữa `OPEN` và `CLOSED`.
- DELETE là soft delete: chỉ chuyển `current_status` sang `CLOSED` và trả `204 No Content`.
- Không xóa bản ghi Job hoặc Application; toàn bộ lịch sử ứng tuyển được giữ lại.
- Khóa ngoại `application.job_id` dùng `ON DELETE RESTRICT` để database ngăn xóa vật lý một job còn Application.
- Gọi DELETE nhiều lần là idempotent: job đã `CLOSED` vẫn trả `204`.

### HTTP status

- Tạo thành công: `201 Created`.
- Đọc/cập nhật thành công: `200 OK`.
- Đóng job thành công: `204 No Content`.
- Request hoặc salary range không hợp lệ: `400 Bad Request`.
- Thiếu/token không hợp lệ: `401 Unauthorized`.
- JWT đúng nhưng không phải Employer: `403 Forbidden`.
- Job không tồn tại hoặc không thuộc Employer hiện tại: `404 Not Found`.

## 4. Contract API

### 4.1 Tạo job

`POST /api/employer/jobs`

Request:

```json
{
  "category": "Software Development",
  "title": "Java Backend Developer",
  "description": "Develop and maintain backend services",
  "location": "Ha Noi",
  "positionLevel": "JUNIOR",
  "salaryMin": 15000000,
  "salaryMax": 25000000
}
```

Response `201`:

```json
{
  "id": 1,
  "employerId": 7,
  "category": "Software Development",
  "title": "Java Backend Developer",
  "description": "Develop and maintain backend services",
  "location": "Ha Noi",
  "positionLevel": "JUNIOR",
  "salaryMin": 15000000,
  "salaryMax": 25000000,
  "status": "OPEN",
  "createdAt": "2026-10-05T09:00:00",
  "updatedAt": "2026-10-05T09:00:00"
}
```

### 4.2 Danh sách job của Employer

`GET /api/employer/jobs?page=0&size=20`

Response `200`:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

### 4.3 Chi tiết job

`GET /api/employer/jobs/{jobId}`

- Chỉ trả job thuộc Employer hiện tại.
- Response dùng cùng cấu trúc job như API tạo.

### 4.4 Cập nhật job

`PATCH /api/employer/jobs/{jobId}`

Request có thể chứa một hoặc nhiều trường:

```json
{
  "title": "Senior Java Backend Developer",
  "salaryMin": 30000000,
  "salaryMax": 45000000,
  "status": "CLOSED"
}
```

- Trường `null` hoặc không xuất hiện được hiểu là không cập nhật.
- Response `200` trả job sau cập nhật.

### 4.5 Đóng job (soft delete)

`DELETE /api/employer/jobs/{jobId}`

- Kiểm tra ownership trước khi xóa.
- Chuyển trạng thái job sang `CLOSED`, không xóa vật lý.
- Giữ nguyên các Application liên quan để xem lịch sử.
- Thành công trả `204` và không có response body.

### 4.6 Error response

Tiếp tục dùng cấu trúc lỗi chung:

```json
{
  "timestamp": "2026-10-05T09:00:00Z",
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Job not found",
  "path": "/api/employer/jobs/99"
}
```

## 5. Cấu trúc cần triển khai

```text
src/main/java/com/example/jobapp/
├── controller/
│   └── EmployerJobController.java
├── dto/
│   ├── request/
│   │   ├── CreateJobRequest.java
│   │   └── UpdateJobRequest.java
│   └── response/
│       ├── EmployerJobResponse.java
│       └── PageResponse.java
├── exception/
│   ├── InvalidJobDataException.java
│   └── ResourceNotFoundException.java
├── repository/
│   └── JobRepository.java
└── service/
    └── EmployerJobService.java
```

## 6. Các bước triển khai

### Bước 1 — DTO, Repository và lỗi nghiệp vụ

- Khai báo request DTO với Jakarta Validation.
- Tạo response DTO, không trả trực tiếp Entity.
- Tạo `JobRepository` với truy vấn theo `employerId` và `jobId + employerId`.
- Bổ sung lỗi `404` và lỗi salary/update không hợp lệ vào global handler.

### Bước 2 — EmployerJobService

- Tạo job bằng Employer lấy từ JWT.
- Liệt kê job theo Employer và phân trang.
- Đọc/cập nhật/đóng job bằng truy vấn có ownership.
- Kiểm tra salary range sau khi áp dụng dữ liệu PATCH.
- Mapping Entity sang response tại service.

### Bước 3 — Controller và Swagger

- Controller chỉ xử lý HTTP, principal và tham số phân trang.
- Gắn `@SecurityRequirement(name = "bearerAuth")`.
- Mô tả các response `200/201/204/400/401/403/404`.
- Không nhận `employerId` từ path, query hoặc body.

### Bước 4 — Kiểm thử

#### Unit test

`EmployerJobServiceTest` bao gồm:

- Tạo job đúng Employer và trạng thái mặc định `OPEN`.
- Từ chối salary range không hợp lệ.
- Danh sách chỉ gọi truy vấn theo Employer hiện tại.
- Xem job của mình thành công.
- Job không tồn tại hoặc không thuộc quyền trả `ResourceNotFoundException`.
- PATCH chỉ sửa trường được gửi và kiểm tra salary range sau cập nhật.
- DELETE kiểm tra ownership, chuyển status sang `CLOSED` và không gọi repository delete.

#### Web/security test

`EmployerJobControllerTest` bao gồm:

- Không có token trả `401`.
- Token Applicant trả `403`.
- Token Employer gọi được endpoint và service nhận đúng ID từ principal.
- Validation request trả `400` với `fieldErrors`.
- POST trả `201`; DELETE trả `204`.

#### Integration test cần bổ sung khi có Docker

- Chạy migration trên MySQL thật hoặc Testcontainers MySQL.
- Kiểm tra unique/FK/check constraint và `ON DELETE RESTRICT` bảo vệ lịch sử Application.
- Kiểm tra toàn bộ luồng register Employer → login → CRUD job.

## 7. Thứ tự commit đề xuất

1. `docs(employer-jobs): add implementation plan`
2. `feat(employer-jobs): add DTOs repository and job errors`
3. `feat(employer-jobs): implement employer job service and endpoints`
4. `test(employer-jobs): add service and controller coverage`

Mỗi commit phải build được; không đưa thay đổi không liên quan trong `docs/ENDPOINTS.md` vào các commit trên.

## 8. Definition of Done

- [ ] Đủ năm endpoint Employer Jobs.
- [ ] Tất cả endpoint yêu cầu JWT role `EMPLOYER`.
- [ ] Employer ID chỉ lấy từ principal của JWT.
- [ ] Employer không đọc/sửa/đóng được job của Employer khác.
- [ ] DELETE chỉ đóng job và không xóa Job/Application khỏi database.
- [ ] Create/update có validation và kiểm tra salary range.
- [ ] Danh sách có phân trang và sắp xếp ổn định.
- [ ] Không trả Entity hoặc thông tin nhạy cảm trong response.
- [ ] Swagger mô tả đủ request, response và Bearer JWT.
- [ ] Unit test và web/security test pass.
- [ ] Migration được xác nhận trên MySQL trước khi merge.
- [ ] Các commit đúng phạm vi và không chứa secret.

## 9. Luồng kiểm thử thủ công cuối cùng

```text
Register Employer + Company
→ Login Employer
→ POST /api/employer/jobs
→ GET /api/employer/jobs
→ GET /api/employer/jobs/{jobId}
→ PATCH /api/employer/jobs/{jobId}
→ DELETE /api/employer/jobs/{jobId}

Employer A tạo job
→ Employer B dùng ID đó để GET/PATCH/DELETE
→ Nhận 404

Không token
→ Nhận 401

Token Applicant
→ Gọi /api/employer/jobs
→ Nhận 403
```
