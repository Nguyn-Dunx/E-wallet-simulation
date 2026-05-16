package org.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.utils.JwtUtils;
import org.example.backend.config.MessageService;
import org.example.backend.modules.identity.repository.AccountRepository;
import org.example.backend.modules.identity.services.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.example.backend.common.Constants.ACCESS_TOKEN_COOKIE;
import static org.example.backend.common.Constants.BEARER_PREFIX;

@Slf4j
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final MessageService messageService;
    private final JwtUtils jwtUtils;
    private final AccountRepository accountRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final HandlerExceptionResolver exceptionResolver;

    // Viết constructor thủ công thay vì @RequiredArgsConstructor để an toàn inject HandlerExceptionResolver
    public AuthTokenFilter(
            MessageService messageService,
            JwtUtils jwtUtils,
            AccountRepository accountRepository,
            TokenBlacklistService tokenBlacklistService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.messageService = messageService;
        this.jwtUtils = jwtUtils;
        this.accountRepository = accountRepository;
        this.tokenBlacklistService = tokenBlacklistService;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null) {
                // Ném lỗi nếu token không hợp lệ (hết hạn, sai chữ ký, format láo)
                if (!jwtUtils.isValid(token)) {
                    throw new RuntimeException("Invalid or expired JWT token");
                }

                // Ném lỗi nếu token đã bị đăng xuất / thu hồi
                UUID jti = jwtUtils.getJti(token);
                if (tokenBlacklistService.isBlacklisted(jti)) {
                    throw new RuntimeException("Token has been blacklisted");
                }

                // Đủ điều kiện: Set Security Context
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    processAuthentication(token, request);
                }
            }
        } catch (Exception ex) {
            log.error("JWT Filter Exception: {}", ex.getMessage());
            SecurityContextHolder.clearContext();

            // CHỐT CHẶN: Đẩy lỗi về cho GlobalExceptionHandler xử lý thành JSON
            exceptionResolver.resolveException(request, response, null, ex);

            // CẮT LUỒNG: Không cho request đi tiếp vào Controller gây lỗi 500
            return;
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

        var authentication = buildAuthentication(userId, username, tokenVersion, roles, request);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ================= SMALL METHODS =================

    private void validateTokenVersion(String username, Integer tokenVersion) {
        Integer versionInDb = accountRepository
                .findTokenVersion(username)
                .orElseThrow(() -> new RuntimeException("User not found in database"));

        if (!versionInDb.equals(tokenVersion)) {
            throw new RuntimeException("Token version is outdated. Please login again.");
        }
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(
            UUID userId,
            String username,
            Integer tokenVersion,
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
                tokenVersion,
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
            return bearer.substring(BEARER_PREFIX.length());
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