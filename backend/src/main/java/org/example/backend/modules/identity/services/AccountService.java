package org.example.backend.modules.identity.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.common.exception.ElementAlreadyExistsException;
import org.example.backend.modules.identity.common.enums.EkycStatus;
import org.example.backend.modules.identity.common.enums.LoginType;
import org.example.backend.modules.identity.common.enums.RoleName;
import org.example.backend.modules.identity.common.utils.OtpUtils;
import org.example.backend.modules.identity.dto.request.SignupAdminRequest;
import org.example.backend.modules.identity.dto.request.SignupUserRequest;
import org.example.backend.modules.identity.dto.response.CommandResponse;
import org.example.backend.modules.identity.entity.*;
import org.example.backend.modules.identity.repository.AccountRepository;
import org.example.backend.modules.identity.repository.PendingUserRepository;
import org.example.backend.modules.identity.repository.ResetPasswordSessionRepo;
import org.example.backend.modules.identity.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PendingUserRepository pendingUserRepository;
    private final OtpUtils otpUtils;
    private final ResetPasswordSessionRepo sessionRepository;


    @Transactional
    public ApiResponse<String> initSignupUser(SignupUserRequest request) {
        // Kiểm tra xem số điện thoại đã là thành viên chính thức chưa
        if (accountRepository.existsByLoginKeyIgnoreCase(request.getLoginKey())) {
            throw new ElementAlreadyExistsException("Account existed!");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password is not matching");
        }

        // Sinh OTP ngẫu nhiên 6 số
        String otpCode = otpUtils.createOtp();

        // Tìm xem thằng này có đang đăng ký dở không, có thì ghi đè, không thì tạo mới
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
        account.setPasswordHash(pendingUser.getHashedPassword()); // Không hash lại, lấy luôn từ Pending
        account.setLoginType(LoginType.PHONE);
        account.setStatus("ACTIVE");

        Role userRole = roleRepository.findByName(RoleName.USER).orElseThrow();
        account.setRole(userRole);

        User user = new User();
        user.setFullName(pendingUser.getFullName()); // Mày cần thêm FullName vào PendingUser nếu muốn lấy đúng tên
        user.setPhone(pendingUser.getPhoneNumber());
        user.setEkycStatus(EkycStatus.UNVERIFIED);
        user.setAccount(account);
        account.setUser(user);

        accountRepository.save(account);

        // 5. Xóa dữ liệu tạm đi
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
        account.setStatus("ACTIVE");

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
}