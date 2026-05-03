package org.example.backend.modules.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.common.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets", schema = "wallet") // Trỏ chuẩn xác vào schema wallet
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(length = 5)
    private String currency;

    @Column(length = 20)
    private String status;

    @Version // chống Race Condition khi transaction trừ tiền
    private Integer version;

    @Override
    public void prePersist() {
        super.prePersist();

        if (this.balance == null) this.balance = BigDecimal.ZERO;
        if (this.currency == null) this.currency = "VND";
        if (this.status == null) this.status = "ACTIVE";
    }

}
