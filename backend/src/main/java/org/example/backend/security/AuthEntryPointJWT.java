package org.example.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.config.MessageService;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.example.backend.common.MessageKeys.ERROR_UNAUTHORIZED;
import static org.example.backend.common.MessageKeys.ERROR_UNAUTHORIZED_DETAILS;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEntryPointJWT implements AuthenticationEntryPoint {
    private final MessageService messageService;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.error(messageService.getMessage(ERROR_UNAUTHORIZED_DETAILS, authException.getMessage()));

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", messageService.getMessage(ERROR_UNAUTHORIZED));
        body.put("message", messageService.getMessage(ERROR_UNAUTHORIZED));
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
