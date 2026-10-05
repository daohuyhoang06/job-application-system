CREATE TABLE applicant (
    applicant_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    location VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_applicant_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE company (
    company_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    company_description TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE employer (
    employer_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_employer_email UNIQUE (email),
    CONSTRAINT fk_employer_company
        FOREIGN KEY (company_id) REFERENCES company (company_id)
        ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE job (
    job_id INT AUTO_INCREMENT PRIMARY KEY,
    employer_id INT NOT NULL,
    category VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    job_description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,
    position_level ENUM ('INTERN', 'FRESHER', 'JUNIOR', 'SENIOR') NOT NULL,
    salary_min BIGINT UNSIGNED,
    salary_max BIGINT UNSIGNED,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    current_status ENUM ('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    CONSTRAINT fk_job_employer
        FOREIGN KEY (employer_id) REFERENCES employer (employer_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_job_salary_range CHECK (
        salary_min IS NULL
        OR salary_max IS NULL
        OR salary_max >= salary_min
    ),
    INDEX idx_job_public (current_status, created_at),
    INDEX idx_job_employer (employer_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE application (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    job_id INT NOT NULL,
    applicant_id INT NOT NULL,
    resume_url VARCHAR(500),
    cover_letter TEXT,
    application_status ENUM ('PENDING', 'REVIEWING', 'INTERVIEW', 'OFFERED', 'REJECTED')
        NOT NULL DEFAULT 'PENDING',
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_job
        FOREIGN KEY (job_id) REFERENCES job (job_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_application_applicant
        FOREIGN KEY (applicant_id) REFERENCES applicant (applicant_id)
        ON DELETE CASCADE,
    CONSTRAINT uk_application_job_applicant UNIQUE (job_id, applicant_id),
    INDEX idx_application_applicant (applicant_id, applied_at),
    INDEX idx_application_job (job_id, applied_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
