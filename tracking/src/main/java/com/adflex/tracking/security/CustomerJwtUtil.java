package com.adflex.tracking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class CustomerJwtUtil {

    private final Key signingKey;
    private final long expirationMs;

    public CustomerJwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public String generate(UUID leadId, UUID trackingToken) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(Map.of(
                        "typ", "customer",
                        "lead_id", leadId.toString(),
                        "tracking_token", trackingToken.toString()
                ))
                .setSubject(leadId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isCustomerToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return "customer".equals(claims.get("typ", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public UUID extractLeadId(String token) {
        Claims claims = parseClaims(token);
        return UUID.fromString(claims.get("lead_id", String.class));
    }

    public UUID extractTrackingToken(String token) {
        Claims claims = parseClaims(token);
        return UUID.fromString(claims.get("tracking_token", String.class));
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
