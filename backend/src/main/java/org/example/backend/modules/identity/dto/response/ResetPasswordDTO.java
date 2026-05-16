package org.example.backend.modules.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetPasswordDTO {
    private String otpCode;
}
