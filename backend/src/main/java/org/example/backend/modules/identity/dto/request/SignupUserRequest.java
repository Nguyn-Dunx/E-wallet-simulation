package org.example.backend.modules.identity.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupUserRequest {

    @NotBlank(message = "{validation.auth.loginKey.required}")
    private String loginKey; // phone

    @NotBlank(message = "{validation.auth.password.required}")
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank(message = "{validation.user.fullName.required}")
    private String fullName;
}