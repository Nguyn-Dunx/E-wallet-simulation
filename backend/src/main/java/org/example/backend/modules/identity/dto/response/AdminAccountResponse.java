package org.example.backend.modules.identity.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.modules.identity.common.enums.AccountStatus;
import org.example.backend.modules.identity.common.enums.LoginType;
import org.example.backend.modules.identity.common.enums.RoleName;
import org.example.backend.modules.identity.entity.Account;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class AdminAccountResponse {
    private UUID id;
    private String loginKey;
    private LoginType loginType;
    private RoleName role;
    private AccountStatus status;
    private String displayName;
    private String phone;
    private String employeeCode;
    private Integer tokenVersion;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public static AdminAccountResponse from(Account account) {
        String displayName = account.getLoginKey();
        String phone = null;
        String employeeCode = null;

        if (account.getUser() != null) {
            displayName = account.getUser().getFullName();
            phone = account.getUser().getPhone();
        }

        if (account.getAdmin() != null) {
            displayName = account.getAdmin().getEmployeeCode();
            employeeCode = account.getAdmin().getEmployeeCode();
        }

        return AdminAccountResponse.builder()
                .id(account.getId())
                .loginKey(account.getLoginKey())
                .loginType(account.getLoginType())
                .role(account.getRole().getName())
                .status(account.getStatus())
                .displayName(displayName)
                .phone(phone)
                .employeeCode(employeeCode)
                .tokenVersion(account.getTokenVersion())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .deletedAt(account.getDeletedAt())
                .build();
    }
}
