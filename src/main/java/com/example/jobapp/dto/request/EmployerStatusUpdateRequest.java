package com.example.jobapp.dto.request;
import com.example.jobapp.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
public record EmployerStatusUpdateRequest(
        @NotNull(message = "Status must not be null")
        ApplicationStatus status 
) {
}