package org.example.backend.modules.identity.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.common.utils.JwtUtils;
import org.example.backend.modules.identity.dto.request.*;
import org.example.backend.modules.identity.dto.response.CommandResponse;
import org.example.backend.modules.identity.dto.response.JwtResponse;
import org.example.backend.modules.identity.dto.response.PinStatusResponse;
import org.example.backend.modules.identity.dto.response.ResetPasswordDTO;
import org.example.backend.modules.identity.services.AccountService;
import org.example.backend.modules.identity.services.AuthService;
import org.example.backend.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/users/change-password")
    public ResponseEntity<ApiResponse<String>> changeUserPassword(
            @RequestHeader("Authorization") String bearerToken,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        String token = bearerToken.substring(7);
        ApiResponse<String> response = accountService.changeUserPassword(request, userDetails.getUsername(), token);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete-account")
    public ResponseEntity<ApiResponse<String>> deleteAccount(
            @RequestBody DeleteAccountRequest request,
            @RequestHeader("Authorization") String bearerToken)
    {
        String token = bearerToken.substring(7);
        return ResponseEntity.ok(
                accountService.deleteAccount(request, token)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lock-account")
    public ResponseEntity<ApiResponse<String>> lockAccount(
            @RequestBody LockAccountRequest request,
            @RequestHeader("Authorization") String bearerToken)
    {
        String token = bearerToken.substring(7);
        return ResponseEntity.ok(
                accountService.lockAccount(request, token)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/unlock-account")
    public ResponseEntity<ApiResponse<String>> unlockAccount(
            @RequestBody UnlockAccountRequest request
            )
    {
        return ResponseEntity.ok(
                accountService.unlockAccount(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request
    ) {
        String token = jwtUtils.resolveToken(request);
        ApiResponse<String> response = authService.logout(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/set-pin")
    public ResponseEntity<ApiResponse<String>> setTransactionPin(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody SetPinRequest request) {
        
        ApiResponse<String> response = accountService.setTransactionPin(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/change-pin")
    public ResponseEntity<ApiResponse<String>> changeTransactionPin(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChangePinRequest request) {
        
        ApiResponse<String> response = accountService.changeTransactionPin(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

        @GetMapping("/users/pin-status")
        public ResponseEntity<ApiResponse<PinStatusResponse>> getTransactionPinStatus(
                        @AuthenticationPrincipal UserDetailsImpl userDetails
        ) {
                return ResponseEntity.ok(accountService.getTransactionPinStatus(userDetails.getUsername()));
        }

        @PostMapping("/users/forgot-pin/create-otp")
        public ResponseEntity<ApiResponse<ResetPasswordDTO>> createOtpForForgotPin(
                        @AuthenticationPrincipal UserDetailsImpl userDetails
        ) {
                return ResponseEntity.ok(accountService.createOtpForForgotPin(userDetails.getUsername()));
        }

        @PostMapping("/users/forgot-pin/confirm")
        public ResponseEntity<ApiResponse<String>> confirmForgotPin(
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @Valid @RequestBody ConfirmForgotPinRequest request
        ) {
                return ResponseEntity.ok(accountService.confirmForgotPin(userDetails.getUsername(), request));
        }
}