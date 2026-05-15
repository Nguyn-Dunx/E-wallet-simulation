package org.example.backend.modules.identity.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.modules.identity.dto.request.UpdateNameRequest;
import org.example.backend.modules.identity.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users") // Cấu hình lại base path tùy project của mày
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/display-name") // Hoặc @PutMapping/PatchMapping tùy chuẩn REST mày dùng
    public ResponseEntity<ApiResponse<String>> updateDisplayName(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody UpdateNameRequest request) {

        // Cắt bỏ tiền tố "Bearer " để lấy đúng token core
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;

        ApiResponse<String> response = userService.updateDisplayName(request.newFullName(), token);

        return ResponseEntity.ok(response);
    }
}