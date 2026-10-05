package com.example.jobapp.dto.response;
import com.example.jobapp.entity.enums.ApplicationStatus;
import java.time.LocalDateTime;

public record EmployerApplicationResponse(
        Integer applicationId,
        Integer applicantId,
        String applicantFullName,
        String applicantEmail,
        String applicantPhone,
        String resumeUrl,
        String coverLetter,
        String applicantLocation,
        ApplicationStatus applicationStatus,
        LocalDateTime appliedAt
) {
}