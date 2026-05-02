package org.example.backend.security.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum  RoleName {
    USER(1),
    ADMIN(2);

    private final int id;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
