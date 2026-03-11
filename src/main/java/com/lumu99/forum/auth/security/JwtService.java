package com.lumu99.forum.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    @Value("${security.jwt.secret:lumu99-default-jwt-secret-key-please-change-in-prod-2026}")
    private String secret;

    @Value("${security.jwt.expiration-seconds:7200}")
    private long expirationSeconds;

    public String generateToken(String userUuid, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userUuid)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(signingKey())
                .compact();
    }

    public Optional<JwtPrincipal> parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String role = claims.get("role", String.class);
            if (claims.getSubject() == null || role == null) {
                return Optional.empty();
            }
            return Optional.of(new JwtPrincipal(claims.getSubject(), role));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record JwtPrincipal(String userUuid, String role) {
    }
}
