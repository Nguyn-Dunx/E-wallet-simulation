package org.example.backend.modules.identity.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.modules.identity.common.enums.LoginType;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class JwtResponse {

    @Builder.Default
    private String type = "Bearer";

    private String token;

    private UUID id;

    private String loginKey;

    private String displayName;

    private LoginType loginType;

    private List<String> roles;
}
