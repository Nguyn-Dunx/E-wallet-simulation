package org.example.backend.modules.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkBankRequest {
    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;
}
