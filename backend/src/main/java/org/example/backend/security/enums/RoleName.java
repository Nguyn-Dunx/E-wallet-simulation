<<<<<<<< HEAD:backend/src/main/java/org/example/backend/security/enums/RoleName.java
package org.example.backend.security.enums;
========
package org.example.backend.modules.identity.common;
>>>>>>>> e5cbe51a1013d5f92df64cf259fa8317213943b8:backend/src/main/java/org/example/backend/modules/identity/common/RoleName.java

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
