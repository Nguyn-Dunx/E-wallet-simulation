package org.example.backend.modules.identity.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.common.utils.JwtUtils;
import org.example.backend.modules.identity.dto.request.LoginRequest;
import org.example.backend.modules.identity.dto.request.SignupAdminRequest;
import org.example.backend.modules.identity.dto.request.SignupUserRequest;
import org.example.backend.modules.identity.dto.request.VerifyOtpRequest;
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
    private final JwtUtils jwtUtils;

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

    @PostMapping("/users/signup/init")
    public ResponseEntity<ApiResponse<String>> initSignupUser(
            @Valid @RequestBody SignupUserRequest request
    ) {
        ApiResponse<String> response = accountService.initSignupUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * BƯỚC 2: Kiểm tra OTP, nếu đúng thì tạo tài khoản chính thức
     */
    @PostMapping("/users/signup/verify")
    public ResponseEntity<ApiResponse<String>> verifySignupUser(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        ApiResponse<String> response = accountService.verifyAndSignupUser(request.getPhone(), request.getOtp());

        // Trả về 201 Created vì lúc này tài khoản mới chính thức sinh ra
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

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request
    ) {
        String token = jwtUtils.resolveToken(request);
        ApiResponse<String> response = authService.logout(token);
        return ResponseEntity.ok(response);
    }

}