package org.example.backend.modules.identity.dto.request;

import lombok.Data;
import org.example.backend.modules.identity.ultils.PhoneValidator.ValidPhoneNumber;

@Data
public class ResetPasswordRequest {

    @ValidPhoneNumber
    private String phone;
}
