package org.example.backend.modules.transaction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.common.utils.SecurityUtils;
import org.example.backend.modules.transaction.dto.request.DepositRequest;
import org.example.backend.modules.transaction.dto.request.TransferRequest;
import org.example.backend.modules.transaction.dto.request.WithdrawRequest;
import org.example.backend.modules.transaction.dto.response.TransactionResponse;
import org.example.backend.modules.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transferMoney(@RequestBody @Valid TransferRequest request) {
        UUID userId = securityUtils.getCurrentUserId();

        TransactionResponse data = transactionService.transfer(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Transfer successful!", data));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getHistory(
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        UUID userId = securityUtils.getCurrentUserId();

        Page<TransactionResponse> data;

        if (startDate != null || endDate != null) {
            data = transactionService.getMyTransactionHistory(userId, startDate, endDate, pageable);
        } else {
            data = transactionService.getMyTransactionHistory(userId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success("Retrieve the transaction history!", data));
    }

    @GetMapping("/{txnCode}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getDetail(@PathVariable String txnCode) {
        UUID userId = securityUtils.getCurrentUserId();

        TransactionResponse data = transactionService.getTransactionDetail(userId, txnCode);

        return ResponseEntity.ok(ApiResponse.success("Retrieve transaction details successfully!", data));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @RequestBody @Valid DepositRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("successfully deposited!",
                transactionService.deposit(userId, request)));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @RequestBody @Valid WithdrawRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("successfully withdrawal!",
                transactionService.withdraw(userId, request)));
    }
}
