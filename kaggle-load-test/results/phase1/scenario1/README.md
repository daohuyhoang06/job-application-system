# Kết quả kiểm thử tải Phase 1 (Baseline)

Thư mục này lưu trữ các file kết quả JSON đo đạc từ k6 trên Kaggle CPU.

## Cấu trúc lưu trữ:
- `scenario1_baseline/`: Lưu 3 lần chạy của Kịch bản 1 (Public Read - 50 VUs).
  - `run1.json`
  - `run2.json`
  - `run3.json`
- `scenario2_stress/`: Lưu kết quả của Kịch bản 2 (Stress Test - 100-300 VUs).
- `scenario3_auth/`: Lưu kết quả của Kịch bản 3 (Auth & Write - 30 VUs).

## Tại sao cần chạy 3 lần?
Trong thực nghiệm Kỹ thuật Phần mềm (Empirical Software Engineering), việc chạy 3 lần và lấy giá trị **trung bình cộng (Average)** hoặc **trung vị (Median)** giúp loại trừ sai số ngẫu nhiên (nhiễu CPU, thời gian warm-up) và tăng độ tin cậy tuyệt đối cho báo cáo BTL.
