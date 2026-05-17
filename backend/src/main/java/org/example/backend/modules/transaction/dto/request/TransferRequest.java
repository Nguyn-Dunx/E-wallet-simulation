package org.example.backend.modules.transaction.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransferRequest {

    @NotNull(message = "Receiver phone number is required")
    private String receiverLoginKey;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.0", message = "Minimum transfer amount is 1,000 VND")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "PIN must be exactly 6 digits")
    private String pin;

}
