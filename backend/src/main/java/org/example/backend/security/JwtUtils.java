package org.example.backend.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.config.MessageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.example.backend.common.MessageKeys.ERROR_JWT_INVALID_TOKEN;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtils {
    private final MessageService messageService;
    private final JwtProperties jwtProperties;
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication, Integer version) {
        final String username = authentication.getName();
        final List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        final Date now = new Date();
        final Date expiry = new Date(jwtProperties.getJwtExpirationMs() + now.getTime());
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("ver", version)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn(messageService.getMessage(ERROR_JWT_INVALID_TOKEN));
        }
        return false;
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public List<String> getRoles(String token) {
        List<?> roles = parseClaims(token).get("roles", List.class);

        return roles.stream()
                .map(Object::toString)
                .toList();
    }

    public Integer getVersion(String token) {
        return parseClaims(token).get("ver", Integer.class);
    }
}
