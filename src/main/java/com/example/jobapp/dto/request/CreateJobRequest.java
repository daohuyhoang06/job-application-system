package com.example.jobapp.dto.request;

import com.example.jobapp.entity.enums.PositionLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateJobRequest(
        @NotBlank(message = "Category is required")
        @Size(max = 255, message = "Category must not exceed 255 characters")
        String category,

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 10000, message = "Description must not exceed 10000 characters")
        String description,

        @NotBlank(message = "Location is required")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        @NotNull(message = "Position level is required")
        PositionLevel positionLevel,

        @PositiveOrZero(message = "Minimum salary must not be negative")
        Long salaryMin,

        @PositiveOrZero(message = "Maximum salary must not be negative")
        Long salaryMax
) {
}
