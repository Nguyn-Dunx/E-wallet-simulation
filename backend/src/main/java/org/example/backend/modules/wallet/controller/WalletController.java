package org.example.backend.modules.wallet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.common.utils.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet() {

        UUID userId = securityUtils.getCurrentUserId(); // get ID from Token
        WalletResponse response = walletService.getMyWallet(userId);

        return ResponseEntity.ok(response); // status: 200 ok

    }

    @GetMapping("/me/banks")
    public ResponseEntity<?> getMyLinkedBanks() {
        UUID userId = securityUtils.getCurrentUserId(); // Mock user
        return ResponseEntity.ok(ApiResponse.success("Retrieve the list of banks successfully!", walletService.getMyLinkedSources(userId)));
    }

    @PostMapping("/me/banks")
    public ResponseEntity<ApiResponse<LinkedSourceResponse>> linkBank(
            @RequestBody @Valid LinkBankRequest request) {
        UUID userId = securityUtils.getCurrentUserId();

        LinkedSourceResponse data = walletService.linkBankAccount(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Bank account linked successfully!", data));
    }

    @DeleteMapping("/me/banks/{sourceId}")
    public ResponseEntity<ApiResponse<Void>> unlinkBank(@PathVariable UUID sourceId) {
        UUID userId = securityUtils.getCurrentUserId();
        walletService.unlinkBankAccount(userId, sourceId);
        return ResponseEntity.ok(ApiResponse.success(
                "The bank account has been successfully unlinked!",null));
    }

}
