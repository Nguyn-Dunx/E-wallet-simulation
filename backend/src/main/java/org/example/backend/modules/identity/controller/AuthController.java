package org.example.backend.modules.identity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.modules.identity.dto.request.LoginRequest;
import org.example.backend.modules.identity.dto.request.SignupAdminRequest;
import org.example.backend.modules.identity.dto.request.SignupUserRequest;
import org.example.backend.modules.identity.dto.response.CommandResponse;
import org.example.backend.modules.identity.dto.response.JwtResponse;
import org.example.backend.modules.identity.services.AccountService;
import org.example.backend.modules.identity.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AccountService accountService;

    /**
     * Login for both USER and ADMIN
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        JwtResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Signup normal user
     */
    @PostMapping("/users/signup")
    public ResponseEntity<CommandResponse> signupUser(
            @Valid @RequestBody SignupUserRequest request
    ) {

        CommandResponse response =
                accountService.signupUser(request);
        System.out.println("Registering");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Signup admin
     */
    @PostMapping("/admins/signup")
    public ResponseEntity<CommandResponse> signupAdmin(
            @Valid @RequestBody SignupAdminRequest request
    ) {

        CommandResponse response =
                accountService.signupAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}