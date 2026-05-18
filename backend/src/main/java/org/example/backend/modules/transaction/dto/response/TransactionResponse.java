package org.example.backend.modules.transaction.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class TransactionResponse {
    private String transactionCode;
    private UUID senderWalletId;
    private UUID receiverWalletId;
    private BigDecimal amount;
    private BigDecimal fee;
    private String type;
    private String status;
    private String description;
    private Instant createdAt;

    private String direction;
    private String senderAccountNumber;
    private String senderAccountName;
    private String receiverAccountNumber;
    private String receiverAccountName;
}
