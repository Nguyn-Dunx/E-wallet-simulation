package org.example.backend.modules.identity.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.example.backend.common.event.UserRegisteredEvent;

import org.example.backend.common.dto.ApiResponse;
import org.example.backend.common.exception.ElementAlreadyExistsException;
import org.example.backend.common.utils.JwtUtils;
import org.example.backend.modules.identity.common.enums.AccountStatus;
import org.example.backend.modules.identity.common.enums.EkycStatus;
import org.example.backend.modules.identity.common.enums.LoginType;
import org.example.backend.modules.identity.common.enums.RoleName;
import org.example.backend.modules.identity.common.utils.OtpUtils;
import org.example.backend.modules.identity.dto.request.*;
import org.example.backend.modules.identity.dto.response.CommandResponse;

import org.springframework.context.ApplicationEventPublisher;

import org.example.backend.modules.identity.entity.*;
import org.example.backend.modules.identity.repository.*;
import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PendingUserRepository pendingUserRepository;
    private final OtpUtils otpUtils;
    private final ResetPasswordSessionRepo sessionRepository;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;


    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ApiResponse<String> initSignupUser(SignupUserRequest request) {
        if (accountRepository.existsByLoginKeyIgnoreCase(request.getLoginKey())) {
            throw new ElementAlreadyExistsException("Account existed!");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password is not matching");
        }

        String otpCode = otpUtils.createOtp();

        PendingUser pendingUser = pendingUserRepository.findById(request.getLoginKey())
                .orElse(new PendingUser());

        pendingUser.setPhoneNumber(request.getLoginKey());
        pendingUser.setHashedPassword(passwordEncoder.encode(request.getPassword()));
        pendingUser.setFullName(request.getFullName());
        pendingUser.setOtpCode(otpCode);
        pendingUser.setCreatedAt(LocalDateTime.now());
        pendingUser.setExpiredAt(LocalDateTime.now().plusMinutes(5)); // Hết hạn sau 5 phút

        pendingUserRepository.save(pendingUser);


        return ApiResponse.success(HttpStatus.CREATED, "Sending otp", otpCode);
    }

    // Bước 2: Xác thực OTP và lưu vào DB chính thức
    @Transactional
    public ApiResponse<String> verifyAndSignupUser(String phone, String otp) {
        // 1. Tìm thông tin tạm
        PendingUser pendingUser = pendingUserRepository.findById(phone)
                .orElseThrow(() -> new RuntimeException("Not found any request for this phone number"));

        // 2. Kiểm tra hạn OTP
        if (pendingUser.getExpiredAt().isBefore(LocalDateTime.now())) {
            pendingUserRepository.delete(pendingUser); // Xóa rác
            throw new RuntimeException("Expired Otp, try again!");
        }

        // 3. Kiểm tra OTP có khớp không
        if (!pendingUser.getOtpCode().equals(otp)) {
            throw new RuntimeException("Invalid Otp.");
        }

        // 4. Mọi thứ hợp lệ -> Đưa vào bảng Account và User
        Account account = new Account();
        account.setLoginKey(pendingUser.getPhoneNumber());
        account.setPasswordHash(pendingUser.getHashedPassword());
        account.setLoginFailedCount(0);// Không hash lại, lấy luôn từ Pending
        account.setTokenVersion(0);
        account.setLoginType(LoginType.PHONE);
        account.setStatus(AccountStatus.ACTIVE);

        Role userRole = roleRepository.findByName(RoleName.USER).orElseThrow();
        account.setRole(userRole);

        User user = new User();
        user.setFullName(pendingUser.getFullName()); // cần thêm FullName vào PendingUser nếu muốn lấy đúng tên
        user.setPhone(pendingUser.getPhoneNumber());
        user.setEkycStatus(EkycStatus.UNVERIFIED);
        user.setAccount(account);
        account.setUser(user);

        accountRepository.save(account);
        // notify to common to create a wallet
        eventPublisher.publishEvent(new UserRegisteredEvent(account.getId()));

        pendingUserRepository.delete(pendingUser);

        return ApiResponse.success(HttpStatus.ACCEPTED, null,"Register successfully");
    }

    public CommandResponse signupAdmin(SignupAdminRequest request) {

        if (accountRepository.existsByLoginKeyIgnoreCase(request.getLoginKey())) {
            throw new ElementAlreadyExistsException("Login key already exists");
        }

        Account account = new Account();
        account.setLoginKey(request.getLoginKey());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setLoginType(LoginType.EMPLOYEE_CODE);
        account.setStatus(AccountStatus.ACTIVE);

        Admin admin = new Admin();
        admin.setEmployeeCode(request.getEmployeeCode());
        admin.setAccount(account);
        account.setAdmin(admin);

        accountRepository.save(account);

        return CommandResponse.builder()
                .message("Successfully")
                .build();
    }

    @Transactional
    public ApiResponse<String> resetUserPassword(String token, String newPassword) {
        ResetPasswordSession session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessionRepository.delete(session);
            throw new RuntimeException("Token is expired.");
        }

        Account account = session.getAccount();
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setTokenVersion(account.getTokenVersion() + 1); // Invalid các JWT cũ
        accountRepository.save(account);

        sessionRepository.delete(session);
        return ApiResponse.success(
                HttpStatus.ACCEPTED,
                "Reset password successfully",
                null
        );
    }

    @Transactional(rollbackOn = Exception.class)
    public ApiResponse<String> changeUserPassword(ChangePasswordRequest request, String loginKey , String token) {
        Account account = accountRepository.findAuthAccount(loginKey)
                .orElseThrow(() -> new RuntimeException("Not found account"));

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPasswordHash())) {
            throw new RuntimeException("Current password is not match");
        }

// 2. Kiểm tra mật khẩu mới có trùng mật khẩu cũ không
        if (passwordEncoder.matches(request.getNewPassword(), account.getPasswordHash())) {
            throw new RuntimeException("New password must be not match old password");
        }

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        UUID userId = account.getUser().getId();
        account.setTokenVersion(account.getTokenVersion() + 1);

        accountRepository.save(account);

        UUID jti = jwtUtils.getJti(token);
        if (jti != null) {
            tokenBlacklistService.blacklist(jti, userId, null);
        }

        return ApiResponse.success(HttpStatus.OK, "Change password successfully. Please re-login", null);
    }

    @Transactional
    public ApiResponse<String> deleteAccount(DeleteAccountRequest request, String token) {
        Account account = accountRepository.findAuthAccount(request.loginKey())
                .orElseThrow(() -> new RuntimeException("Not found account"));
        UUID userId = account.getUser().getId();
        account.setTokenVersion(account.getTokenVersion() + 1);
        account.setStatus(AccountStatus.DELETED);
        account.setDeletedAt(Instant.now());
        accountRepository.save(account);
        UUID jti = jwtUtils.getJti(token);
        if (jti != null) {
            tokenBlacklistService.blacklist(jti, userId, null);
        }

        return ApiResponse.success(HttpStatus.OK, "Deleted Account", null);
    }

    @Transactional
    public ApiResponse<String> lockAccount(LockAccountRequest request, String token) {
        Account account = accountRepository.findAuthAccount(request.loginKey())
                .orElseThrow(() -> new RuntimeException("Not found account"));
        UUID userId = account.getUser().getId();
        account.setTokenVersion(account.getTokenVersion() + 1);
        account.setStatus(AccountStatus.LOCKED);
        account.setUpdatedAt(Instant.now());
        accountRepository.save(account);
        UUID jti = jwtUtils.getJti(token);
        if (jti != null) {
            tokenBlacklistService.blacklist(jti, userId, null);
        }

        return ApiResponse.success(HttpStatus.OK, "Locked Account", null);
    }

    @Transactional
    public ApiResponse<String> unlockAccount(UnlockAccountRequest request) {
        Account account = accountRepository.findAuthAccount(request.loginKey())
                .orElseThrow(() -> new RuntimeException("Not found account"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(Instant.now());
        accountRepository.save(account);
        return ApiResponse.success(HttpStatus.OK, "Unlocked Account", null);
    }
}