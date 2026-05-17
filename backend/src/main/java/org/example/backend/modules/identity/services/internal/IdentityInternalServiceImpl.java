package org.example.backend.modules.identity.services.internal;

import lombok.RequiredArgsConstructor;
import org.example.backend.modules.identity.common.enums.AccountStatus;
import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.example.backend.common.exception.PinVerificationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdentityInternalServiceImpl implements IdentityInternalService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(noRollbackFor = PinVerificationException.class)
    public void verifyTransactionPin(UUID userId, String rawPin) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new RuntimeException("Account is locked. Please contact support.");
        }

        if (account.getPinHash() == null) {
            throw new RuntimeException("Transaction PIN is not set. Please set your PIN before making transactions.");
        }

        if (!passwordEncoder.matches(rawPin, account.getPinHash())) {
            int newFailedCount = account.getPinFailedCount() + 1;
            account.setPinFailedCount(newFailedCount);

            if (newFailedCount >= 5) {
                account.setStatus(AccountStatus.LOCKED);
                account.setTokenVersion(account.getTokenVersion() + 1); // Invalid all tokens
                accountRepository.save(account);
                throw new PinVerificationException("Account has been locked due to 5 consecutive invalid PIN attempts. Please contact support.");
            }

            accountRepository.save(account);
            throw new PinVerificationException("Invalid Transaction PIN. Attempts left: " + (5 - newFailedCount));
        }

        // Reset failed count on success
        if (account.getPinFailedCount() > 0) {
            account.setPinFailedCount(0);
            accountRepository.save(account);
        }
    }
}
