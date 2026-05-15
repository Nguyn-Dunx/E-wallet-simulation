package org.example.backend.modules.transaction.service;

import org.example.backend.modules.transaction.dto.request.DepositRequest;
import org.example.backend.modules.transaction.dto.request.TransferRequest;
import org.example.backend.modules.transaction.dto.request.WithdrawRequest;
import org.example.backend.modules.transaction.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface TransactionService {
    TransactionResponse transfer(UUID senderUserId, TransferRequest request);

    // Sao kê giao dịch có phân trang
    Page<TransactionResponse> getMyTransactionHistory(UUID userId, Pageable pageable);
    Page<TransactionResponse> getMyTransactionHistory(UUID userId, Instant startDate, Instant endDate, Pageable pageable);

    // Xem chi tiết một giao dịch
    TransactionResponse getTransactionDetail(UUID userId, String txnCode);

    //nap - rut
    TransactionResponse deposit(UUID userId, DepositRequest request);
    TransactionResponse withdraw(UUID userId, WithdrawRequest request);
}
