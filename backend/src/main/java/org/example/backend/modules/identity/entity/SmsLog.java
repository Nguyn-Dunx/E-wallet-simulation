package org.example.backend.modules.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.modules.identity.common.enums.SmsLogStatus;
import org.example.backend.modules.identity.common.enums.SmsLogType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sms_logs", schema = "identity")
@IdClass(SmsLogId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Id
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SmsLogType type;

    @Column(name = "otp_code", nullable = false, length = 20)
    private String otpCode;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SmsLogStatus status;

    @Column(name = "expired_at")
    private Instant expiredAt;

    // Tự động gán giá trị mặc định trước khi insert xuống DB nếu code chưa set
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = SmsLogStatus.PENDING;
        }
        this.expiredAt = this.createdAt.plusSeconds(300);
    }
}