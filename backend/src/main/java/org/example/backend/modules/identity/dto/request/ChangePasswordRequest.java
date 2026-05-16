package org.example.backend.modules.identity.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @Size(min = 6, max = 20)
    private String newPassword;

    @Size(min = 6, max = 20)
    private String oldPassword;
}
