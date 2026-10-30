# Kiểm thử

[Mục lục](../README.md) · [Yêu cầu bài tập](../yeu_cau.md)

## Chạy test

Cần JDK 21 và Maven. Chạy từ thư mục gốc:

```powershell
mvn test
mvn clean package
```

Kết quả nằm trong `target/surefire-reports/`. Nếu Maven dùng sai JDK, kiểm tra `mvn -version`. Dockerfile build với `-DskipTests`, nên cần chạy test riêng trước khi build image.

| Test | Nội dung |
| --- | --- |
| `AuthServiceTest` | Đăng ký, email trùng, băm mật khẩu, tạo công ty/employer và đăng nhập |
| `ApplicantServiceTest` | Đọc/cập nhật hồ sơ |
| `EmployerJobServiceTest` | Tạo, tìm theo chủ sở hữu, sửa/đóng tin, kiểm tra lương và phân trang |
| `EmployerApplicationServiceTest` | Xem đơn và cập nhật trạng thái theo employer |
| `JwtServiceTest` | Sinh/đọc token, từ chối token bị sửa |
| `EmployerJobControllerTest` | MockMvc: xác thực, phân quyền, validation, tạo và đóng tin |

Các test dùng Mockito hoặc `@WebMvcTest`, không kết nối MySQL/MinIO thật. Luồng nộp/rút đơn, upload PDF và migration cần bổ sung test tích hợp.

## Kiểm tra luồng nghiệp vụ

Chạy dự án theo [SETUP.md](SETUP.md). Tạo hai ứng viên và hai nhà tuyển dụng để kiểm tra cả role lẫn quyền sở hữu.

| Nhóm | Tình huống |
| --- | --- |
| Tài khoản | Đăng ký hợp lệ, email trùng, sai mật khẩu, token thiếu/sai/hết hạn, sai role |
| Tin tuyển dụng | Tạo/sửa/đóng/mở tin, lương sai, enum sai, body cập nhật rỗng, tìm kiếm, phân trang |
| Quyền sở hữu | Tài khoản B không đọc/sửa/rút dữ liệu của A qua API quản lý |
| PDF | File hợp lệ, rỗng, sai MIME/đuôi/chữ ký, quá dung lượng, MinIO mất kết nối |
| Đơn ứng tuyển | Nộp lần đầu, nộp trùng, tin đóng, đổi trạng thái, rút rồi nộp lại |
| Dữ liệu | Migration trên database mới, object khớp metadata, đóng tin giữ đơn, khởi động lại giữ dữ liệu |

Đối chiếu status và response với [đặc tả API](ENDPOINTS.md). Các lỗi đã biết như employer nhận `500` khi đơn không thuộc mình và CV còn trên MinIO sau khi rút đơn được ghi tại đó.

## Kiểm tra trước khi bàn giao

1. Chạy `mvn test`, sau đó khởi động bằng Docker Compose với cấu hình từ `.env.example` và database mới. Kiểm tra Flyway, Swagger UI và `GET /api/jobs`.
2. Thử trọn một luồng: đăng ký hai loại tài khoản → đăng tin → nộp CV → đổi trạng thái → rút đơn. Kiểm tra bằng tài khoản thứ hai để xác nhận quyền sở hữu.
3. Dừng và khởi động lại các container, kiểm tra dữ liệu MySQL và CV vẫn còn trong volume.
4. Chạy tải trên Kaggle CPU, lưu JSON và log từng lượt cùng commit, cấu hình máy, JVM flags và dữ liệu seed.
5. Kiểm tra repository GitHub truy cập công khai; mã nguồn, cấu hình mẫu, migration, test và tài liệu có đủ để chạy lại dự án.

## Kịch bản tải k6

Script nằm trong [kaggle-load-test/](../../kaggle-load-test/). Các kịch bản đọc cần [seed_data.sql](../../kaggle-load-test/seed_data.sql), gồm 50 tin `OPEN` với ID 1–50.

| Kịch bản | Tải | Thời lượng stage | Ngưỡng |
| --- | --- | --- | --- |
| 1 — [baseline_read](../../kaggle-load-test/test_scenario1_baseline_read.js) | Danh sách, tìm kiếm, chi tiết; đỉnh 50 VU | 40 giây | HTTP lỗi < 5%; P95 < 1.500 ms |
| 2 — [stress_test](../../kaggle-load-test/test_scenario2_stress_test.js) | Ba request đọc/vòng, đỉnh 300 VU | 60 giây | P95 < 3.000 ms |
| 3 — [auth_and_write](../../kaggle-load-test/test_scenario3_auth_and_write.js) | Đăng ký, đăng nhập, đọc hồ sơ; đỉnh 30 VU | 40 giây | HTTP lỗi < 5%; P95 < 2.500 ms |
| 4 — [spike_1000vus](../../kaggle-load-test/test_scenario4_spike_1000vus.js) | Tăng từ 50 lên 1.000 VU rồi hạ tải | 65 giây | HTTP lỗi < 15%; P95 < 3.000 ms |
| 5 — [high_rps_2000](../../kaggle-load-test/test_scenario5_high_rps_2000.js) | Arrival rate, mục tiêu đỉnh 2.000 vòng/giây | 65 giây | HTTP lỗi < 10%; P95 < 2.000 ms |

VU là người dùng ảo của k6. Kịch bản 5 gửi một request/vòng, chia khoảng 50% danh sách, 30% tìm kiếm và 20% chi tiết; `preAllocatedVUs=300`, `maxVUs=2500`. `dropped_iterations` đếm số vòng bộ phát tải không khởi chạy được.

Các script dùng cố định `http://localhost:8080/api`. Kịch bản 3 tạo tài khoản mới mỗi vòng, làm dữ liệu tăng sau mỗi lượt chạy. Các kịch bản hiện chưa đo upload/tải CV hoặc thao tác ứng tuyển.

## Kết quả đã lưu

Số liệu lấy từ JSON trong [results/phase1/](../../kaggle-load-test/results/phase1/). Với kịch bản chạy nhiều lần, các cột là trung bình cộng theo lần chạy; cột P95 cũng lấy trung bình các P95, không tính gộp từ request.

| Kịch bản | Số lần | RPS | Latency TB (ms) | P95 TB (ms) | HTTP lỗi (%) |
| --- | --- | --- | --- | --- | --- |
| 1 — Baseline, 50 VU | 3 | 214,03 | 6,19 | 9,80 | 0,00 |
| 2 — Stress, 300 VU | 3 | 914,88 | 166,15 | 518,84 | 0,00 |
| 3 — Auth & write, 30 VU | 3 | 36,11 | 375,71 | 851,67 | 0,00 |
| 4 — Spike, 1.000 VU | 1 | 510,61 | 486,61 | 1.103,48 | 0,00 |
| 5 — Arrival rate, đỉnh 2.000 vòng/s | 1 | 716,80 | 1.097,22 | 3.012,27 | 0,00 |

Kịch bản 1–4 nằm dưới ngưỡng P95 tương ứng. Kịch bản 5 vượt ngưỡng 2.000 ms và có **13.409 vòng lặp bị bỏ**. Tỷ lệ lỗi HTTP 0% chỉ tính các request đã gửi; lượt chạy này không phát đủ tải theo lịch đã cấu hình.

Summary không có chuỗi thời gian hoặc metric hệ thống để tính thời gian phục hồi sau spike hay xác định nguyên nhân chậm. Các tệp hiện có cũng thiếu commit, CPU/RAM và JVM flags của từng lượt đo.

Tổng hợp lại bằng Python 3, không cần cài thêm package:

```powershell
python -X utf8 ./kaggle-load-test/results/aggregate_results.py all
```

Thay `all` bằng `1`–`5` để xem riêng từng kịch bản. Khi đọc báo cáo, lưu ý các điểm sau trong công cụ:

- Script tổng hợp áp ngưỡng HTTP lỗi < 5% cho kịch bản 2, trong khi script k6 chỉ đặt ngưỡng P95.
- Nhận xét về Hikari/MySQL I/O được in sẵn trong script, chưa dựa trên metric hệ thống.
- Khi thiếu `p(99)`, script lấy `max` thay thế; giá trị này không dùng làm P99.
- Ô tổng hợp cuối notebook chỉ đọc `metrics.*.values`, còn JSON đã lưu có dạng phẳng. Dùng script độc lập nếu notebook hiển thị số 0.

## Chạy trên Kaggle CPU

Dùng [phase1_load_test.ipynb](../../kaggle-load-test/phase1_load_test.ipynb):

1. Build JAR bằng `mvn clean package`. Đưa `target/job-application-system-0.0.1-SNAPSHOT.jar` và seed SQL vào Kaggle Dataset; có thể đổi tên JAR thành `app.jar`.
2. Gắn Dataset vào notebook, chọn CPU và bật Internet. Chỉ để một JAR ứng dụng trong nguồn dữ liệu vì notebook chọn file đầu tiên tìm thấy.
3. Chạy các ô cài Java/MySQL/k6, tạo DB, khởi động backend và nạp seed. Kiểm tra log và gọi thử API trước khi chạy tải, kể cả khi ô chờ 15 giây đã xong.
4. Chạy kịch bản tuần tự, lặp lại trong cùng điều kiện. Mỗi lần lưu summary bằng tên riêng để tránh ghi đè.
5. Tải JSON và log về, lưu cùng commit, cấu hình máy, phiên bản phần mềm và dữ liệu đầu vào.

Notebook có bản sao các script `.js`; khi sửa script cần cập nhật cả notebook. Các bản sao được ghi ra `/kaggle/working` khi chạy ô tương ứng.

Notebook cài k6 `0.49.0`, MySQL qua package hệ điều hành và chạy Java với:

```text
-Dspring.threads.virtual.enabled=true
-Dspring.jpa.show-sql=false
-Dspring.datasource.hikari.maximum-pool-size=40
-Dserver.tomcat.accept-count=1000
-Xms1g
-Xmx4g
```

Cấu hình này khác Docker/Maven mặc định. Khi so sánh hai phiên bản, giữ nguyên flags và ghi lại phiên bản MySQL thực tế. Notebook không chạy MinIO vì kịch bản chưa dùng CV.

## Chạy local

Cài k6 và khởi động backend tại `localhost:8080`. Dùng database riêng cho thử tải vì seed có ID cố định và có thể cập nhật bản ghi trùng ID.

Sau khi Flyway đã tạo schema, nạp seed vào MySQL của Compose:

```powershell
docker compose cp ./kaggle-load-test/seed_data.sql mysql:/tmp/seed_data.sql
docker compose exec mysql sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < /tmp/seed_data.sql'
k6 run --summary-export=summary_scenario1.json ./kaggle-load-test/test_scenario1_baseline_read.js
```

Lệnh lấy tài khoản MySQL từ môi trường container. Nếu cần thử đăng nhập, tạo tài khoản qua API thay vì dựa vào mật khẩu ghi trong chú thích seed.
