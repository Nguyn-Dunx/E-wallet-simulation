package org.example.backend.modules.identity.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "pending_users",
        indexes = {
                @Index(
                        name = "idx_pending_users_expired_at",
                        columnList = "expired_at"
                )
        },
        schema = "identity"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingUser {

    @Id
    @Column(name = "phone_number", length = 15, nullable = false)
    private String phoneNumber;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
}