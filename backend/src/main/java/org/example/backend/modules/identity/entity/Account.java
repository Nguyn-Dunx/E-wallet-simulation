package org.example.backend.modules.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.common.BaseEntity;
import org.example.backend.modules.identity.common.LoginType;

@Entity
@Table(name = "accounts", schema = "public")
@Getter
@Setter
public class Account extends BaseEntity {
    @Column(name = "login_key", nullable = false)
    private String loginKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "login_failed_count", nullable = false)
    private int loginFailedCount;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "account")
    private User user;

    @OneToOne(mappedBy = "account")
    private Admin admin;

}
