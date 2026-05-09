package org.example.backend.modules.wallet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.modules.wallet.dto.request.LinkBankRequest;
import org.example.backend.modules.wallet.dto.response.LinkedSourceResponse;
import org.example.backend.modules.wallet.dto.response.WalletResponse;
import org.example.backend.modules.wallet.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    final WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet() {

        /*
         * [MOCK DATA]
         * (Chưa có JWT Security) -> tạm hard-code một UUID của user để test API trước.
         * Sau này chỉ cần đổi thành:
         * UUID userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().getId();
         *
         * UserDetailsImpl user = (UserDetailsImpl)
           SecurityContextHolder.getContext().getAuthentication().getPrincipal();

           UUID userId = user.getId();
         */
        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        WalletResponse response = walletService.getMyWallet(mockUserId);

        return ResponseEntity.ok(response); // status: 200 ok

    }

    @GetMapping("/me/banks")
    public ResponseEntity<?> getMyLinkedBanks() {
        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"); // Mock user
        return ResponseEntity.ok(walletService.getMyLinkedSources(mockUserId));
    }

    @PostMapping("/me/banks")
    public ResponseEntity<ApiResponse<LinkedSourceResponse>> linkBank(
            @RequestBody @Valid LinkBankRequest request) {
        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"); // Mock user

        LinkedSourceResponse data = walletService.linkBankAccount(mockUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Bank account linked successfully!", data));
    }

    @DeleteMapping("/me/banks/{sourceId}")
    public ResponseEntity<ApiResponse<Void>> unlinkBank(@PathVariable UUID sourceId) {
        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"); // Mock user
        walletService.unlinkBankAccount(mockUserId, sourceId);
        return ResponseEntity.ok(ApiResponse.success(
                "The bank account has been successfully unlinked!",null));
    }

}
