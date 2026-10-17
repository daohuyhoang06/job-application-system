-- 1. Insert Company
INSERT INTO company (company_id, company_name, company_description, created_at)
VALUES 
(1, 'FPT Software', 'Tập đoàn công nghệ hàng đầu Việt Nam', NOW()),
(2, 'Viettel Digital', 'Tổng công ty Dịch vụ số Viettel', NOW()),
(3, 'VNG Corporation', 'Kỳ lân công nghệ đầu tiên tại Việt Nam', NOW())
ON DUPLICATE KEY UPDATE company_name=VALUES(company_name);

-- 2. Insert Employer (Mật khẩu: Password123)
INSERT INTO employer (employer_id, company_id, email, password_hash, full_name, created_at)
VALUES 
(1, 1, 'hr@fpt.com', '$2a$10$eE0ZcMmsfVb5.N8j.6p62.vR9/aZ17Jd8kYdFf6Y8V8j/cI2rFZZK', 'HR FPT', NOW()),
(2, 2, 'hr@viettel.com', '$2a$10$eE0ZcMmsfVb5.N8j.6p62.vR9/aZ17Jd8kYdFf6Y8V8j/cI2rFZZK', 'HR Viettel', NOW())
ON DUPLICATE KEY UPDATE email=VALUES(email);

-- 3. Insert Applicant (Mật khẩu: Password123)
INSERT INTO applicant (applicant_id, email, password_hash, full_name, phone, location, created_at)
VALUES
(1, 'candidate1@example.com', '$2a$10$eE0ZcMmsfVb5.N8j.6p62.vR9/aZ17Jd8kYdFf6Y8V8j/cI2rFZZK', 'Nguyen Van A', '0912345678', 'Hà Nội', NOW()),
(2, 'candidate2@example.com', '$2a$10$eE0ZcMmsfVb5.N8j.6p62.vR9/aZ17Jd8kYdFf6Y8V8j/cI2rFZZK', 'Tran Thi B', '0987654321', 'TP.HCM', NOW())
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 4. Insert 50 sample Jobs
INSERT INTO job (job_id, employer_id, category, title, job_description, location, position_level, salary_min, salary_max, current_status, created_at)
VALUES 
(1, 1, 'IT', 'Backend Java Spring Boot Engineer', 'Tuyển dụng Backend Developer có kinh nghiệm Spring Boot, MySQL', 'Hà Nội', 'JUNIOR', 15000000, 25000000, 'OPEN', NOW()),
(2, 1, 'IT', 'Senior Java Architect', 'Thiết kế kiến trúc hệ thống lớn, High Availability, Microservices', 'Hà Nội', 'SENIOR', 35000000, 60000000, 'OPEN', NOW()),
(3, 1, 'IT', 'Frontend ReactJS Developer', 'Phát triển giao diện web hiện đại với React, TypeScript, Tailwind', 'Đà Nẵng', 'FRESHER', 10000000, 16000000, 'OPEN', NOW()),
(4, 2, 'IT', 'DevOps & Cloud Engineer', 'Vận hành hạ tầng AWS, Kubernetes, CI/CD pipeline', 'TP.HCM', 'SENIOR', 30000000, 50000000, 'OPEN', NOW()),
(5, 2, 'IT', 'Intern Java Software Engineer', 'Thực tập sinh lập trình Java backend, đào tạo từ đầu', 'Hà Nội', 'INTERN', 5000000, 8000000, 'OPEN', NOW()),
(6, 2, 'Design', 'UI/UX Designer', 'Thiết kế wireframe, prototype cho ứng dụng ngân hàng số', 'Hà Nội', 'JUNIOR', 12000000, 20000000, 'OPEN', NOW()),
(7, 1, 'Marketing', 'Digital Marketing Executive', 'Lên kế hoạch và thực thi chiến dịch quảng cáo đa kênh', 'TP.HCM', 'JUNIOR', 12000000, 18000000, 'OPEN', NOW()),
(8, 2, 'IT', 'Data Engineer', 'Xây dựng pipeline ETL, Data Warehouse, Spark, Hadoop', 'TP.HCM', 'SENIOR', 30000000, 55000000, 'OPEN', NOW()),
(9, 1, 'IT', 'Mobile Flutter Developer', 'Phát triển ứng dụng mobile iOS & Android bằng Flutter', 'Đà Nẵng', 'FRESHER', 9000000, 15000000, 'OPEN', NOW()),
(10, 2, 'IT', 'QA/QC Automation Tester', 'Viết test script tự động với Selenium, JUnit, Postman', 'Hà Nội', 'JUNIOR', 14000000, 22000000, 'OPEN', NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- Thêm 40 job nữa với biến thể ID từ 11 đến 50
INSERT INTO job (job_id, employer_id, category, title, job_description, location, position_level, salary_min, salary_max, current_status, created_at)
SELECT 
    t.n + 10,
    IF(t.n % 2 = 0, 1, 2),
    IF(t.n % 3 = 0, 'IT', IF(t.n % 3 = 1, 'Marketing', 'Sales')),
    CONCAT('Vị trí tuyển dụng chuyên môn #', t.n + 10),
    CONCAT('Mô tả công việc chi tiết cho vị trí tuyển dụng số ', t.n + 10, ' tại công ty công nghệ.'),
    IF(t.n % 3 = 0, 'Hà Nội', IF(t.n % 3 = 1, 'TP.HCM', 'Đà Nẵng')),
    IF(t.n % 4 = 0, 'INTERN', IF(t.n % 4 = 1, 'FRESHER', IF(t.n % 4 = 2, 'JUNIOR', 'SENIOR'))),
    10000000 + (t.n * 500000),
    20000000 + (t.n * 800000),
    'OPEN',
    NOW()
FROM (
    SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
    UNION SELECT 31 UNION SELECT 32 UNION SELECT 33 UNION SELECT 34 UNION SELECT 35
    UNION SELECT 36 UNION SELECT 37 UNION SELECT 38 UNION SELECT 39 UNION SELECT 40
) t
ON DUPLICATE KEY UPDATE title=VALUES(title);
