package org.example.backend.modules.identity.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmResetPasswordRequest {
    private String token;
    private String newPassword;
}