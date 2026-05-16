package org.example.backend.modules.identity.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.modules.identity.dto.request.LoginRequest;
import org.example.backend.modules.identity.dto.response.JwtResponse;
import org.example.backend.common.utils.JwtUtils;
import org.example.backend.security.UserDetailsImpl;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLoginKey().trim(),
                        request.getPassword().trim()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        int tokenVersion = 0;
        String token = jwtUtils.generateToken(authentication, tokenVersion);


        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.info("User {} logged in", userDetails.getUsername());

        return JwtResponse.builder()
                .token(token)
                .id(userDetails.getId())
                .loginKey(userDetails.getUsername())
                .displayName(userDetails.getDisplayName())
                .loginType(userDetails.getLoginType())
                .roles(roles)
                .build();
    }


    @Transactional
    public ApiResponse<String> logout(String token) {
        if (token == null || !jwtUtils.isValid(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        UUID id = jwtUtils.getJti(token);
        UUID userId = jwtUtils.getUserId(token);
        Date expiredAt = jwtUtils.getExpiration(token);
        tokenBlacklistService.blacklist(id, userId, expiredAt.toInstant());
        return ApiResponse.success(HttpStatus.ACCEPTED, "Log out successfully", null);
    }
}