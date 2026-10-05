package com.example.jobapp.dto.response;

public record ApplicantProfileResponse(
        Integer id,
        String email,
        String fullName,
        String phone,
        String location
) {
}