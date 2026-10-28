import http from 'k6/http';
import { check } from 'k6';

// =======================================================================
// KỊCH BẢN 5: HIGH-THROUGHPUT CAPACITY (ÉP 1.000 -> 2.000 REQUESTS/GIÂY)
// Mục tiêu:
// 1. Áp dụng mô hình chuẩn công nghiệp: Open Workload Model (Arrival Rate).
// 2. Không phụ thuộc vào số VUs tĩnh mà trực tiếp ép mục tiêu Throughput:
//    500 RPS -> 1.000 RPS -> 2.000 RPS.
// 3. Tìm điểm bão hòa thông lượng (Throughput Saturation Point) của tầng
//    Tomcat (Virtual Threads) và MySQL HikariCP Connection Pool.
// =======================================================================

export const options = {
  scenarios: {
    high_throughput_challenge: {
      executor: 'ramping-arrival-rate',
      startRate: 200,            // Bắt đầu từ 200 requests/s
      timeUnit: '1s',
      preAllocatedVUs: 300,      // Khởi tạo sẵn 300 VUs trong bộ nhớ
      maxVUs: 2500,              // Cho phép k6 tự động scale tối đa 2.500 VUs nếu hệ thống chậm
      stages: [
        { target: 500, duration: '15s' },   // Nấc 1: Tăng lên 500 RPS (Khởi động)
        { target: 1000, duration: '20s' },  // Nấc 2: Chinh phục mốc 1.000 RPS
        { target: 2000, duration: '20s' },  // Nấc 3: Ép cực hạn 2.000 RPS
        { target: 0, duration: '10s' },     // Hạ tải về 0
      ],
    },
  },
  thresholds: {
    // Tỷ lệ lỗi cho phép dưới 10% tại tải cực đại 2.000 RPS
    http_req_failed: ['rate<0.10'],
    // 95% request hoàn thành trong vòng 2 giây
    http_req_duration: ['p(95)<2000'],
  },
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
  // Phân bổ ngẫu nhiên các loại request đọc theo tỷ lệ thực tế:
  // 50% Xem danh sách phân trang, 30% Tìm kiếm theo từ khóa/danh mục, 20% Xem chi tiết tin
  const rand = Math.random();

  if (rand < 0.50) {
    // 50%: Lấy danh sách việc làm (trang ngẫu nhiên từ 0 đến 4)
    const page = Math.floor(Math.random() * 5);
    const res = http.get(`${BASE_URL}/jobs?page=${page}&size=10`);
    check(res, { 'GET /jobs 200': (r) => r.status === 200 });
  } else if (rand < 0.80) {
    // 30%: Tìm kiếm việc làm
    const keywords = ['Java', 'Engineer', 'Developer', 'Backend', 'Senior'];
    const kw = keywords[Math.floor(Math.random() * keywords.length)];
    const res = http.get(`${BASE_URL}/jobs?keyword=${kw}&category=IT`);
    check(res, { 'GET /jobs search 200': (r) => r.status === 200 });
  } else {
    // 20%: Xem chi tiết việc làm ngẫu nhiên (ID từ 1 đến 50)
    const jobId = Math.floor(Math.random() * 50) + 1;
    const res = http.get(`${BASE_URL}/jobs/${jobId}`);
    check(res, { 'GET /jobs/{id} 200': (r) => r.status === 200 });
  }
}
