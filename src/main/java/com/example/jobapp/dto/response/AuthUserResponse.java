package com.example.jobapp.dto.response;

import com.example.jobapp.entity.enums.UserRole;

public record AuthUserResponse(
        Integer id,
        String email,
        String fullName,
        UserRole role,
        Integer companyId
) {
}
