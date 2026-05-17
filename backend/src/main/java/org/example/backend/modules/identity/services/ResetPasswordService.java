package org.example.backend.modules.identity.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.modules.identity.common.enums.SmsLogType;
import org.example.backend.modules.identity.common.utils.OtpUtils;
import org.example.backend.modules.identity.dto.response.ResetPasswordDTO;
import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.entity.ResetPasswordSession;
import org.example.backend.modules.identity.entity.SmsLog;
import org.example.backend.modules.identity.entity.User;
import org.example.backend.modules.identity.repository.ResetPasswordSessionRepo;
import org.example.backend.modules.identity.repository.SmsLogRepository;
import org.example.backend.modules.identity.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResetPasswordService {

    private final SmsLogRepository smsLogRepository;
    private final UserRepository userRepository;
    private final ResetPasswordSessionRepo sessionRepository;
    private final OtpUtils otpUtils;

    public ApiResponse<ResetPasswordDTO> createOtp(String phone) {
        User user =  userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Not found phone number"));
        String otpCode = otpUtils.createOtp();
        SmsLog smsLog = SmsLog.builder().phone(phone).otpCode(otpCode).type(SmsLogType.CHANGE_PASSWORD).build();
        smsLogRepository.save(smsLog);
        return ApiResponse.success(
                HttpStatus.CONTINUE,
                "Sent otp to your phone numbers",
                new ResetPasswordDTO(otpCode)
        );
    }

    public ApiResponse<String> verifyAndGenerateToken(String phone, String otpCode) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Invalid demanded phone number"));
        SmsLog smsLog = smsLogRepository.findFirstByPhoneAndTypeOrderByCreatedAtDesc(phone, SmsLogType.CHANGE_PASSWORD)
                .orElseThrow(() -> new IllegalArgumentException("Not found any otp for this request"));
        if (!smsLog.getOtpCode().equals(otpCode)) {
            throw new IllegalArgumentException("Invalid otp code");
        }
        if (smsLog.getExpiredAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Otp expired");
        }
        String token = generateResetToken(user);
        return ApiResponse.success(HttpStatus.CREATED, "Verified Otp", token);
    }

    private String generateResetToken(User user) {
        // 1. Tìm account từ phone (vì Account và User chung ID)
        Account account = user.getAccount();

        // 2. Dọn dẹp session cũ của account này
        sessionRepository.deleteByAccount(account);

        // 3. Tạo token mới
        String token = UUID.randomUUID().toString();
        ResetPasswordSession session = new ResetPasswordSession();
        session.setAccount(account);
        session.setToken(token);
        session.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // Hiệu lực ngắn

        sessionRepository.save(session);
        return token;
    }
}
