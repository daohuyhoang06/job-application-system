import http from 'k6/http';
import { check, sleep } from 'k6';

// Cấu hình kịch bản test nhanh (40s):
export const options = {
  stages: [
    { duration: '10s', target: 30 }, // 10s đầu: Tăng lên 30 users
    { duration: '20s', target: 50 }, // 20s tiếp theo: Duy trì 50 users ổn định
    { duration: '10s', target: 0 },  // 10s cuối: Hạ về 0
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],    // Tỷ lệ request lỗi < 5%
    http_req_duration: ['p(95)<1500'], // 95% request phản hồi dưới 1.5s
  },
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
  // 1. Kịch bản xem danh sách việc làm phân trang
  const resList = http.get(`${BASE_URL}/jobs?page=0&size=10`);
  check(resList, {
    '1. GET /jobs 200': (r) => r.status === 200,
  });

  sleep(0.1);

  // 2. Kịch bản tìm kiếm và lọc (Dùng category=IT & keyword=Java chuẩn ASCII, tránh lỗi Tomcat RFC 7230)
  const resFilter = http.get(`${BASE_URL}/jobs?keyword=Java&category=IT`);
  check(resFilter, {
    '2. GET /jobs search 200': (r) => r.status === 200,
  });

  sleep(0.1);

  // 3. Kịch bản xem chi tiết tin tuyển dụng ngẫu nhiên
  const randomJobId = Math.floor(Math.random() * 50) + 1;
  const resDetail = http.get(`${BASE_URL}/jobs/${randomJobId}`);
  check(resDetail, {
    '3. GET /jobs/{id} 200': (r) => r.status === 200,
  });

  sleep(0.2);
}
