package org.example.backend.modules.identity.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.common.utils.JwtUtils;
import org.example.backend.modules.identity.common.enums.LoginType;
import org.example.backend.modules.identity.dto.request.ChangePasswordRequest;
import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.repository.AccountRepository;
import org.example.backend.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@Getter
@Setter
@AllArgsConstructor
public class AdminService {

    private final AccountRepository accountRepository;

    private final JwtUtils jwtUtils;

    private final PasswordEncoder passwordEncoder;

    private final TokenBlacklistService tokenBlacklistService;

    @Transactional(rollbackOn = Exception.class)
    public ApiResponse<String> changeAdminPassword(ChangePasswordRequest request, String token) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) auth.getPrincipal();

        Account account = accountRepository.findAuthAccount(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getLoginType() != LoginType.EMPLOYEE_CODE || account.getAdmin() == null) {
            throw new RuntimeException("Invalid account or missing Admin privileges");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPasswordHash())) {
            throw new RuntimeException("Incorrect current password");
        }

        if (passwordEncoder.matches(request.getNewPassword(), account.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as the current password");
        }

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        int currentVersion = account.getTokenVersion();
        account.setTokenVersion(currentVersion + 1);

        accountRepository.save(account);

        UUID jti = jwtUtils.getJti(token);
        if (jti != null) {
            tokenBlacklistService.blacklist(jti, account.getAdmin().getId(), null);
        }

        return ApiResponse.success(HttpStatus.OK, "Change password successfully. Please re-login", null);
    }
}
