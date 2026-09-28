# Job Application System — Kế hoạch triển khai Pha 1

**Stack:** MySQL + Docker + Spring Boot 3.x (Java 21)
**Phạm vi:** Backend REST API cho Applicant và Employer
**Nguyên tắc:** Ưu tiên hoàn thiện luồng nghiệp vụ cốt lõi, kiến trúc phân tầng. Viết Test (Unit/Integration) và cập nhật Swagger liên tục cùng lúc với việc phát triển từng endpoint. Dockerize toàn bộ hệ thống ngay từ đầu.

---

## Giai đoạn 1 — Infrastructure & Foundation

**Mục tiêu:** Môi trường dev chạy được, Spring Boot kết nối MySQL, schema và cấu trúc project sẵn sàng.

### BE-1.1 | Setup Docker & Database | Priority: Immediate

- Tạo `Dockerfile` multi-stage cho Backend (Build: Maven, Runtime: JRE 21).
- Tạo `docker-compose.yml` gồm 2 service: `job-backend` và `mysql`.
- Cấu hình MySQL: database name, username/password, port, volume lưu dữ liệu.
- Cấu hình backend nhận biến môi trường (Database URL, Credentials).
- Tạo `.env.example` và `.dockerignore`.
- Kiểm tra `docker compose up --build` chạy thành công kết nối giữa Backend và MySQL.

### BE-1.2 | Spring Boot Project Init | Priority: Immediate

- Khởi tạo Spring Boot 3.x, Java 21, Maven.
- Dependencies:
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-security`
  - MySQL Driver
  - JWT library
  - `springdoc-openapi`
- Cấu hình `application.yml` đọc biến môi trường.

### BE-1.3 | Project Package Structure | Priority: Immediate

Tạo cấu trúc:

```text
controller/
dto/
  request/
  response/
service/
repository/
entity/
security/
config/
exception/
```

Quy ước:

```text
Controller → Service → Repository → Database
```

- Controller chỉ xử lý HTTP.
- Service chứa business logic.
- Repository phụ trách truy cập dữ liệu.

**Quy ước đặt tên (Naming Convention):**
- **Entity:** Danh từ số ít, PascalCase (VD: `Job`, `Applicant`).
- **Repository:** `[Entity]Repository` (VD: `JobRepository`).
- **Service:** `[Feature]Service` (VD: `JobService`).
- **Controller:** `[Feature]Controller` (VD: `EmployerJobController`).
- **DTO Request:** `[Action][Entity]Request` (VD: `CreateJobRequest`, `LoginRequest`).
- **DTO Response:** `[Entity]Response` (VD: `JobDetailResponse`, `ProfileResponse`).
- **Exception:** `[Error]Exception` (VD: `ResourceNotFoundException`).

### BE-1.4 | Database Schema + JPA Entities | Priority: Urgent

- Chốt schema:
  - `applicant`
  - `company`
  - `employer`
  - `job`
  - `application`
- Đổi password field sang dạng `password_hash`.
- Hoàn thiện các field Applicant Profile nếu sử dụng.
- Tạo Entity:
  - `Applicant`
  - `Company`
  - `Employer`
  - `Job`
  - `Application`
- Tạo enum:
  - `PositionLevel`
  - `JobStatus`
  - `ApplicationStatus`
- Mapping các quan hệ:
  - Company 1-N Employer
  - Employer 1-N Job
  - Job 1-N Application
  - Applicant 1-N Application

### BE-1.5 | OpenAPI / Swagger Init | Priority: High

- Cấu hình `springdoc-openapi`.
- Cấu hình Bearer JWT trên Swagger UI (để sẵn sàng cho các phase sau).
- **Quy định chung:** Bắt đầu từ Giai đoạn 2, mỗi khi hoàn thành một API mới, BẮT BUỘC phải bổ sung annotation Swagger và viết kèm Unit/Integration Test cho API đó. Bỏ qua việc tạo trước Repository, ai làm API nào thì người đó tự tạo Repository tương ứng.

**Checkpoint Giai đoạn 1:**

- `docker compose up --build` chạy được cả MySQL và Backend.
- Swagger UI truy cập được tại `/swagger-ui.html`.
- JPA mapping hoạt động.
- Không query DB trực tiếp từ Controller/Service.

---

## Giai đoạn 2 — Authentication & Applicant Profile

**Mục tiêu:** Applicant/Employer đăng ký, đăng nhập bằng JWT và Applicant quản lý được profile.

### BE-2.1 | JWT Authentication + Spring Security | Priority: Urgent

- Cấu hình `PasswordEncoder` dùng BCrypt.
- Implement:
  - `JwtService`
  - JWT generate/validate.
  - JWT Authentication Filter.
  - `SecurityFilterChain`.
- JWT chứa:
  - user id
  - role (`APPLICANT` / `EMPLOYER`)
- Cấu hình:
  - `/api/auth/**` → public.
  - Public Jobs → public.
  - Applicant APIs → Applicant.
  - Employer APIs → Employer.
- Request thiếu token → `401`.
- Sai role → `403`.

### BE-2.2 | Applicant Register & Login | Priority: Urgent

Implement:

```text
POST /api/auth/applicants/register
POST /api/auth/applicants/login
```

- Validate email/password.
- Không cho email Applicant trùng.
- Hash password trước khi lưu.
- Login đúng → trả JWT.
- Login sai → trả lỗi phù hợp.

### BE-2.3 | Employer Register & Login | Priority: Urgent

Implement:

```text
POST /api/auth/employers/register
POST /api/auth/employers/login
```

- Employer đăng ký cùng thông tin Company.
- Tạo Company + Employer trong cùng transaction.
- Hash password.
- Login đúng → trả JWT role `EMPLOYER`.

### BE-2.4 | Applicant Profile | Priority: High

Implement:

```text
GET   /api/applicants/me
PATCH /api/applicants/me
```

- Applicant ID lấy từ JWT.
- Không nhận `applicantId` từ client.
- Cho phép xem/cập nhật các field profile được phép.
- Không trả `password_hash`.

### BE-2.5 | Global Exception Handling + Validation | Priority: High

- Tạo `@RestControllerAdvice`.
- Chuẩn hóa response lỗi.
- Xử lý:
  - validation error
  - resource not found
  - duplicated email
  - unauthorized
  - forbidden
  - duplicated application
- Dùng Jakarta Validation cho Request DTO.

**Checkpoint Giai đoạn 2:**

- Applicant register → login → lấy JWT.
- Employer register → login → lấy JWT.
- Applicant dùng JWT gọi `/api/applicants/me`.
- Không token → `401`.
- Employer token gọi Applicant API → `403`.

---

## Giai đoạn 3 — Job Management & Search

**Mục tiêu:** Public có thể tìm/xem job; Employer quản lý được các job của mình.

### BE-3.1 | Public Job Listing & Search | Priority: Urgent

Implement:

```text
GET /api/jobs
```

Hỗ trợ:

- keyword
- location
- category
- level
- salaryMin
- page
- size

Quy tắc:

- Chỉ trả job `Open`.
- Có phân trang.
- Search/filter thực hiện qua Repository.

### BE-3.2 | Public Job Detail | Priority: High

Implement:

```text
GET /api/jobs/{jobId}
```

- Trả thông tin job.
- Trả thông tin company cần thiết.
- Không expose dữ liệu nhạy cảm của employer.
- Job không tồn tại → `404`.

### BE-3.3 | Employer Create Job | Priority: Urgent

Implement:

```text
POST /api/employer/jobs
```

- Employer ID lấy từ JWT.
- Không nhận `employerId` từ request.
- Validate:
  - title
  - description
  - salary
  - position level
- Status mặc định `Open`.

### BE-3.4 | Employer View Own Jobs | Priority: High

Implement:

```text
GET /api/employer/jobs
GET /api/employer/jobs/{jobId}
```

- Chỉ trả job thuộc employer hiện tại.
- Employer A không xem management view của job Employer B.

### BE-3.5 | Employer Update/Delete Job | Priority: High

Implement:

```text
PATCH  /api/employer/jobs/{jobId}
DELETE /api/employer/jobs/{jobId}
```

- Kiểm tra ownership.
- Cho phép cập nhật thông tin job.
- Cho phép Open/Close job nếu cần.
- Không cho sửa `employerId`.
- Xác nhận hành vi cascade Application khi xóa Job.

**Checkpoint Giai đoạn 3:**

- Public tìm kiếm job thành công.
- Employer đăng job mới.
- Employer xem/sửa/xóa được job của mình.
- Employer không thao tác được job của employer khác.

---

## Giai đoạn 4 — Job Applications

**Mục tiêu:** Applicant ứng tuyển/rút đơn; Employer xem và cập nhật trạng thái đơn ứng tuyển.

### BE-4.1 | Applicant Apply Job | Priority: Urgent

Implement:

```text
POST /api/jobs/{jobId}/applications
```

Business rules:

- Job phải tồn tại.
- Job phải đang `Open`.
- Applicant lấy từ JWT.
- Không apply cùng job hai lần.
- Status mặc định `Pending`.

### BE-4.2 | Applicant View Applications | Priority: High

Implement:

```text
GET /api/applications/me
GET /api/applications/{applicationId}
```

- `/me` trả danh sách đơn của Applicant hiện tại.
- Chi tiết application chỉ owner mới xem được.
- Trả thông tin job và application status.

### BE-4.3 | Applicant Withdraw Application | Priority: High

Implement:

```text
DELETE /api/applications/{applicationId}
```

- Chỉ owner được xóa/rút đơn.
- Application không tồn tại → `404`.

### BE-4.4 | Employer View Job Applications | Priority: Urgent

Implement:

```text
GET /api/employer/jobs/{jobId}/applications
```

- Employer phải sở hữu job.
- Trả danh sách Applicant đã apply.
- Hiển thị thông tin cần thiết:
  - Applicant profile
  - resume URL
  - cover letter
  - application status

### BE-4.5 | Employer View Application Detail | Priority: High

Implement:

```text
GET /api/employer/applications/{applicationId}
```

- Application phải thuộc job của Employer hiện tại.
- Employer khác không xem được.

### BE-4.6 | Employer Update Application Status | Priority: Urgent

Implement:

```text
PATCH /api/employer/applications/{applicationId}/status
```

Status:

```text
Pending
Reviewing
Interview
Offered
Rejected
```

- Validate status.
- Chỉ employer sở hữu job được update.

**Checkpoint Giai đoạn 4:**

- Applicant tìm job → apply.
- Applicant xem danh sách application.
- Applicant rút application.
- Employer xem Applicant đã apply.
- Employer chuyển status `Pending → Reviewing → Interview/...`.

---

## Giai đoạn 5 — Load Testing & Final Review

**Mục tiêu:** Kiểm thử hiệu năng hệ thống để làm cơ sở cho Pha 2 và hoàn thiện tài liệu nộp bài.

### BE-5.1 | Load Test Baseline | Priority: High

- Chọn công cụ: Locust / k6 / JMeter.
- Kịch bản test tối thiểu:
  - `GET /api/jobs` (Tìm kiếm công việc)
  - `GET /api/jobs/{jobId}` (Xem chi tiết)
- Ghi nhận các chỉ số:
  - Requests/sec (Throughput)
  - Average latency
  - P95 latency
  - Error rate
- Môi trường chạy test: Kaggle CPU (bắt buộc theo yêu cầu).
- Lưu script test và kết quả (log/report) vào thư mục `docs/loadtest/` trong repo.

### BE-5.2 | README & Final Review | Priority: High

- Hoàn thiện file `README.md` bao gồm:
  - Giới thiệu tổng quan dự án.
  - Công nghệ sử dụng (Stack).
  - Kiến trúc phân tầng (Controller → Service → Repository).
  - Thiết kế cơ sở dữ liệu (Schema diagram).
  - Phân quyền (Roles).
  - Danh sách API.
  - Hướng dẫn chạy dự án bằng Docker (`docker compose up`).
  - Hướng dẫn truy cập Swagger.
  - Kết quả Load test (tóm tắt).
- Rà soát Checklist:
  - [ ] REST API + JSON.
  - [ ] Có các method GET, POST, DELETE.
  - [ ] Xác thực JWT qua Spring Security Filter.
  - [ ] OpenAPI/Swagger hiển thị đúng và test được.
  - [ ] Unit Test và Integration Test pass 100%.
  - [ ] Dự án khởi chạy trơn tru qua `docker-compose`.
  - [ ] Mã nguồn đẩy lên Github Public đầy đủ.

**Checkpoint Giai đoạn 5:**

- Bất kỳ ai clone repo về, gõ `docker compose up --build` là hệ thống chạy.
- Swagger test trơn tru các luồng (Register -> Login -> Apply).
- Đã có số liệu Load Test rõ ràng để chuẩn bị cho Pha 2.

---

# Tổng kết Deliverables

| Giai đoạn                    | Deliverables                                                                     |
| ------------------------------ | -------------------------------------------------------------------------------- |
| **Foundation**           | Docker Compose (Backend + MySQL), Spring Boot, Schema, Swagger     |
| **Authentication**       | Auth APIs, JWT Security, Profile API, Unit/Integration tests                     |
| **Jobs**                 | Public Job APIs, Employer Job APIs, Unit/Integration tests                       |
| **Applications**         | Apply Job APIs, Application Management APIs, Unit/Integration tests              |
| **Quality & Deployment** | Load test trên Kaggle CPU, README, Final Review                                  |

---

# Endpoint Scope

## AUTH

```text
POST /api/auth/applicants/register
POST /api/auth/applicants/login
POST /api/auth/employers/register
POST /api/auth/employers/login
```

## PUBLIC JOBS

```text
GET /api/jobs
GET /api/jobs/{jobId}
```

## APPLICANT PROFILE

```text
GET   /api/applicants/me
PATCH /api/applicants/me
```

## APPLICANT APPLICATIONS

```text
POST   /api/jobs/{jobId}/applications
GET    /api/applications/me
GET    /api/applications/{applicationId}
DELETE /api/applications/{applicationId}
```

## EMPLOYER JOBS

```text
POST   /api/employer/jobs
GET    /api/employer/jobs
GET    /api/employer/jobs/{jobId}
PATCH  /api/employer/jobs/{jobId}
DELETE /api/employer/jobs/{jobId}
```

## EMPLOYER APPLICATIONS

```text
GET   /api/employer/jobs/{jobId}/applications
GET   /api/employer/applications/{applicationId}
PATCH /api/employer/applications/{applicationId}/status
```
