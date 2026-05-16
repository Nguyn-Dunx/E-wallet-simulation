package org.example.backend.modules.identity.repository;

import org.example.backend.modules.identity.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;


@Repository
public interface TokenBlacklistRepository
        extends JpaRepository<TokenBlacklist, UUID> {

    void deleteByExpiredAtBefore(Instant now);
}