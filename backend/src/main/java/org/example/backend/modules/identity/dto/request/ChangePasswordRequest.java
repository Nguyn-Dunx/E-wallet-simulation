package org.example.backend.modules.identity.dto.request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @Size(min = 8, max = 20)
    private String newPassword;

    @Size(min = 8, max = 20)
    private String oldPassword;
}
