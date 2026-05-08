package org.example.backend.modules.identity.dto.request;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank(message = "{validation.auth.loginKey.required}")
    @Size(min = 3, max = 50, message = "{validation.auth.loginKey.length}")
    private String loginKey;

    @NotBlank(message = "{validation.auth.password.required}")
    @Size(min = 6, max = 100, message = "{validation.auth.password.length}")
    private String password;

    private String loginType;
}
