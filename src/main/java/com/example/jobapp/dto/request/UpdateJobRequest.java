package com.example.jobapp.dto.request;

import com.example.jobapp.entity.enums.JobStatus;
import com.example.jobapp.entity.enums.PositionLevel;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateJobRequest(
        @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, message = "Category must not be blank")
        @Size(max = 255, message = "Category must not exceed 255 characters")
        String category,

        @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, message = "Description must not be blank")
        @Size(max = 10000, message = "Description must not exceed 10000 characters")
        String description,

        @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, message = "Location must not be blank")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        PositionLevel positionLevel,

        @PositiveOrZero(message = "Minimum salary must not be negative")
        Long salaryMin,

        @PositiveOrZero(message = "Maximum salary must not be negative")
        Long salaryMax,

        JobStatus status
) {

    public boolean hasChanges() {
        return category != null
                || title != null
                || description != null
                || location != null
                || positionLevel != null
                || salaryMin != null
                || salaryMax != null
                || status != null;
    }
}
