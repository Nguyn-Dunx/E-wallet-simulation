package org.example.backend.modules.transaction.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionResponse {
    private String transactionCode;
    private BigDecimal amount;
    private BigDecimal fee;
    private String type;
    private String status;
    private String description;
    private Instant createdAt;
}
