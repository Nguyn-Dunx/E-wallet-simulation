package org.example.backend.modules.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupAdminRequest {

    @NotBlank(message = "{validation.auth.loginKey.required}")
    private String loginKey;

    @NotBlank(message = "{validation.auth.password.required}")
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank(message = "{validation.admin.employeeCode.required}")
    private String employeeCode;
}