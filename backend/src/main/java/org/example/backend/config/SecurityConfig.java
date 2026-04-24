package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import org.example.backend.security.AuthEntryPointJWT;
import org.example.backend.security.UserAccountDetailsServiceImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthEntryPointJWT entryPoint;
//    private final UserAccountDetailsServiceImpl userAccountDetailsService;
}
