package com.example.jobapp.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApplyJobRequest(
        @NotBlank(message = "Resume URL is required")
        String resumeUrl,

        String coverLetter
) {
}