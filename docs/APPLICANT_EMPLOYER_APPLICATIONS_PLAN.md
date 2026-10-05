 # Kế hoạch Applicant Profile và Employer Applications

## 1. Mục tiêu

Hoàn thiện hai nhóm nghiệp vụ:

- Applicant xem và cập nhật profile của chính mình.
- Employer xem các application thuộc các job của mình.
- Employer xem chi tiết application.
- Employer cập nhật trạng thái application.
- Mọi API được bảo vệ bằng JWT và lấy user ID từ principal.
- Không trả trực tiếp JPA Entity ra API; sử dụng DTO response.

## 2. Phạm vi

### Trong phạm vi

```text
GET   /api/applicants/me
PATCH /api/applicants/me

GET   /api/employer/jobs/{jobId}/applications
GET   /api/employer/applications/{applicationId}
PATCH /api/employer/applications/{applicationId}/status
```

### Quyền truy cập

- `/api/applicants/**` yêu cầu role `APPLICANT`.
- `/api/employer/**` yêu cầu role `EMPLOYER`.
- `AuthenticatedUser(userId, role)` được lấy từ JWT.
- Client không được tự gửi `applicantId` hoặc `employerId` để quyết định đối tượng cần truy cập.

## 3. Quyết định thiết kế

### Kiến trúc phân tầng

```text
HTTP Request -> Controller -> Service -> Repository -> MySQL
```

- Controller xử lý HTTP, path variable, request body và authentication.
- Service chứa business logic và kiểm tra ownership.
- Repository truy vấn dữ liệu qua Spring Data JPA.
- Service map entity sang DTO trước khi trả response.

### Kiểm tra ownership

`ApplicationRepository` dùng các derived query:

```java
findByJob_IdAndJob_Employer_Id(jobId, employerId)
findByIdAndJob_Employer_Id(applicationId, employerId)
```

Các query này đảm bảo employer chỉ xem hoặc cập nhật application thuộc job của mình.

### Transaction

- API đọc dùng `@Transactional(readOnly = true)`.
- API cập nhật profile hoặc status dùng `@Transactional`.

### Dữ liệu được phép cập nhật

Applicant profile chỉ cập nhật:

- `fullName`
- `phone`
- `location`

Không cập nhật `id`, `email` hoặc `passwordHash` trong API profile.

## 4. Contract API

### 4.1 Xem profile Applicant

`GET /api/applicants/me`

Header:

```text
Authorization: Bearer <applicant-jwt>
```

Response `200 OK`:

```json
{
	"id": 1,
	"email": "applicant@example.com",
	"fullName": "Nguyen Van A",
	"phone": "0912345678",
	"location": "Ha Noi"
}
```

### 4.2 Cập nhật profile Applicant

`PATCH /api/applicants/me`

Request:

```json
{
	"fullName": "Nguyen Van B",
	"phone": "0987654321",
	"location": "Ho Chi Minh"
}
```

PATCH cho phép cập nhật một phần. Field không gửi lên được giữ nguyên.

### 4.3 Xem danh sách application của job

`GET /api/employer/jobs/{jobId}/applications`

Header:

```text
Authorization: Bearer <employer-jwt>
```

Response gồm các thông tin application và applicant:

```json
[
	{
		"applicationId": 1,
		"applicantId": 3,
		"applicantFullName": "Nguyen Van A",
		"applicantEmail": "applicant@example.com",
		"applicantPhone": "0912345678",
		"resumeUrl": "https://example.com/resume.pdf",
		"coverLetter": "I am interested in this position.",
		"applicantLocation": "Ha Noi",
		"applicationStatus": "PENDING",
		"appliedAt": "2026-10-05T10:30:00"
	}
]
```

### 4.4 Xem chi tiết application

`GET /api/employer/applications/{applicationId}`

Service tìm theo `applicationId` và employer ID lấy từ JWT.

### 4.5 Cập nhật trạng thái application

`PATCH /api/employer/applications/{applicationId}/status`

Request:

```json
{
	"status": "INTERVIEW"
}
```

Các trạng thái hợp lệ:

```text
PENDING
REVIEWING
INTERVIEW
OFFERED
REJECTED
```

Response trả về application sau khi cập nhật với `200 OK`.

## 5. Cấu trúc cần triển khai

```text
src/main/java/com/example/jobapp/
├── controller/
│   ├── ApplicantController.java
│   └── EmployerController.java
├── dto/
│   ├── request/
│   │   ├── ApplicantProfileUpdateRequest.java
│   │   └── EmployerStatusUpdateRequest.java
│   └── response/
│       ├── ApplicantProfileResponse.java
│       └── EmployerApplicationResponse.java
├── repository/
│   └── ApplicationRepository.java
└── service/
		├── ApplicantService.java
		└── EmployerApplicationService.java

src/test/java/com/example/jobapp/service/
├── ApplicantServiceTest.java
└── EmployerApplicationServiceTest.java
```

## 6. Các bước triển khai

### Bước 1 — Applicant Profile DTO và service

- Tạo `ApplicantProfileUpdateRequest`.
- Tạo `ApplicantProfileResponse`.
- Implement `getMyProfile` và `updateMyProfile`.
- Lấy applicant ID từ `AuthenticatedUser`.

Kết quả: Applicant có thể xem và cập nhật profile của chính mình.

### Bước 2 — Application repository và response DTO

- Tạo `ApplicationRepository`.
- Thêm query kiểm tra application thuộc job và employer.
- Tạo `EmployerApplicationResponse`.
- Không serialize trực tiếp entity `Application` hoặc `Applicant`.

Kết quả: Có lớp truy cập dữ liệu và contract response cho employer.

### Bước 3 — Employer application service

- Implement lấy danh sách application theo `jobId` và employer ID.
- Implement lấy chi tiết theo `applicationId` và employer ID.
- Implement cập nhật status trong transaction.
- Map entity sang `EmployerApplicationResponse`.

Kết quả: Business logic Employer Applications hoàn chỉnh.

### Bước 4 — Controller, Security và Swagger

- Expose hai API GET và một API PATCH trong `EmployerController`.
- Gắn Bearer security requirement cho Swagger.
- Kiểm tra role trong `SecurityConfig`.
- Đảm bảo Applicant token không truy cập route Employer và ngược lại.

Kết quả: Năm endpoint hiển thị và test được trên Swagger UI.

### Bước 5 — Kiểm thử

- Tạo `ApplicantServiceTest` bằng JUnit 5 và Mockito:
	- Kiểm tra đọc profile hiện tại.
	- Kiểm tra cập nhật các field được phép.
	- Kiểm tra applicant không tồn tại.
- Tạo `EmployerApplicationServiceTest` bằng JUnit 5 và Mockito:
	- Kiểm tra lấy danh sách application theo ownership.
	- Kiểm tra xem chi tiết application.
	- Kiểm tra cập nhật status.
	- Kiểm tra employer không có quyền truy cập application.
- Chạy `mvn test`.
- Build Docker bằng `docker compose up --build -d`.
- Test Applicant bằng applicant JWT.
- Test Employer bằng employer JWT.
- Kiểm tra ownership bằng ID của user khác.

## 7. Luồng kiểm thử thủ công

### Applicant

1. Đăng ký applicant.
2. Login applicant và copy `accessToken`.
3. Authorize Swagger bằng applicant JWT.
4. Gọi `GET /api/applicants/me`.
5. Gọi `PATCH /api/applicants/me` với body cập nhật.
6. Gọi lại GET để xác nhận dữ liệu mới.

### Employer

1. Đăng ký employer.
2. Login employer và copy `accessToken`.
3. Authorize Swagger bằng employer JWT.
4. Cần có `jobId` và `applicationId` hợp lệ trong database.
5. Gọi API danh sách application.
6. Gọi API chi tiết application.
7. Gọi PATCH status với status hợp lệ.
8. Gọi lại API chi tiết để xác nhận status mới.

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Docker:

```powershell
docker compose up --build -d
```

Maven:

```powershell
mvn test
```

## 8. Thứ tự commit đề xuất

1. `feat(applicant): add profile APIs`
2. `feat(employer): add application repository and DTOs`
3. `feat(employer): add application query APIs`
4. `feat(employer): add application status update`
5. `docs: add applicant and employer applications plan`

## 9. Definition of Done

- [ ] `GET /api/applicants/me` trả đúng profile của applicant đang đăng nhập.
- [ ] `PATCH /api/applicants/me` cập nhật được các field profile được phép.
- [ ] `GET /api/employer/jobs/{jobId}/applications` chỉ trả application thuộc job của employer hiện tại.
- [ ] `GET /api/employer/applications/{applicationId}` chỉ cho employer có quyền xem application.
- [ ] `PATCH /api/employer/applications/{applicationId}/status` cập nhật được status hợp lệ.
- [ ] Applicant token không truy cập được API Employer Applications.
- [ ] Employer token không truy cập được API Applicant Profile.
- [ ] Không trả `passwordHash` hoặc JPA Entity trực tiếp ra response.
- [ ] Swagger hiển thị đầy đủ năm endpoint và hỗ trợ Bearer JWT.
- [ ] Có unit test cho `ApplicantService` và `EmployerApplicationService`.
- [ ] `mvn test` chạy thành công.
- [ ] `docker compose up --build -d` khởi động được backend và MySQL.
