package com.example.jobapp.security;

import com.example.jobapp.entity.enums.UserRole;

public record AuthenticatedUser(
        Integer userId,
        UserRole role
) {
}
