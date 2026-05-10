package org.example.backend.modules.transaction.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WithdrawRequest {
    @NotNull
    private UUID sourceId;
    @NotNull @DecimalMin("1000.0") private BigDecimal amount;
    private String description;
}
