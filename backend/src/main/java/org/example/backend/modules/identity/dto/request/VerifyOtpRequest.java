package org.example.backend.modules.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
    @NotBlank
    private String phone;

    @NotBlank
    private String otp;
}