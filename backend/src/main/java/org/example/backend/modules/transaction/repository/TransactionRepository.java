package org.example.backend.modules.transaction.repository;

import org.example.backend.modules.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByTransactionCode(String transactionCode);

    // sao ke transaction 1 wallet
    @Query("SELECT t FROM Transaction t " +
            "WHERE (t.senderWalletId = :walletId OR t.receiverWalletId = :walletId) " +
                    "AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    Page<Transaction> getTransactionHistoryByWalletId(@Param("walletId") UUID walletId, Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
            "WHERE (t.senderWalletId = :walletId OR t.receiverWalletId = :walletId) " +
            "AND t.createdAt >= :startDate " +
            "AND t.createdAt <= :endDate " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> getHistoryWithFilters(
            @Param("walletId") UUID walletId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );
}
