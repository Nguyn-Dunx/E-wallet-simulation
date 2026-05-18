package org.example.backend.modules.identity.services;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.common.utils.JwtUtils;
import org.example.backend.modules.identity.common.enums.AccountStatus;
import org.example.backend.modules.identity.common.enums.LoginType;
import org.example.backend.modules.identity.common.enums.RoleName;
import org.example.backend.modules.identity.dto.request.ChangePasswordRequest;
import org.example.backend.modules.identity.dto.response.AdminAccountResponse;
import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.repository.AccountRepository;
import org.example.backend.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    public Page<AdminAccountResponse> getAccounts(
            String keyword,
            AccountStatus status,
            RoleName role,
            Pageable pageable
    ) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();

        return accountRepository
                .findAll((root, query, cb) -> {
                    Join<Object, Object> roleJoin = root.join("role", JoinType.INNER);
                    Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
                    Join<Object, Object> adminJoin = root.join("admin", JoinType.LEFT);
                    List<Predicate> predicates = new ArrayList<>();

                    if (status != null) {
                        predicates.add(cb.equal(root.get("status"), status));
                    }

                    if (role != null) {
                        predicates.add(cb.equal(roleJoin.get("name"), role));
                    }

                    if (!normalizedKeyword.isBlank()) {
                        String pattern = "%" + normalizedKeyword + "%";
                        predicates.add(cb.or(
                                cb.like(cb.lower(root.get("loginKey")), pattern),
                                cb.like(cb.lower(cb.coalesce(userJoin.get("fullName"), "")), pattern),
                                cb.like(cb.lower(cb.coalesce(userJoin.get("phone"), "")), pattern),
                                cb.like(cb.lower(cb.coalesce(adminJoin.get("employeeCode"), "")), pattern)
                        ));
                    }

                    return cb.and(predicates.toArray(Predicate[]::new));
                }, pageable)
                .map(AdminAccountResponse::from);
    }

    @Transactional(rollbackOn = Exception.class)
    public ApiResponse<AdminAccountResponse> lockAccount(UUID accountId) {
        Account account = getManageableUserAccount(accountId);
        account.setStatus(AccountStatus.LOCKED);
        account.setTokenVersion(account.getTokenVersion() + 1);
        account.setUpdatedAt(Instant.now());

        Account saved = accountRepository.save(account);
        return ApiResponse.success(HttpStatus.OK, "Locked account", AdminAccountResponse.from(saved));
    }

    @Transactional(rollbackOn = Exception.class)
    public ApiResponse<AdminAccountResponse> unlockAccount(UUID accountId) {
        Account account = getManageableUserAccount(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(Instant.now());

        Account saved = accountRepository.save(account);
        return ApiResponse.success(HttpStatus.OK, "Unlocked account", AdminAccountResponse.from(saved));
    }

    @Transactional(rollbackOn = Exception.class)
    public ApiResponse<AdminAccountResponse> deleteAccount(UUID accountId) {
        Account account = getManageableUserAccount(accountId);
        account.setStatus(AccountStatus.DISABLED);
        account.setTokenVersion(account.getTokenVersion() + 1);
        Instant deletedAt = Instant.now();
        account.setDeletedAt(deletedAt);
        if (account.getUser() != null) {
            account.getUser().setDeletedAt(deletedAt);
        }

        Account saved = accountRepository.save(account);
        return ApiResponse.success(HttpStatus.OK, "Deleted account", AdminAccountResponse.from(saved));
    }

    private Account getManageableUserAccount(UUID accountId) {
        UUID currentAdminId = getCurrentAdminId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getId().equals(currentAdminId)) {
            throw new RuntimeException("Cannot modify your own admin account");
        }

        if (account.getRole().getName() == RoleName.ADMIN) {
            throw new RuntimeException("Cannot modify admin accounts from this panel");
        }

        return account;
    }

    private UUID getCurrentAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) auth.getPrincipal();
        return currentUser.getId();
    }

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
