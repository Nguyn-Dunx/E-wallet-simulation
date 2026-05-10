package org.example.backend.modules.wallet.service.internal;

import org.example.backend.modules.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletInternalService {

    Wallet createWallet(UUID userId);

    Wallet getWalletEntityById(UUID walletId);

    Wallet deposit(UUID walletId, BigDecimal amount);

    Wallet withdraw(UUID walletId, BigDecimal amount);

    Wallet getWalletEntityByUserId(UUID userId);
}
