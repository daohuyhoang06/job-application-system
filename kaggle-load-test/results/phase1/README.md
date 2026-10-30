# Kết quả kiểm thử tải Pha 1

Summary JSON của năm kịch bản k6. Bảng tổng hợp và cách chạy nằm trong [TESTING.md](../../../docs/phase_1/TESTING.md).

| Thư mục | Kịch bản | Số tệp kết quả |
| --- | --- | --- |
| [scenario1/](scenario1/) | Baseline đọc, đỉnh 50 VU | 3 |
| [scenario2/](scenario2/) | Stress đọc, đỉnh 300 VU | 3 |
| [scenario3/](scenario3/) | Đăng ký, đăng nhập, đọc hồ sơ, đỉnh 30 VU | 3 |
| [scenario4/](scenario4/) | Spike, đỉnh 1.000 VU | 1 |
| [scenario5/](scenario5/) | Arrival rate, mục tiêu đỉnh 2.000 vòng lặp/giây | 1 |

Chạy từ thư mục gốc repository:

```powershell
python -X utf8 ./kaggle-load-test/results/aggregate_results.py all
```

Bảng tổng hợp lấy trung bình theo lần chạy, kể cả P95. Kịch bản 5 vượt ngưỡng P95 và có 13.409 `dropped_iterations`.

Lưu lượt đo mới bằng tên riêng, kèm commit, cấu hình máy và JVM flags theo [hướng dẫn](../../../docs/phase_1/TESTING.md).
