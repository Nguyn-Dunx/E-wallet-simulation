package org.example.backend.modules.wallet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.modules.wallet.dto.response.WalletResponse;
import org.example.backend.modules.wallet.entity.Wallet;
import org.example.backend.modules.wallet.mapper.WalletMapper;
import org.example.backend.modules.wallet.repository.WalletRepository;
import org.example.backend.modules.wallet.service.WalletService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;


    @Override
    public WalletResponse getMyWallet(UUID userId) {

        log.info("Fetching wallet info for user: {}", userId);
        // Retrieve DB tìm ví
        Wallet wallet = walletRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for this user"));

        return walletMapper.toResponse(wallet); // Map Entity sang DTO
    }
}
