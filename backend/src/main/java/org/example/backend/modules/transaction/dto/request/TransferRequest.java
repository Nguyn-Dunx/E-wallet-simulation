package org.example.backend.modules.transaction.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransferRequest {

    @NotNull(message = "Receiver Wallet ID is required")
    private UUID receiverWalletId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.0", message = "Minimum transfer amount is 1,000 VND")
    private BigDecimal amount;

    private String description;

}
