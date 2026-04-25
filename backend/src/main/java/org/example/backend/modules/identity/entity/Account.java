package org.example.backend.modules.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts", schema = "public")
@Getter
@Setter
public class Account extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "pin_hash")
    private String pinHash;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "login_failed_count", nullable = false)
    private int loginFailedCount;

    @Column(name = "password_change_at")
    private Instant passwordChangeAt;

}
