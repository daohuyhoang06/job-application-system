package com.example.jobapp.dto.response;

import java.time.LocalDateTime;

public record ApplicantRegisterResponse(
        Integer id,
        String email,
        String fullName,
        String phone,
        String location,
        LocalDateTime createdAt
) {
}
