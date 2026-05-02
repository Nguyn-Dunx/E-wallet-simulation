package org.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.utils.JwtUtils;
import org.example.backend.config.MessageService;
import org.example.backend.modules.identity.repository.AccountRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.example.backend.common.Constants.ACCESS_TOKEN_COOKIE;
import static org.example.backend.common.Constants.BEARER_PREFIX;
import static org.example.backend.common.MessageKeys.ERROR_JWT_INVALID_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private final MessageService messageService;
    private final JwtUtils jwtUtils;
    private final AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null && jwtUtils.isValid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                processAuthentication(token, request);
            }

        } catch (Exception ex) {
            log.warn(messageService.getMessage(ERROR_JWT_INVALID_TOKEN, ex.getMessage()));
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    // ================= CORE FLOW =================

    private void processAuthentication(String token, HttpServletRequest request) {

        String username = jwtUtils.getUsername(token);
        UUID userId = jwtUtils.getUserId(token);
        Integer tokenVersion = jwtUtils.getVersion(token);

        validateTokenVersion(username, tokenVersion);

        List<String> roles = jwtUtils.getRoles(token);

        var authentication = buildAuthentication(userId, username, roles, request);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ================= SMALL METHODS =================

    private void validateTokenVersion(String username, Integer tokenVersion) {
        Integer versionInDb = accountRepository
                .findTokenVersion(username)
                .orElseThrow();

        if (!versionInDb.equals(tokenVersion)) {
            throw new RuntimeException("Invalid token");
        }
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(
            UUID userId,
            String username,
            List<String> roles,
            HttpServletRequest request
    ) {

        var authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        UserDetailsImpl userDetails = new UserDetailsImpl(
                userId,
                username,
                null,
                null,
                null,
                null,
                authorities
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        return authentication;
    }

    // ================= TOKEN RESOLVER =================

    private String resolveToken(HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");

        if (StringUtils.hasText(bearer) && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}