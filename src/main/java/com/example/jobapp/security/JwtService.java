package com.example.jobapp.security;

import com.example.jobapp.entity.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMillis;
    private SecretKey signingKey;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMillis
    ) {
        this.secret = secret;
        this.expirationMillis = expirationMillis;
    }

    @PostConstruct
    void initialize() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 256 bits");
        }
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Integer userId, UserRole role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(signingKey)
                .compact();
    }

    public AuthenticatedUser parseAuthenticatedUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Integer userId = Integer.valueOf(claims.getSubject());
        UserRole role = UserRole.valueOf(claims.get("role", String.class));
        return new AuthenticatedUser(userId, role);
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }
}
