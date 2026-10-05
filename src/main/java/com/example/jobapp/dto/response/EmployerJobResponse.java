package com.example.jobapp.dto.response;

import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.entity.enums.PositionLevel;

import java.time.LocalDateTime;

public record EmployerJobResponse(
        Integer id,
        Integer employerId,
        String category,
        String title,
        String description,
        String location,
        PositionLevel positionLevel,
        Long salaryMin,
        Long salaryMax,
        JobStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
