package org.example.backend.modules.identity.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePinRequest {

    @NotNull(message = "Old PIN is required")
    private String oldPin;

    @NotNull(message = "New PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "New PIN must be exactly 6 digits")
    private String newPin;

    @NotNull(message = "Confirm New PIN is required")
    private String confirmNewPin;
}
