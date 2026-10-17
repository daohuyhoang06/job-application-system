package com.example.jobapp.dto.response;

import com.example.jobapp.entity.enums.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicantApplicationResponse(
        Integer applicationId,
        Integer jobId,
        String jobTitle,
        String companyName,
        String resumeObjectKey,
        String resumeOriginalFilename,
        String resumeContentType,
        Long resumeSizeBytes,
        String coverLetter,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {
}
