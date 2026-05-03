package org.example.backend.modules.wallet.repository;

import org.example.backend.modules.wallet.entity.Wallet;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository {

    Optional<Wallet> findByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<Wallet> findByIdAndDeletedAtIsNull(UUID id);

    Wallet save(Wallet newWallet);
}
