import http from 'k6/http';
import { check, sleep } from 'k6';

// =======================================================================
// KỊCH BẢN 3: AUTHENTICATION & WRITE OPERATIONS (ĐĂNG KÝ, ĐĂNG NHẬP, XÁC THỰC)
// Mục tiêu: Đo lường chi phí CPU của thuật toán BCrypt và tính an toàn 
// của tầng Bảo mật JWT cùng các tác vụ Ghi (Insert) cơ sở dữ liệu.
// =======================================================================

export const options = {
  stages: [
    { duration: '10s', target: 15 }, // Tăng lên 15 users
    { duration: '20s', target: 30 }, // Duy trì 30 users (BCrypt rất nặng CPU)
    { duration: '10s', target: 0 },  // Hạ về 0
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],    // Tỷ lệ lỗi < 5%
    http_req_duration: ['p(95)<2500'], // BCrypt tốn CPU nên cho phép p95 dưới 2.5s
  },
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
  // Tạo email ngẫu nhiên theo từng VU và timestamp để không bao giờ bị trùng khóa UNIQUE
  const uniqueId = `${__VU}_${__ITER}_${Date.now()}`;
  const userEmail = `candidate_${uniqueId}@test.com`;
  const userPassword = 'Password123';

  // 1. Ghi dữ liệu: Đăng ký tài khoản ứng viên mới (Tốn CPU hash BCrypt + Ghi DB)
  const registerPayload = JSON.stringify({
    email: userEmail,
    password: userPassword,
    fullName: `Candidate ${uniqueId}`,
    phone: '0912345678',
    location: 'Hà Nội'
  });

  const headers = { 'Content-Type': 'application/json' };

  const regRes = http.post(`${BASE_URL}/auth/applicants/register`, registerPayload, { headers });
  check(regRes, {
    '1. POST /register 201': (r) => r.status === 201,
  });

  sleep(0.1);

  // 2. Xác thực: Đăng nhập lấy JWT Token (Tốn CPU verify BCrypt + Sinh JWT)
  const loginPayload = JSON.stringify({
    email: userEmail,
    password: userPassword,
  });

  const loginRes = http.post(`${BASE_URL}/auth/applicants/login`, loginPayload, { headers });
  const loginSuccess = check(loginRes, {
    '2. POST /login 200': (r) => r.status === 200,
  });

  // 3. Đọc có bảo mật: Dùng JWT Token gọi API lấy thông tin cá nhân
  if (loginSuccess && loginRes.json('accessToken')) {
    const token = loginRes.json('accessToken');
    const authHeaders = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    };

    const profileRes = http.get(`${BASE_URL}/applicants/me`, { headers: authHeaders });
    check(profileRes, {
      '3. GET /applicants/me 200': (r) => r.status === 200,
    });
  }

  sleep(0.2);
}
