package org.example.backend.modules.wallet.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.modules.wallet.dto.response.WalletResponse;
import org.example.backend.modules.wallet.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
