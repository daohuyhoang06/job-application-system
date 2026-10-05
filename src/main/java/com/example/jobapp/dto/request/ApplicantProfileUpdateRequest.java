package com.example.jobapp.dto.request;
import jakarta.validation.constraints.Size;

public record ApplicantProfileUpdateRequest(
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,
        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location
) {
}