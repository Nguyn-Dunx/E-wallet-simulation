package org.example.backend.modules.wallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.example.backend.common.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "linked_sources", schema = "wallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkedSource extends BaseEntity {

    // Tham chiếu đến ví (Lưu ID trực tiếp để tối ưu truy vấn, tránh N+1 Query nếu không cần thiết)
    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(name = "bank_name", length = 50, nullable = false)
    private String bankName;

    @Column(name = "account_number", length = 20, nullable = false)
    private String accountNumber;

    @Column(length = 20)
    private String status; // VERIFIED, UNVERIFIED, DISCONNECTED


    @Override
    protected void prePersist() {
        super.prePersist(); // Bắt buộc gọi để BaseEntity tạo createdAt

        if (this.status == null) {
            this.status = "VERIFIED";
        }
    }

}
