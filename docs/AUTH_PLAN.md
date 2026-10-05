# Kế hoạch hoàn thiện Authentication Backend

## 1. Mục tiêu

Hoàn thiện toàn bộ luồng xác thực cho hai loại tài khoản:

- Applicant đăng ký và đăng nhập.
- Employer đăng ký cùng Company và đăng nhập.
- Mật khẩu được băm bằng BCrypt, không lưu hoặc trả về mật khẩu thô.
- API đăng nhập trả JWT chứa định danh người dùng và role.
- Spring Security xác thực JWT tại filter dùng chung, không lặp logic ở controller.
- Phân quyền rõ giữa `APPLICANT` và `EMPLOYER`.
- Có validation, chuẩn hóa lỗi, Swagger và kiểm thử đầy đủ.

## 2. Phạm vi

### Trong phạm vi

```text
POST /api/auth/applicants/register
POST /api/auth/applicants/login
POST /api/auth/employers/register
POST /api/auth/employers/login
```

Đồng thời chuẩn bị nền tảng bảo mật để các API profile, job và application sử dụng JWT sau này.

### Chưa làm trong giai đoạn này

- Refresh token.
- Logout/revoke token phía server.
- Quên hoặc đặt lại mật khẩu.
- Xác minh email.
- OAuth2/đăng nhập Google.
- Admin và quản lý tài khoản.

## 3. Quyết định thiết kế

### JWT payload

JWT gồm các claim tối thiểu:

```json
{
  "sub": "<user-id>",
  "role": "APPLICANT | EMPLOYER",
  "iat": 0,
  "exp": 0
}
```

- Ký bằng HMAC SHA-256 với `jwt.secret` tối thiểu 256 bit.
- Thời hạn lấy từ `jwt.expiration`, hiện mặc định là 24 giờ.
- Không đưa email, password, thông tin công ty hoặc dữ liệu nhạy cảm vào token.
- Cặp `userId + role` xác định duy nhất principal vì Applicant và Employer dùng hai bảng riêng.

### Mật khẩu

- Dùng `BCryptPasswordEncoder`.
- Mật khẩu thô chỉ tồn tại trong request và không được log.
- Quy tắc ban đầu: 8-72 ký tự, có ít nhất một chữ và một số.

### Email

- Trim và chuyển về lowercase trước khi kiểm tra/lưu.
- Email duy nhất trong từng loại tài khoản theo schema hiện tại.
- Cùng một email có thể tồn tại ở cả Applicant và Employer vì hai bảng tách biệt.

### HTTP status

- Đăng ký thành công: `201 Created`.
- Đăng nhập thành công: `200 OK`.
- Request sai validation: `400 Bad Request`.
- Email đã tồn tại: `409 Conflict`.
- Sai email hoặc mật khẩu: `401 Unauthorized`, dùng cùng một thông báo để tránh lộ tài khoản có tồn tại hay không.
- Thiếu/token không hợp lệ/token hết hạn: `401 Unauthorized`.
- Đúng token nhưng sai role: `403 Forbidden`.

## 4. Contract API

### 4.1 Applicant register

`POST /api/auth/applicants/register`

Request:

```json
{
  "email": "applicant@example.com",
  "password": "Password123",
  "fullName": "Nguyen Van A",
  "phone": "0912345678",
  "location": "Ha Noi"
}
```

Validation:

- `email`: bắt buộc, đúng định dạng, tối đa 255 ký tự.
- `password`: bắt buộc, 8-72 ký tự, đạt quy tắc mật khẩu.
- `fullName`: bắt buộc, không chỉ chứa khoảng trắng, tối đa 255 ký tự.
- `phone`: tùy chọn, tối đa 30 ký tự.
- `location`: tùy chọn, tối đa 255 ký tự.

Response `201`:

```json
{
  "id": 1,
  "email": "applicant@example.com",
  "fullName": "Nguyen Van A",
  "phone": "0912345678",
  "location": "Ha Noi",
  "createdAt": "2026-10-04T10:00:00"
}
```

### 4.2 Applicant login

`POST /api/auth/applicants/login`

Request:

```json
{
  "email": "applicant@example.com",
  "password": "Password123"
}
```

Response `200`:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "email": "applicant@example.com",
    "fullName": "Nguyen Van A",
    "role": "APPLICANT"
  }
}
```

### 4.3 Employer register

`POST /api/auth/employers/register`

Request:

```json
{
  "email": "employer@example.com",
  "password": "Password123",
  "fullName": "Tran Van B",
  "company": {
    "name": "Example Company",
    "description": "Software company"
  }
}
```

Validation:

- Các trường tài khoản áp dụng quy tắc tương tự Applicant.
- `company.name`: bắt buộc, tối đa 255 ký tự.
- `company.description`: bắt buộc, không chỉ chứa khoảng trắng.

Response `201`:

```json
{
  "id": 1,
  "email": "employer@example.com",
  "fullName": "Tran Van B",
  "company": {
    "id": 1,
    "name": "Example Company",
    "description": "Software company"
  },
  "createdAt": "2026-10-04T10:00:00"
}
```

Việc tạo Company và Employer phải nằm trong cùng một transaction; nếu một bước lỗi thì rollback toàn bộ.

### 4.4 Employer login

`POST /api/auth/employers/login`

Request giống Applicant login. Response giống cấu trúc login chung, nhưng `role` là `EMPLOYER` và có thể trả thêm `companyId` trong `user`.

### 4.5 Error response chung

```json
{
  "timestamp": "2026-10-04T10:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/auth/applicants/register",
  "fieldErrors": {
    "email": "must be a well-formed email address"
  }
}
```

`fieldErrors` chỉ xuất hiện khi lỗi validation theo trường.

## 5. Cấu trúc cần triển khai

```text
src/main/java/com/example/jobapp/
├── config/
│   ├── SecurityConfig.java
│   └── OpenApiConfig.java
├── controller/
│   └── AuthController.java
├── dto/
│   ├── request/
│   │   ├── ApplicantRegisterRequest.java
│   │   ├── EmployerRegisterRequest.java
│   │   ├── CompanyRegisterRequest.java
│   │   └── LoginRequest.java
│   └── response/
│       ├── ApplicantRegisterResponse.java
│       ├── EmployerRegisterResponse.java
│       ├── AuthResponse.java
│       ├── AuthUserResponse.java
│       └── ErrorResponse.java
├── entity/enums/
│   └── UserRole.java
├── exception/
│   ├── DuplicateEmailException.java
│   ├── InvalidCredentialsException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── ApplicantRepository.java
│   ├── EmployerRepository.java
│   └── CompanyRepository.java
├── security/
│   ├── AuthenticatedUser.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAccessDeniedHandler.java
│   └── JwtService.java
└── service/
    └── AuthService.java
```

Không trả trực tiếp Entity từ controller. Mapping Entity sang response được thực hiện trong service hoặc mapper riêng nếu mapping tăng về sau.

## 6. Các bước triển khai

### Bước 1 — Repository và DTO

- Tạo repository cho Applicant, Employer và Company.
- Bổ sung `existsByEmailIgnoreCase` và `findByEmailIgnoreCase` cho hai repository tài khoản.
- Tạo request/response DTO bằng Java record hoặc class thống nhất toàn project.
- Thêm Jakarta Validation cho toàn bộ input.

Kết quả: contract request/response được cố định và có thể test validation độc lập.

### Bước 2 — Password và AuthService

- Khai báo bean `PasswordEncoder` dùng BCrypt.
- Implement đăng ký Applicant:
  1. Chuẩn hóa email và chuỗi đầu vào.
  2. Kiểm tra email trùng.
  3. Băm mật khẩu.
  4. Lưu Applicant.
  5. Trả response không chứa `passwordHash`.
- Implement đăng ký Employer trong `@Transactional`:
  1. Chuẩn hóa và kiểm tra email.
  2. Tạo Company.
  3. Tạo Employer tham chiếu Company.
  4. Trả response.
- Implement login riêng cho từng role:
  1. Tìm user theo email.
  2. So khớp BCrypt.
  3. Sinh JWT với đúng `userId` và `role`.
  4. Trả token và thông tin user tối thiểu.

Kết quả: business logic Auth hoàn chỉnh, chưa phụ thuộc controller.

### Bước 3 — JWT service và security filter

- `JwtService` chịu trách nhiệm sinh token, parse claim, kiểm tra chữ ký và thời hạn.
- `JwtAuthenticationFilter` đọc header `Authorization: Bearer <token>`.
- Token hợp lệ được chuyển thành `AuthenticatedUser` và đặt vào `SecurityContext`.
- Không query database trong mỗi request nếu token hợp lệ; principal lấy từ claim đã ký.
- Filter chỉ chạy một lần mỗi request bằng `OncePerRequestFilter`.
- Không ghi token vào log.

Kết quả: các API bảo vệ có thể dùng principal hiện tại và role thống nhất.

### Bước 4 — SecurityConfig và lỗi bảo mật

- Tắt CSRF vì backend REST stateless.
- Đặt session policy là `STATELESS`.
- Bỏ HTTP Basic.
- Cho phép public:
  - `/api/auth/**`
  - `/api/jobs` và `/api/jobs/**` cho các method public phù hợp.
  - Swagger/OpenAPI.
- Bảo vệ theo role:
  - `/api/applicants/**`, `/api/applications/**` → `APPLICANT`.
  - `/api/employer/**` → `EMPLOYER`.
- Các route còn lại yêu cầu xác thực.
- Dùng `JwtAuthenticationEntryPoint` trả JSON `401`.
- Dùng `JwtAccessDeniedHandler` trả JSON `403`.

Kết quả: auth và authorization được xử lý tập trung tại Spring Security.

### Bước 5 — Controller, exception và Swagger

- Tạo `AuthController` chỉ nhận request, gọi service và trả HTTP response.
- Tạo `GlobalExceptionHandler` xử lý validation, email trùng, credentials sai và lỗi không mong đợi.
- Không trả stack trace hoặc chi tiết nội bộ cho client.
- Thêm OpenAPI bearer security scheme.
- Mô tả request, response và các mã `201/200/400/401/409` cho bốn endpoint.

Kết quả: bốn endpoint Auth sử dụng được và hiển thị đầy đủ trên Swagger UI.

### Bước 6 — Cấu hình an toàn

- Giữ secret trong biến môi trường `JWT_SECRET`; giá trị mặc định chỉ dùng cho local development.
- Startup phải từ chối secret quá ngắn ở môi trường không phải local/test.
- Chuyển `show-sql` sang biến môi trường hoặc tắt mặc định để tránh log dữ liệu không cần thiết.
- Cập nhật `.env.example` với `JWT_SECRET` và `JWT_EXPIRATION`.
- Không commit secret thật.

### Bước 7 — Kiểm thử

#### Unit test

`AuthServiceTest`:

- Đăng ký Applicant thành công.
- Applicant email trùng, gồm trường hợp khác hoa/thường.
- Password được băm và không lưu plain text.
- Đăng ký Employer tạo cả Company và Employer.
- Đăng ký Employer lỗi thì transaction rollback.
- Login thành công trả đúng role và token.
- Email không tồn tại và password sai cùng trả lỗi credentials chung.

`JwtServiceTest`:

- Sinh và đọc đúng `sub`, `role`, `iat`, `exp`.
- Từ chối token sai chữ ký, malformed và hết hạn.

#### Integration test

Dùng `MockMvc` cùng Testcontainers MySQL để kiểm tra đúng schema Flyway:

- Bốn happy path của register/login.
- Validation trả `400` và đúng `fieldErrors`.
- Email trùng trả `409`.
- Login sai trả `401`.
- API bảo vệ không token trả JSON `401`.
- Token Applicant truy cập route Employer trả `403`.
- Token Employer truy cập route Applicant trả `403`.
- Token đúng role truy cập được endpoint test/protected endpoint hiện có.

Không dùng secret hoặc database production trong test.

## 7. Thứ tự commit đề xuất

1. `feat(auth): add repositories and auth DTOs`
2. `feat(auth): implement registration and login services`
3. `feat(security): add JWT service and authentication filter`
4. `feat(security): configure stateless role-based authorization`
5. `feat(auth): expose auth endpoints and standardized errors`
6. `docs(auth): document auth APIs in OpenAPI and project docs`
7. `test(auth): add unit and integration coverage`

Mỗi commit phải build được; không commit secret, file `.env` thật hoặc dữ liệu test sinh ra.

## 8. Definition of Done

- [ ] Cả bốn endpoint Auth hoạt động theo contract.
- [ ] Password lưu trong DB là BCrypt hash.
- [ ] Employer registration tạo Company và Employer atomically.
- [ ] JWT có `userId`, `role`, thời hạn và chữ ký hợp lệ.
- [ ] API public không cần token; API bảo vệ yêu cầu Bearer token.
- [ ] Thiếu/sai token trả `401`; sai role trả `403` dưới dạng JSON chuẩn.
- [ ] Entity và `passwordHash` không bị serialize ra response.
- [ ] Validation và exception được xử lý tập trung.
- [ ] Swagger hiển thị đủ request/response và nút Authorize cho Bearer JWT.
- [ ] Unit test và integration test pass.
- [ ] `mvn test` pass.
- [ ] `docker compose up --build` chạy được và login qua Swagger thành công.

## 9. Luồng kiểm thử thủ công cuối cùng

```text
Register Applicant
→ Login Applicant
→ Gọi API Applicant bằng Bearer token
→ Dùng token Applicant gọi API Employer và nhận 403

Register Employer + Company
→ Login Employer
→ Gọi API Employer bằng Bearer token
→ Dùng token Employer gọi API Applicant và nhận 403

Không token / token sửa nội dung / token hết hạn
→ Nhận 401 với error response chuẩn
```
