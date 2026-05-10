package org.example.backend.modules.wallet.service.internal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.modules.wallet.entity.Wallet;
import org.example.backend.modules.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletInternalServiceImpl implements WalletInternalService{

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public Wallet createWallet(UUID userId) {
        Wallet newWallet = Wallet.builder()
                .userId(userId)
                // Các field balance, currency, status sẽ dc set bởi @PrePersist
                .build();
        return walletRepository.save(newWallet);
    }

    @Override
    public Wallet getWalletEntityById(UUID walletId) {
        return walletRepository.findByIdAndDeletedAtIsNull(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    @Override
    @Transactional
    public Wallet deposit(UUID walletId, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("Deposit amount must be greater than zero");
        }
        Wallet wallet = getWalletEntityById(walletId);

        // Nap tiền
        BigDecimal balance = wallet.getBalance();
        wallet.setBalance(balance.add(amount));

        log.info("Depositing {} to wallet {}. New balance: {}",amount, walletId, wallet.getBalance());

        return walletRepository.save(wallet);   // *Jpa will check @Version here
    }

    @Override
    @Transactional
    public Wallet withdraw(UUID walletId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdraw amount must be greater than zero");
        }

        Wallet wallet = getWalletEntityById(walletId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            log.error("Insufficient funds in wallet {}", walletId);
            throw new RuntimeException("Insufficient funds"); // Nên tạo InsufficientBalanceException
        }

        // Trừ tiền
        BigDecimal currentBalance = wallet.getBalance();
        wallet.setBalance(currentBalance.subtract(amount));

        log.info("Withdrawing {} from wallet {}. New balance: {}", amount, walletId, wallet.getBalance());

        return walletRepository.save(wallet);   // *Jpa will check @Version here
    }

    @Override
    public Wallet getWalletEntityByUserId(UUID userId) {

        return walletRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }


}
