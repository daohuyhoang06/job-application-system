package com.example.jobapp.security;

import com.example.jobapp.entity.enums.UserRole;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "super_secret_jwt_key_that_is_longer_than_256_bits_for_tests_123456789",
                86_400_000L
        );
        jwtService.initialize();
    }

    @Test
    void generateAndParseTokenPreservesUserIdAndRole() {
        String token = jwtService.generateToken(42, UserRole.EMPLOYER);

        AuthenticatedUser user = jwtService.parseAuthenticatedUser(token);

        assertThat(user.userId()).isEqualTo(42);
        assertThat(user.role()).isEqualTo(UserRole.EMPLOYER);
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(86_400L);
    }

    @Test
    void parseRejectsTamperedToken() {
        String token = jwtService.generateToken(42, UserRole.APPLICANT);
        String tamperedToken = token.substring(0, token.length() - 1) + "x";

        assertThatThrownBy(() -> jwtService.parseAuthenticatedUser(tamperedToken))
                .isInstanceOf(JwtException.class);
    }
}
