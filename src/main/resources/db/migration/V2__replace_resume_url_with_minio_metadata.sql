ALTER TABLE application
    RENAME COLUMN resume_url TO resume_object_key,
    ADD COLUMN resume_original_filename VARCHAR(255) NULL AFTER resume_object_key,
    ADD COLUMN resume_content_type VARCHAR(100) NULL AFTER resume_original_filename,
    ADD COLUMN resume_size_bytes BIGINT UNSIGNED NULL AFTER resume_content_type,
    ADD COLUMN resume_uploaded_at DATETIME NULL AFTER resume_size_bytes,
    ADD CONSTRAINT uk_application_resume_object_key UNIQUE (resume_object_key),
    ADD CONSTRAINT chk_application_resume_content_type
        CHECK (resume_content_type IS NULL OR resume_content_type = 'application/pdf'),
    ADD CONSTRAINT chk_application_resume_size_bytes
        CHECK (resume_size_bytes IS NULL OR resume_size_bytes > 0);
