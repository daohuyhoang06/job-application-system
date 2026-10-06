package com.example.jobapp.dto.response;

import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.entity.enums.PositionLevel;

import java.time.LocalDateTime;

public record PublicJobResponse(
        Integer id,
        String title,
        String category,
        String description,
        String location,
        PositionLevel positionLevel,
        Long salaryMin,
        Long salaryMax,
        JobStatus status,
        CompanySummaryResponse company,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
