import http from 'k6/http';
import { check, sleep } from 'k6';

// =======================================================================
// KỊCH BẢN 2: STRESS TEST - TÌM ĐIỂM GÃY (BREAKING POINT) (100 -> 300 VUs)
// Mục tiêu: Đẩy số lượng người dùng vượt giới hạn bình thường để xem 
// khi nào CPU đạt 100%, kết nối MySQL bị nghẽn và xuất hiện lỗi 500/timeout.
// =======================================================================

export const options = {
  stages: [
    { duration: '10s', target: 100 }, // Nấc 1: 100 users
    { duration: '20s', target: 200 }, // Nấc 2: Đẩy lên 200 users
    { duration: '20s', target: 300 }, // Nấc 3: Cao trào 300 users (Tìm điểm gãy)
    { duration: '10s', target: 0 },   // Hạ tải
  ],
  thresholds: {
    // Không đặt threshold chặn fail vì mục đích bài test là quan sát khi nào hệ thống bị quá tải
    http_req_duration: ['p(95)<3000'],
  },
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
  // Gửi dồn dập các truy vấn nặng vào database
  const resList = http.get(`${BASE_URL}/jobs?page=0&size=20`);
  check(resList, { 'GET /jobs 200': (r) => r.status === 200 });

  const resFilter = http.get(`${BASE_URL}/jobs?keyword=Engineer&location=Hanoi`);
  check(resFilter, { 'GET /jobs search 200': (r) => r.status === 200 });

  const randomJobId = Math.floor(Math.random() * 50) + 1;
  const resDetail = http.get(`${BASE_URL}/jobs/${randomJobId}`);
  check(resDetail, { 'GET /jobs/{id} 200': (r) => r.status === 200 });

  // Giảm thời gian nghỉ xuống 0.05s để ép tải CPU và Hikari Connection Pool tối đa
  sleep(0.05);
}
