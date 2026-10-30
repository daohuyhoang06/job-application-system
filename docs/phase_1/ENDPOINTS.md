# Đặc tả REST API

[Mục lục tài liệu](../README.md) · [Cài đặt](SETUP.md) · [Bài toán và nghiệp vụ](PROBLEM_DESCRIPTION.md)

## 1. Quy ước chung

- Origin mặc định: `http://localhost:8080`; các endpoint nghiệp vụ có tiền tố `/api`.
- Request/response dùng JSON với tên trường `camelCase`, ngoại trừ nộp CV dùng `multipart/form-data`.
- Endpoint được bảo vệ nhận `Authorization: Bearer <accessToken>`. Vai trò là `APPLICANT` hoặc `EMPLOYER`.
- ID ứng viên/nhà tuyển dụng lấy từ JWT.
- `201` trả tài nguyên vừa tạo; `200` trả dữ liệu; `204` không có response body.
- Enum viết hoa. Thay ID trong ví dụ bằng ID server trả về.
- Timestamp nghiệp vụ dùng ISO local date-time, không có offset; timestamp lỗi dùng UTC `Instant`.

Có thể thử trực tiếp trên [Swagger UI](http://localhost:8080/swagger-ui.html) hoặc lấy schema tại [OpenAPI JSON](http://localhost:8080/v3/api-docs).

## 2. Danh mục 20 endpoint

| Method | Endpoint | Quyền | Thành công | Ý nghĩa |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/applicants/register` | Công khai | `201` | Đăng ký ứng viên |
| POST | `/api/auth/applicants/login` | Công khai | `200` | Đăng nhập ứng viên |
| POST | `/api/auth/employers/register` | Công khai | `201` | Đăng ký employer và công ty mới |
| POST | `/api/auth/employers/login` | Công khai | `200` | Đăng nhập employer |
| GET | `/api/jobs` | Công khai | `200` | Tìm việc đang mở |
| GET | `/api/jobs/{jobId}` | Công khai | `200` | Chi tiết tin đang mở |
| GET | `/api/applicants/me` | `APPLICANT` | `200` | Hồ sơ của mình |
| PATCH | `/api/applicants/me` | `APPLICANT` | `200` | Cập nhật hồ sơ |
| POST | `/api/jobs/{jobId}/applications` | `APPLICANT` | `201` | Nộp đơn kèm PDF |
| GET | `/api/applications/me` | `APPLICANT` | `200` | Các đơn của mình |
| GET | `/api/applications/{applicationId}` | `APPLICANT` | `200` | Chi tiết đơn của mình |
| DELETE | `/api/applications/{applicationId}` | `APPLICANT` | `204` | Rút đơn |
| POST | `/api/employer/jobs` | `EMPLOYER` | `201` | Tạo tin |
| GET | `/api/employer/jobs` | `EMPLOYER` | `200` | Danh sách tin của mình |
| GET | `/api/employer/jobs/{jobId}` | `EMPLOYER` | `200` | Chi tiết tin của mình |
| PATCH | `/api/employer/jobs/{jobId}` | `EMPLOYER` | `200` | Cập nhật / mở lại tin |
| DELETE | `/api/employer/jobs/{jobId}` | `EMPLOYER` | `204` | Đóng tin |
| GET | `/api/employer/jobs/{jobId}/applications` | `EMPLOYER` | `200` | Các đơn thuộc tin mình đăng |
| GET | `/api/employer/applications/{applicationId}` | `EMPLOYER` | `200` | Chi tiết đơn thuộc tin mình đăng |
| PATCH | `/api/employer/applications/{applicationId}/status` | `EMPLOYER` | `200` | Cập nhật trạng thái đơn |

## 3. Đăng ký và đăng nhập

### 3.1. Đăng ký ứng viên

`POST /api/auth/applicants/register`

```json
{
  "email": "candidate@example.com",
  "password": "Password123",
  "fullName": "Nguyễn Văn A",
  "phone": "0912345678",
  "location": "Hà Nội"
}
```

| Trường | Bắt buộc | Ràng buộc |
| --- | --- | --- |
| `email` | Có | Không trắng, đúng định dạng email, tối đa 255 ký tự |
| `password` | Có | 8–72 ký tự, ít nhất một chữ ASCII và một chữ số |
| `fullName` | Có | Không trắng, tối đa 255 ký tự |
| `phone` | Không | Tối đa 30 ký tự |
| `location` | Không | Tối đa 255 ký tự |

Response `201` có `id`, `email`, `fullName`, `phone`, `location`, `createdAt`; không có password hoặc token. Email được trim và tra cứu không phân biệt hoa/thường. Phone/location trắng được chuẩn hóa thành `null` khi đăng ký.

Email đã có trong bảng ứng viên trả `409 DUPLICATE_EMAIL`; dữ liệu vi phạm DTO trả `400 VALIDATION_ERROR`.

### 3.2. Đăng ký nhà tuyển dụng

`POST /api/auth/employers/register`

```json
{
  "email": "recruiter@example.com",
  "password": "Password123",
  "fullName": "Trần Thị B",
  "company": {
    "name": "Example Technology",
    "description": "Phát triển sản phẩm phần mềm."
  }
}
```

`email`, `password`, `fullName` có ràng buộc như đăng ký ứng viên. `company` bắt buộc; `company.name` không trắng, tối đa 255 ký tự; `company.description` không trắng.

Response `201`:

```json
{
  "id": 1,
  "email": "recruiter@example.com",
  "fullName": "Trần Thị B",
  "company": {
    "id": 1,
    "name": "Example Technology",
    "description": "Phát triển sản phẩm phần mềm."
  },
  "createdAt": "2026-10-30T09:00:00"
}
```

Mỗi lần đăng ký tạo một công ty mới trong cùng transaction với employer. Chưa hỗ trợ truyền `companyId` để gia nhập công ty. Email duy nhất trong bảng employer; cùng một email có thể tồn tại riêng ở bảng ứng viên.

### 3.3. Đăng nhập

`POST /api/auth/applicants/login` hoặc `POST /api/auth/employers/login`

```json
{
  "email": "candidate@example.com",
  "password": "Password123"
}
```

Email phải đúng định dạng, email/password không được trắng. Chọn endpoint đúng loại tài khoản. Response ứng viên `200`:

```json
{
  "accessToken": "<JWT>",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "email": "candidate@example.com",
    "fullName": "Nguyễn Văn A",
    "role": "APPLICANT",
    "companyId": null
  }
}
```

Response employer có cùng cấu trúc, `role` là `EMPLOYER` và `companyId` có giá trị. `expiresIn` tính bằng **giây**; giá trị phụ thuộc cấu hình `JWT_EXPIRATION` tính bằng mili giây. Sai email/mật khẩu trả `401 INVALID_CREDENTIALS`.

Chưa có API refresh token, logout hoặc đổi mật khẩu. Trên Swagger, nhập token thuần vào **Authorize**; trên HTTP client, thêm tiền tố `Bearer ` vào header.

## 4. Việc làm công khai

### 4.1. Tìm kiếm và phân trang

`GET /api/jobs`

| Query | Kiểu / mặc định | Hành vi |
| --- | --- | --- |
| `keyword` | Chuỗi, tùy chọn | Tìm chuỗi con trong title hoặc description, không phân biệt hoa/thường |
| `location` | Chuỗi, tùy chọn | Tìm chuỗi con trong location, không phân biệt hoa/thường |
| `category` | Chuỗi, tùy chọn | Khớp category không phân biệt hoa/thường |
| `level` | Enum, tùy chọn | `INTERN`, `FRESHER`, `JUNIOR`, `SENIOR` |
| `salaryMin` | `Long`, tùy chọn | `salaryMax >= giá trị`; nếu salaryMax null thì so với salaryMin của tin |
| `page` | Số nguyên, `0` | Bắt đầu từ 0; số âm được điều chỉnh về 0 |
| `size` | Số nguyên, `10` | Được điều chỉnh vào khoảng 1–20 |

Các điều kiện khác nhau kết hợp bằng AND; tìm keyword trong title/description kết hợp bằng OR. Chuỗi trống được bỏ qua. Chỉ trả tin `OPEN`, sắp xếp `createdAt` giảm dần. Chuỗi query cần URL encode, đặc biệt khoảng trắng và tiếng Việt.

```http
GET /api/jobs?keyword=Java&category=IT&level=JUNIOR&salaryMin=15000000&page=0&size=10
```

Response `200`:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Java Backend Developer",
      "category": "IT",
      "description": "Phát triển REST API với Spring Boot.",
      "location": "Hà Nội",
      "positionLevel": "JUNIOR",
      "salaryMin": 15000000,
      "salaryMax": 25000000,
      "status": "OPEN",
      "company": {
        "id": 1,
        "name": "Example Technology",
        "description": "Phát triển sản phẩm phần mềm."
      },
      "createdAt": "2026-10-30T09:00:00",
      "updatedAt": "2026-10-30T09:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

`PageResponse` có các trường trên cho cả danh sách public và danh sách tin employer. Khi không có kết quả, `content` là `[]`. Chưa có query `sort` tùy chọn. API chưa quy định trường tiền tệ hoặc kỳ trả lương.

### 4.2. Xem chi tiết

`GET /api/jobs/{jobId}` trả một object có cấu trúc giống phần tử `content` ở trên. Tin không tồn tại hoặc đã `CLOSED` trả `404 RESOURCE_NOT_FOUND`.

## 5. Hồ sơ ứng viên

`GET /api/applicants/me` trả:

```json
{
  "id": 1,
  "email": "candidate@example.com",
  "fullName": "Nguyễn Văn A",
  "phone": "0912345678",
  "location": "Hà Nội"
}
```

`PATCH /api/applicants/me` nhận một hoặc nhiều trường:

```json
{
  "fullName": "Nguyễn Văn A",
  "phone": "0987654321",
  "location": "Đà Nẵng"
}
```

Ba trường có giới hạn tương ứng 255, 30 và 255 ký tự. Bỏ qua trường hoặc gửi `null` để giữ nguyên. Service chưa trim hay từ chối chuỗi trắng; gửi `{}` trả lại hồ sơ hiện có. Response `200` có cùng cấu trúc với `GET`. Email và mật khẩu không cập nhật qua endpoint này.

## 6. Tin tuyển dụng của nhà tuyển dụng

### 6.1. Tạo tin

`POST /api/employer/jobs`

```json
{
  "category": "IT",
  "title": "Java Backend Developer",
  "description": "Phát triển REST API với Spring Boot.",
  "location": "Hà Nội",
  "positionLevel": "JUNIOR",
  "salaryMin": 15000000,
  "salaryMax": 25000000
}
```

| Trường | Bắt buộc | Ràng buộc |
| --- | --- | --- |
| `category`, `title`, `location` | Có | Không trắng, tối đa 255 ký tự mỗi trường |
| `description` | Có | Không trắng, tối đa 10.000 ký tự |
| `positionLevel` | Có | `INTERN`, `FRESHER`, `JUNIOR`, `SENIOR` |
| `salaryMin`, `salaryMax` | Không | Số nguyên không âm; nếu đủ hai đầu thì max không nhỏ hơn min |

Tin mới luôn `OPEN`. Response `201` gồm `id`, `employerId`, toàn bộ trường của request, `status`, `createdAt`, `updatedAt`. Khác response public, response quản lý chứa `employerId` và không có object `company`.

### 6.2. Danh sách và chi tiết

- `GET /api/employer/jobs?page=0&size=20`: trả `PageResponse<EmployerJobResponse>`, gồm cả tin mở và đóng do mình đăng, sắp xếp `createdAt` giảm dần.
- `page` mặc định 0, phải không âm; `size` mặc định 20, phải trong 1–100. Sai phạm vi trả `400 INVALID_JOB_DATA`, khác cách điều chỉnh của API public.
- `GET /api/employer/jobs/{jobId}`: trả một `EmployerJobResponse`. Tin không tồn tại hoặc thuộc employer khác trả `404 RESOURCE_NOT_FOUND`.

### 6.3. Cập nhật, đóng và mở lại

`PATCH /api/employer/jobs/{jobId}` nhận các trường như tạo tin và thêm `status` (`OPEN` / `CLOSED`):

```json
{
  "salaryMax": 30000000,
  "status": "CLOSED"
}
```

Cần ít nhất một trường khác `null`; body `{}` bị từ chối với `400 INVALID_JOB_DATA`. Trường không gửi hoặc `null` giữ nguyên, kể cả lương; hiện không thể dùng `null` để xóa giá trị lương đã có. Chuỗi được gửi không được trắng. Kiểm tra khoảng lương áp dụng trên dữ liệu sau khi ghép cập nhật. Response `200` là tin đã cập nhật.

`DELETE /api/employer/jobs/{jobId}` trả `204`, chỉ chuyển trạng thái sang `CLOSED`; giữ lại tin và các đơn. Gọi lại trên tin đã đóng vẫn trả `204`. Có thể mở lại bằng `PATCH` với `{"status":"OPEN"}`.

## 7. Đơn ứng tuyển của ứng viên

### 7.1. Nộp đơn kèm PDF

`POST /api/jobs/{jobId}/applications`

| Multipart part | Bắt buộc | Nội dung |
| --- | --- | --- |
| `file` | Có | PDF không rỗng, tên kết thúc `.pdf`, MIME `application/pdf`, chữ ký đầu `%PDF-`, tối đa 5 MiB |
| `coverLetter` | Không | Chuỗi thư giới thiệu |

Toàn bộ request multipart cũng tối đa 5 MiB, vì vậy chọn file nhỏ hơn giới hạn để chừa phần metadata/boundary. Không gửi JSON `resumeUrl` hoặc `resumeObjectKey` thay file.

Ví dụ PowerShell với `curl.exe` (thay token, ID và đường dẫn thật):

```powershell
curl.exe -X POST "http://localhost:8080/api/jobs/1/applications" -H "Authorization: Bearer <APPLICANT_TOKEN>" -F "file=@C:/CV/resume.pdf;type=application/pdf" -F "coverLetter=Tôi muốn ứng tuyển vị trí này."
```

Để HTTP client tự tạo `Content-Type: multipart/form-data` kèm boundary. Tin phải tồn tại, đang `OPEN` và chưa có đơn của ứng viên này còn tồn tại. Response `201`:

```json
{
  "applicationId": 1,
  "jobId": 1,
  "jobTitle": "Java Backend Developer",
  "companyName": "Example Technology",
  "resumeObjectKey": "applicants/1/resumes/550e8400-e29b-41d4-a716-446655440000.pdf",
  "resumeOriginalFilename": "resume.pdf",
  "resumeContentType": "application/pdf",
  "resumeSizeBytes": 123456,
  "coverLetter": "Tôi muốn ứng tuyển vị trí này.",
  "status": "PENDING",
  "appliedAt": "2026-10-30T09:10:00"
}
```

Lỗi chính: tin thiếu `404`; tin đóng hoặc đơn trùng `400 INVALID_REQUEST`; PDF bị từ chối `400 INVALID_RESUME`; vượt multipart `413`; lỗi lưu trữ `503`. Xung đột unique constraint ở DB có thể trả `409 DATA_INTEGRITY_ERROR`.

### 7.2. Xem và rút đơn

- `GET /api/applications/me`: mảng `ApplicantApplicationResponse`, mới nhất trước; chưa phân trang.
- `GET /api/applications/{applicationId}`: một object cùng schema; đơn thiếu hoặc thuộc người khác trả `404 RESOURCE_NOT_FOUND`.
- `DELETE /api/applications/{applicationId}`: xóa bản ghi đơn của mình, trả `204`. Đơn thiếu/không thuộc mình trả `404`.

Rút đơn hiện không bị giới hạn bởi trạng thái đơn. Sau khi rút có thể nộp lại nếu tin mở; đơn mới có ID, CV và trạng thái `PENDING` mới. Object CV của đơn cũ chưa được xóa khỏi MinIO.

## 8. Nhà tuyển dụng xử lý đơn

`GET /api/employer/jobs/{jobId}/applications` trả mảng đơn thuộc tin do employer hiện tại đăng; chưa phân trang hoặc quy định sắp xếp trong repository. Tin không tồn tại, không thuộc mình hoặc chưa có đơn đều trả `200` với `[]` theo code hiện tại.

`GET /api/employer/applications/{applicationId}` trả:

```json
{
  "applicationId": 1,
  "applicantId": 1,
  "applicantFullName": "Nguyễn Văn A",
  "applicantEmail": "candidate@example.com",
  "applicantPhone": "0912345678",
  "resumeObjectKey": "applicants/1/resumes/550e8400-e29b-41d4-a716-446655440000.pdf",
  "resumeOriginalFilename": "resume.pdf",
  "resumeContentType": "application/pdf",
  "resumeSizeBytes": 123456,
  "coverLetter": "Tôi muốn ứng tuyển vị trí này.",
  "applicantLocation": "Hà Nội",
  "applicationStatus": "PENDING",
  "appliedAt": "2026-10-30T09:10:00"
}
```

Trạng thái trong response ứng viên tên là `status`; response nhà tuyển dụng dùng `applicationStatus`. Các trường CV là metadata, chưa có URL tải.

`PATCH /api/employer/applications/{applicationId}/status`:

```json
{
  "status": "REVIEWING"
}
```

`status` bắt buộc, thuộc `PENDING`, `REVIEWING`, `INTERVIEW`, `OFFERED`, `REJECTED`. Response `200` là `EmployerApplicationResponse` sau cập nhật. Hiện không có quy tắc bắt buộc thứ tự chuyển trạng thái; có thể chọn bất kỳ giá trị hợp lệ nào. Đơn của tin đã đóng vẫn có thể được xử lý.

Hai API chi tiết/cập nhật hiện trả `500 INTERNAL_SERVER_ERROR` nếu đơn không tồn tại hoặc không thuộc employer. Nguyên nhân là service ném `IllegalStateException`, được handler chung xử lý thành `500`.

## 9. Response lỗi

Ví dụ lỗi validation:

```json
{
  "timestamp": "2026-10-30T02:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/auth/applicants/register",
  "fieldErrors": {
    "email": "Email must be valid"
  }
}
```

`fieldErrors` chỉ xuất hiện khi có dữ liệu lỗi theo trường; các lỗi còn lại thường chỉ có năm trường đầu.

| HTTP | `error` | Trường hợp |
| --- | --- | --- |
| `400` | `VALIDATION_ERROR` | Vi phạm ràng buộc DTO |
| `400` | `INVALID_REQUEST_BODY` | JSON không đọc được hoặc enum body không hỗ trợ |
| `400` | `INVALID_JOB_DATA` | Khoảng lương, body cập nhật rỗng hoặc phân trang employer sai |
| `400` | `INVALID_REQUEST` | `IllegalArgumentException`, gồm tin đóng hoặc đơn trùng được phát hiện ở service |
| `400` | `INVALID_RESUME` | File không đạt kiểm tra PDF |
| `401` | `INVALID_CREDENTIALS` | Đăng nhập sai thông tin |
| `401` | `UNAUTHORIZED` | Endpoint được bảo vệ nhưng chưa xác thực thành công |
| `403` | `FORBIDDEN` | Role không có quyền gọi endpoint |
| `404` | `RESOURCE_NOT_FOUND` | Service ném `ResourceNotFoundException` |
| `409` | `DUPLICATE_EMAIL` | Email đã tồn tại trong loại tài khoản đăng ký |
| `409` | `DATA_INTEGRITY_ERROR` | Xung đột ràng buộc dữ liệu |
| `413` | `RESUME_TOO_LARGE` | Vượt giới hạn multipart và được xử lý bởi upload handler |
| `503` | `RESUME_STORAGE_UNAVAILABLE` | Không đọc/lưu được CV trong thao tác upload |
| `500` | `INTERNAL_SERVER_ERROR` | Exception chưa có handler riêng |