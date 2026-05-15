package org.example.backend.modules.identity.services;

import lombok.RequiredArgsConstructor;
import org.example.backend.modules.identity.entity.TokenBlacklist;
import org.example.backend.modules.identity.repository.TokenBlacklistRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository repository;

    public void blacklist(
            UUID jti,
            UUID userId,
            Instant expiredAt
    ) {

        TokenBlacklist tokenBlacklist = TokenBlacklist.builder()
                .jti(jti)
                .userId(userId)
                .expiredAt(expiredAt)
                .createdAt(Instant.now())
                .build();

        repository.save(tokenBlacklist);
    }

    public boolean isBlacklisted(UUID jti) {
        return repository.existsById(jti);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        repository.deleteByExpiredAtBefore(Instant.now());
    }
}