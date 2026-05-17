package org.example.backend.modules.identity.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SetPinRequest {

    @NotNull(message = "PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "PIN must be exactly 6 digits")
    private String pin;

    @NotNull(message = "Confirm PIN is required")
    private String confirmPin;
}
