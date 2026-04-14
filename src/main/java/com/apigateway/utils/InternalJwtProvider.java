package com.apigateway.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class InternalJwtProvider {

    private final SecretKey key;

    public InternalJwtProvider(@Value("${jwt.key}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createInternalToken(String subject, String role, Long userId) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60))
                .signWith(key)
                .compact();
    }
}
