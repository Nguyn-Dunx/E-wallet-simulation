package org.example.backend.modules.transaction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.dto.ApiResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transferMoney(@RequestBody @Valid TransferRequest request) {
        // [MOCK] Lấy ID của User đang đăng nhập (Người gửi)
        UUID mockSenderUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        TransactionResponse data = transactionService.transfer(mockSenderUserId, request);

        return ResponseEntity.ok(ApiResponse.success("Transfer successful!", data));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getHistory(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        Page<TransactionResponse> data = transactionService.getMyTransactionHistory(mockUserId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Retrieve the transaction history!", data));
    }

    @GetMapping("/{txnCode}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getDetail(@PathVariable String txnCode) {
        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        TransactionResponse data = transactionService.getTransactionDetail(mockUserId, txnCode);

        return ResponseEntity.ok(ApiResponse.success("Retrieve details of a successful transaction!", data));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @RequestBody @Valid DepositRequest request) {
        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        return ResponseEntity.ok(ApiResponse.success("successfully deposited!",
                transactionService.deposit(mockUserId, request)));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @RequestBody @Valid WithdrawRequest request) {
        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        return ResponseEntity.ok(ApiResponse.success("successfully withdrawal!",
                transactionService.withdraw(mockUserId, request)));
    }
}
