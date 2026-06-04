package com.saas.billing.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    private final Key jwtSecretKey;
    private final long jwtExpirationMs;
    private final long jwtRefreshExpirationMs;

    public TokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long jwtRefreshExpirationMs) {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtRefreshExpirationMs = jwtRefreshExpirationMs;
    }

    public String generateAccessToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String role = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId().toString())
                .claim("organizationId", userPrincipal.getOrganizationId() != null ? userPrincipal.getOrganizationId().toString() : null)
                .claim("organizationSlug", userPrincipal.getOrganizationSlug())
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public UUID getOrganizationIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        String orgIdStr = claims.get("organizationId", String.class);
        return orgIdStr != null ? UUID.fromString(orgIdStr) : null;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Log security parsing error
        }
        return false;
    }
}
