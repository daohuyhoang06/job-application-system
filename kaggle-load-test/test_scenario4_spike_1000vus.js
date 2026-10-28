import http from 'k6/http';
import { check, sleep } from 'k6';

// =======================================================================
// KỊCH BẢN 4: SPIKE TEST - ĐỘT BIẾN TỨC THÌ 1.000 USERS & TỰ PHỤC HỒI
// Mục tiêu:
// 1. Mô phỏng đợt truy cập ồ ạt (Flash-crowd / Ngày hội tuyển dụng lớn):
//    Đang từ 50 VUs bình thường, đột ngột nhảy vọt lên 1.000 VUs chỉ trong 10s.
// 2. Kiểm tra xem hệ thống có bị Crash/OOM hay không khi chịu xung lực cực lớn.
// 3. Quan sát khả năng Tự phục hồi (Self-Healing / Recovery): Sau khi hạ tải
//    về 50 VUs, hệ thống mất bao lâu để đưa độ trễ (latency) về mức ban đầu.
// =======================================================================

export const options = {
  stages: [
    { duration: '10s', target: 50 },    // Nấc 1: Tải bình thường (50 users)
    { duration: '10s', target: 1000 },  // Nấc 2: BÙNG NỔ TỨC THÌ (SPIKE) lên 1.000 users
    { duration: '15s', target: 1000 },  // Nấc 3: Duy trì đỉnh sóng xung kích 1.000 users
    { duration: '10s', target: 50 },    // Nấc 4: Hạ nhanh về 50 users (Kiểm tra phục hồi)
    { duration: '15s', target: 50 },    // Nấc 5: Đo đạc SLA sau phục hồi
    { duration: '5s', target: 0 },      // Hạ tải hoàn toàn
  ],
  thresholds: {
    // Trong bài Spike Test, chấp nhận tỷ lệ lỗi tạm thời < 15% tại đỉnh xung lực
    http_req_failed: ['rate<0.15'],
    // 95% request phải phản hồi dưới 3s
    http_req_duration: ['p(95)<3000'],
  },
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
  // 1. Lấy danh sách việc làm
  const resList = http.get(`${BASE_URL}/jobs?page=0&size=10`);
  check(resList, {
    '1. GET /jobs 200': (r) => r.status === 200,
  });

  // Nghỉ ngắn giữa các thao tác (Think time mô phỏng người dùng thật)
  sleep(0.3);

  // 2. Tìm kiếm việc làm theo từ khóa
  const resSearch = http.get(`${BASE_URL}/jobs?keyword=Java&category=IT`);
  check(resSearch, {
    '2. GET /jobs search 200': (r) => r.status === 200,
  });

  sleep(0.3);

  // 3. Xem chi tiết ngẫu nhiên một công việc (ID từ 1 đến 50)
  const randomJobId = Math.floor(Math.random() * 50) + 1;
  const resDetail = http.get(`${BASE_URL}/jobs/${randomJobId}`);
  check(resDetail, {
    '3. GET /jobs/{id} 200': (r) => r.status === 200,
  });

  sleep(0.4);
}
