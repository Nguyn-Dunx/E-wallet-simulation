package org.example.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.config.MessageService;
import org.example.backend.modules.identity.entity.Account;
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
            final String token = resolveToken(request);
            if (token != null && jwtUtils.isValid(token)) {
                final Claims claims = jwtUtils.parseClaims(token);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {

                    final String username = claims.getSubject();
                    final int tokenVersion = jwtUtils.getVersion(token);

                    Object rolesObj = claims.get("roles");

                    final Account account = accountRepository.findAuthAccount(username)
                            .orElseThrow();
                    if (account.getTokenVersion() != tokenVersion) {
                        throw new RuntimeException("Invalid token");
                    }

                    final List<String> roles;

                    if (rolesObj instanceof List<?> list) {
                        roles = list.stream()
                                .map(Object::toString)
                                .toList();
                    } else {
                        roles = List.of();
                    }

                    final var authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    final var authentication = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            log.warn(messageService.getMessage(ERROR_JWT_INVALID_TOKEN, ex.getMessage()));
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        final String bearer = request.getHeader("Authorization");
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
