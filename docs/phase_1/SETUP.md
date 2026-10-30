# Cài đặt và chạy dự án

[Mục lục tài liệu](../README.md) · [Đặc tả API](ENDPOINTS.md)

Chạy các lệnh dưới đây tại thư mục gốc bằng PowerShell. Trên Linux/macOS, dùng `cp` thay `Copy-Item`, `export NAME=value` thay `$env:NAME = "value"`.

## 1. Yêu cầu

| Cách chạy | Yêu cầu |
| --- | --- |
| Toàn bộ bằng Docker | Docker Engine/Desktop đang chạy, Docker Compose v2 |
| Backend bằng Maven | JDK 21, Maven trong `PATH`; MySQL và MinIO có thể chạy bằng Docker |
| Kiểm thử tự động | JDK 21 và Maven; các test hiện có không cần dịch vụ dữ liệu thật |
| Tổng hợp báo cáo tải | Python 3; script tổng hợp dùng thư viện chuẩn |

Repository chưa có Maven Wrapper. Kiểm tra Java mà Maven thực sự dùng bằng `mvn -version`.

## 2. Chạy toàn bộ bằng Docker Compose

Tạo cấu hình lần đầu:

```powershell
Copy-Item .env.example .env
```

Chỉnh `.env` theo môi trường của bạn, sau đó chạy:

```powershell
docker compose up -d --build
docker compose ps
docker compose logs -f job-backend
```

Compose đọc `.env` khi tạo container. Chỉ cần sao chép file mẫu ở lần đầu. Dockerfile build bằng Maven/Java 21 và chạy bằng JRE 21; test được bỏ qua trong bước build image.

Compose chưa có healthcheck. Nếu backend lỗi kết nối ở lần khởi động đầu, xem log MySQL, chờ database sẵn sàng rồi chạy `docker compose restart job-backend`.

### Kiểm tra sau khởi động

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/jobs?page=0&size=10"
```

API trả `200` và mảng `content`. Mở [Swagger UI](http://localhost:8080/swagger-ui.html) để thử API hoặc [OpenAPI JSON](http://localhost:8080/v3/api-docs) để xem schema. Chưa có endpoint health riêng.

Flyway chỉ tạo schema nên database mới chưa có tin tuyển dụng. Đăng ký tài khoản và tạo tin qua API để thử nghiệp vụ. Nếu cần dữ liệu thử tải, xem cách nạp seed trong [TESTING.md](TESTING.md).

### Địa chỉ dịch vụ

| Dịch vụ | Từ máy host | Từ container backend |
| --- | --- | --- |
| Backend | `http://localhost:8080` | — |
| MySQL | `localhost:<DB_PORT>` | `mysql:3306` |
| MinIO API | `http://localhost:<MINIO_API_PORT>` | `http://minio:9000` |
| MinIO Console | `http://localhost:<MINIO_CONSOLE_PORT>` | Không dùng cho upload |

Mặc định các cổng tương ứng là `8080`, `3306`, `9000`, `9001`. Cổng backend được gán trực tiếp trong Compose; thay `DB_PORT` hay cổng MinIO chỉ đổi cổng trên host.

## 3. Chạy backend bằng Maven

Tạo `.env` như bước trên và chỉ khởi động dịch vụ dữ liệu:

```powershell
docker compose up -d mysql minio
```

Chờ MySQL sẵn sàng rồi chạy:

```powershell
java -version
mvn -version
mvn spring-boot:run
```

Nếu `job-backend` đang chạy trong Docker, dừng riêng container đó bằng `docker compose stop job-backend` để giải phóng cổng `8080`.

Maven/JAR không tự đọc `.env`. Nếu dùng nguyên cấu hình mẫu, các giá trị mặc định trong `application.yml` đã khớp. Nếu đổi tài khoản, database hoặc cổng, khai báo biến môi trường trong shell hoặc cấu hình Run của IDE:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/job_application_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:SPRING_DATASOURCE_USERNAME = "job_db_user"
$env:SPRING_DATASOURCE_PASSWORD = "job_db_password"
$env:JWT_SECRET = "replace_with_a_random_secret_of_at_least_32_bytes"
$env:MINIO_ENDPOINT = "http://localhost:9000"
$env:MINIO_ACCESS_KEY = "minio_admin"
$env:MINIO_SECRET_KEY = "change_this_minio_password"
$env:MINIO_BUCKET = "job-cvs"
mvn spring-boot:run
```

Thay các giá trị ví dụ bằng cấu hình thực tế. Muốn chạy JAR, dùng cùng bộ biến môi trường:

```powershell
mvn clean package
java -jar target/job-application-system-0.0.1-SNAPSHOT.jar
```

## 4. Tham chiếu cấu hình

Nguồn cấu hình: [`.env.example`](../../.env.example), [docker-compose.yml](../../docker-compose.yml), [application.yml](../../src/main/resources/application.yml).

### Biến do Compose đọc từ `.env`

| Biến | Ý nghĩa / giá trị mẫu |
| --- | --- |
| `DB_ROOT_PASSWORD` | Mật khẩu root dùng khi MySQL khởi tạo database mới |
| `DB_NAME` | Database, mặc định mẫu `job_application_db` |
| `DB_USER`, `DB_PASSWORD` | Tài khoản ứng dụng MySQL |
| `DB_PORT` | Cổng MySQL công bố trên host, mẫu `3306` |
| `JWT_SECRET` | Chuỗi khóa ký; backend yêu cầu ít nhất 32 byte UTF-8 |
| `JWT_EXPIRATION` | Hạn token tính bằng **mili giây**, mẫu `86400000` (24 giờ) |
| `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD` | Tài khoản MinIO; Compose cũng dùng làm thông tin truy cập của backend |
| `MINIO_BUCKET` | Bucket CV, mẫu `job-cvs` |
| `MINIO_API_PORT` | Cổng API MinIO trên host, mặc định `9000` |
| `MINIO_CONSOLE_PORT` | Cổng console trên host, mặc định `9001` |

### Biến backend nhận trực tiếp

| Biến backend | Giá trị mặc định khi không được truyền |
| --- | --- |
| `SPRING_DATASOURCE_URL` | MySQL tại `localhost:3306/job_application_db`, kèm tham số JDBC trong YAML |
| `SPRING_DATASOURCE_USERNAME` | `job_db_user` |
| `SPRING_DATASOURCE_PASSWORD` | `job_db_password` |
| `JWT_SECRET`, `JWT_EXPIRATION` | Khóa mẫu trong YAML; `86400000` ms |
| `MINIO_ENDPOINT` | `http://localhost:9000` |
| `MINIO_BUCKET` | `job-cvs` |
| `MINIO_ACCESS_KEY` | `minio_admin` |
| `MINIO_SECRET_KEY` | `change_this_minio_password` |

Trong Docker, Compose ánh xạ `DB_*` thành `SPRING_DATASOURCE_*`, `MINIO_ROOT_*` thành `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` và đặt endpoint nội bộ. Khi chạy Maven, tự thực hiện ánh xạ này. `expiresIn` trong response đăng nhập dùng **giây**, khác đơn vị `JWT_EXPIRATION`.

Không commit `.env`; tệp này đã nằm trong `.gitignore`. Thay khóa JWT làm các token được ký bằng khóa cũ không còn hợp lệ.

## 5. Dữ liệu và thao tác thường dùng

```powershell
docker compose logs --tail=100 mysql minio job-backend
docker compose restart job-backend
docker compose stop
docker compose down
```

Dữ liệu nằm trong volume `mysql_data` và `minio_data` (Docker có thể thêm tiền tố project vào tên). `docker compose down` giữ lại volume; thêm `-v` sẽ xóa dữ liệu.

MySQL chỉ dùng các biến `MYSQL_*` khi khởi tạo thư mục dữ liệu mới. Với volume đã có dữ liệu, đổi `.env` không đổi mật khẩu trong DB. Cần cập nhật tài khoản MySQL rồi sửa cấu hình ứng dụng cho khớp.

Flyway tự chạy khi backend khởi động; Hibernate kiểm tra schema sau migration. Không cần chạy SQL migration thủ công. Quy tắc thay đổi schema nằm trong [DATABASE.md](DATABASE.md).

## 6. Xử lý lỗi

| Hiện tượng | Kiểm tra và xử lý |
| --- | --- |
| `docker compose` không kết nối được daemon | Khởi động Docker Engine/Desktop và kiểm tra `docker info`. |
| Cổng đã bị sử dụng | Kiểm tra dịch vụ đang chiếm cổng; đổi cổng host trong `.env` cho MySQL/MinIO hoặc sửa mapping backend trong Compose. |
| Backend báo không kết nối MySQL | Xem log; kiểm tra MySQL đã sẵn sàng, JDBC URL, tài khoản và cổng. Docker dùng hostname `mysql`, Maven dùng `localhost`. |
| Đổi `.env` nhưng đăng nhập MySQL vẫn lỗi | Kiểm tra tài khoản trong volume cũ; chỉnh `.env` không đổi mật khẩu đã khởi tạo. |
| Maven không được nhận diện hoặc sai Java | Cài Maven, JDK 21 và cấu hình `PATH` / `JAVA_HOME`; đối chiếu `mvn -version`. |
| Flyway checksum/schema validation lỗi | Kiểm tra lịch sử migration và schema đang dùng; không sửa migration đã áp dụng để bỏ qua lỗi. |
| API trả `401` | Kiểm tra header Bearer, chữ ký, hạn token và đúng endpoint đăng nhập cho vai trò. |
| API trả `403` | Token hợp lệ nhưng vai trò không được phép gọi endpoint. |
| Upload trả `400` | Đối chiếu PDF, tên `.pdf`, MIME `application/pdf`, chữ ký `%PDF-` và quy tắc đơn trùng/tin đóng. |
| Upload trả `413` | File hoặc toàn bộ multipart vượt giới hạn 5 MiB; chọn file nhỏ hơn để chừa phần overhead. |
| Upload trả `503` | Kiểm tra MinIO API, access key, secret key và quyền tạo/ghi bucket; không dùng cổng console làm endpoint. |

Kiểm tra kết nối MinIO bằng một lần nộp CV. API đọc việc làm chỉ dùng MySQL.
