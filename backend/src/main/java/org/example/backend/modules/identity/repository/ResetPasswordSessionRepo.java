package org.example.backend.modules.identity.repository;

import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.entity.ResetPasswordSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface ResetPasswordSessionRepo
        extends JpaRepository<ResetPasswordSession, BigInteger> {
    Optional<ResetPasswordSession> findByToken(String token);
    void deleteByAccount(Account account);
}
