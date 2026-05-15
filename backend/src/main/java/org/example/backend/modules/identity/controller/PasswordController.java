package org.example.backend.modules.identity.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.modules.identity.dto.request.ConfirmResetPasswordRequest;
import org.example.backend.modules.identity.dto.request.ResetPasswordRequest;
import org.example.backend.modules.identity.dto.request.VerifyOtpRequest;
import org.example.backend.modules.identity.dto.response.ResetPasswordDTO;
import org.example.backend.modules.identity.services.AccountService;
import org.example.backend.modules.identity.services.ResetPasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordController {

    private final ResetPasswordService passwordChange;
    private final AccountService accountService;

    @PostMapping("/users/reset-password/create-otp")
    public ResponseEntity<ApiResponse<ResetPasswordDTO>> createOtp(@RequestBody ResetPasswordRequest request) {
        ApiResponse<ResetPasswordDTO> response  = passwordChange.createOtp(request.getPhone());

        return ResponseEntity.ok(response);
    }

    // Bước 2: Xác thực OTP, trả về Token (UUID)
    @PostMapping("/users/reset-password/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        // Xóa logic check user ở đây đi, đẩy hết việc cho Service xử lý
        ApiResponse<String> response = passwordChange.verifyAndGenerateToken(request.getPhone(), request.getOtp());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Bước 3: Đổi mật khẩu dựa trên Token
    @PostMapping("/users/reset-password/confirm")
    public ResponseEntity<ApiResponse<String>> confirmResetPassword(@RequestBody ConfirmResetPasswordRequest request) {
        ApiResponse<String> response = accountService.resetUserPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(response);
    }
}
