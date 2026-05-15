package org.example.backend.modules.transaction.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.modules.transaction.dto.request.DepositRequest;
import org.example.backend.modules.transaction.dto.request.TransferRequest;
import org.example.backend.modules.transaction.dto.request.WithdrawRequest;
import org.example.backend.modules.transaction.dto.response.TransactionResponse;
import org.example.backend.modules.transaction.entity.Transaction;
import org.example.backend.modules.transaction.enums.TransStatus;
import org.example.backend.modules.transaction.enums.TransType;
import org.example.backend.modules.transaction.mapper.TransactionMapper;
import org.example.backend.modules.transaction.repository.TransactionRepository;
import org.example.backend.modules.wallet.entity.LinkedSource;
import org.example.backend.modules.wallet.entity.Wallet;
import org.example.backend.modules.wallet.repository.LinkedSourceRepository;
import org.example.backend.modules.wallet.service.internal.WalletInternalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    final TransactionRepository transactionRepository;
    final TransactionMapper transactionMapper;
    final LinkedSourceRepository linkedSourceRepository;

    private final WalletInternalService walletInternalService;

    @Override
    @Transactional
    public TransactionResponse transfer(UUID senderUserId, TransferRequest request) {
        log.info("Initiating transfer from User: {} to Wallet: {}", senderUserId, request.getReceiverWalletId());

        // check sender
        if (senderUserId == null) {
            throw new RuntimeException("Authentication error: Sender information not found!");
        }
        Wallet senderWallet = walletInternalService.getWalletEntityByUserId(senderUserId);
        if (senderWallet.getId().equals(request.getReceiverWalletId())) {
            throw new RuntimeException("You cannot transfer money to yourself!");
        }

        // check receiver
        Wallet receiverWallet = walletInternalService.getWalletEntityById(request.getReceiverWalletId());

        // 3. Khởi tạo bản ghi Giao dịch với trạng thái PENDING
        Transaction transaction = Transaction.builder()
                .senderWalletId(senderWallet.getId())
                .receiverWalletId(receiverWallet.getId())
                .amount(request.getAmount())
                .type(TransType.TRANSFER)
                .status(TransStatus.PENDING)
                .description(request.getDescription())
                .build();

        transaction = transactionRepository.save(transaction); // Lưu mã giao dịch PENDING

        try {
            // 4. THỰC HIỆN TRỪ TIỀN (sender)
            walletInternalService.withdraw(senderWallet.getId(), request.getAmount());

            // 5. THỰC HIỆN CỘNG TIỀN (receiver)
            walletInternalService.deposit(receiverWallet.getId(), request.getAmount());

            // 6. Nếu cả 2 bước trên không quăng lỗi, cập nhật trạng thái SUCCESS
            transaction.setStatus(TransStatus.SUCCESS);
            transactionRepository.save(transaction);

            log.info("Transfer successful. TXN Code: {}", transaction.getTransactionCode());

            return transactionMapper.toResponse(transaction);

        } catch (Exception e) {
            log.error("Transfer failed for TXN: {}. Reason: {}", transaction.getTransactionCode(), e.getMessage());

            throw new RuntimeException("Money transfer failed: " + e.getMessage());
        }
    }

    @Override
    public Page<TransactionResponse> getMyTransactionHistory(UUID userId, Pageable pageable) {

        Wallet wallet = walletInternalService.getWalletEntityByUserId(userId);

        return transactionRepository
                .getTransactionHistoryByWalletId(wallet.getId(), pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    public Page<TransactionResponse> getMyTransactionHistory(UUID userId, Instant startDate, Instant endDate, Pageable pageable) {

        // 1. Từ userId, lấy ra cái ví tương ứng
        Wallet wallet = walletInternalService.getWalletEntityByUserId(userId);

        Instant finalStartDate = (startDate != null) ? startDate : Instant.EPOCH;

        Instant finalEndDate = (endDate != null) ? endDate : Instant.parse("2999-12-31T23:59:59Z");

        Page<Transaction> transactions = transactionRepository.getHistoryWithFilters(
                wallet.getId(),
                finalStartDate,
                finalEndDate,
                pageable
        );


        // 3. Map sang DTO và trả về
        return transactions.map(transactionMapper::toResponse);
    }

    @Override
    public TransactionResponse getTransactionDetail(UUID userId, String txnCode) {
        Wallet wallet = walletInternalService.getWalletEntityByUserId(userId);

        Transaction txn = transactionRepository.findByTransactionCode(txnCode)
                .orElseThrow(() -> new RuntimeException("No transactions found"));

        // Bảo mật: Chỉ cho phép người gửi hoặc người nhận xem chi tiết

        boolean isSender = wallet.getId().equals(txn.getSenderWalletId());
        boolean isReceiver = wallet.getId().equals(txn.getReceiverWalletId());

        if (!isSender && !isReceiver) {
            throw new RuntimeException("You do not have permission to view this transaction");
        }

        return transactionMapper.toResponse(txn);
    }

    @Override
    public TransactionResponse deposit(UUID userId, DepositRequest request) {
        Wallet wallet = walletInternalService.getWalletEntityByUserId(userId);

        // 1. Kiểm tra tính hợp lệ của nguồn tiền
        validateLinkedSource(wallet.getId(), request.getSourceId());

        // 2. Tạo giao dịch PENDING
        Transaction transaction = Transaction.builder()
                .receiverWalletId(wallet.getId())
                .senderWalletId(null) // Nạp tiền thì không có người gửi trong hệ thống
                .amount(request.getAmount())
                .type(TransType.DEPOSIT)
                .status(TransStatus.PENDING)
                .description(request.getDescription())
                .build();
        transaction = transactionRepository.save(transaction);

        // 3. Thực hiện cộng tiền vào ví
        walletInternalService.deposit(wallet.getId(), request.getAmount());

        // 4. Hoàn tất
        transaction.setStatus(TransStatus.SUCCESS);
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    public TransactionResponse withdraw(UUID userId, WithdrawRequest request) {
        Wallet wallet = walletInternalService.getWalletEntityByUserId(userId);

        validateLinkedSource(wallet.getId(), request.getSourceId());

        Transaction transaction = Transaction.builder()
                .senderWalletId(wallet.getId())
                .receiverWalletId(null) // Rút tiền thì không có người nhận trong hệ thống
                .amount(request.getAmount())
                .type(TransType.WITHDRAW)
                .status(TransStatus.PENDING)
                .description(request.getDescription())
                .build();
        transaction = transactionRepository.save(transaction);

        // Thực hiện trừ tiền ví (Hàm này đã có logic check số dư)
        walletInternalService.withdraw(wallet.getId(), request.getAmount());

        transaction.setStatus(TransStatus.SUCCESS);
        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    private void validateLinkedSource(UUID walletId, UUID sourceId) {
        LinkedSource source = linkedSourceRepository.findById(sourceId)
                .orElseThrow(() -> new RuntimeException("The Linked Source do not exist"));
        if (!source.getWalletId().equals(walletId)) {
            throw new RuntimeException("Linked Source that do not belong to you");
        }
    }
}
