package com.example.jobapp.dto.response;

import java.time.LocalDateTime;

public record StoredResumeResponse(
        String objectKey,
        String originalFilename,
        String contentType,
        long sizeBytes,
        LocalDateTime uploadedAt
) {
}
