package org.example.backend.modules.identity.repository;

import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.entity.ResetPasswordSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResetPasswordSessionRepo
        extends JpaRepository<ResetPasswordSession, UUID> {
    Optional<ResetPasswordSession> findByToken(String token);

    @Modifying
    @Transactional
    void deleteByAccount(Account account);
}
