package com.example.jobapp.dto.response;

import java.time.LocalDateTime;

public record EmployerRegisterResponse(
        Integer id,
        String email,
        String fullName,
        CompanyResponse company,
        LocalDateTime createdAt
) {

    public record CompanyResponse(
            Integer id,
            String name,
            String description
    ) {
    }
}
