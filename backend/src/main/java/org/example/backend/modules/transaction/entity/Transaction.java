package org.example.backend.modules.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.common.BaseEntity;
import org.example.backend.modules.transaction.enums.TransStatus;
import org.example.backend.modules.transaction.enums.TransType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "transactions", schema = "txn")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @Column(name = "transaction_code", nullable = false, unique = true, length = 50)
    private String transactionCode;

    @Column(name = "sender_wallet_id")
    private UUID senderWalletId;

    @Column(name = "receiver_wallet_id")
    private UUID receiverWalletId;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal fee;

    // Mapping  PostgreSQL Custom Enum
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "txn.trans_type")
    private TransType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "txn.trans_status")
    private TransStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;


    @Override
    protected void prePersist() {
        super.prePersist();

        if (this.fee == null) {
            this.fee = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = TransStatus.PENDING;
        }
        if (this.transactionCode == null) {
            this.transactionCode = generateTransactionCode();
        }
    }

    private String generateTransactionCode() {
        long timestamp = System.currentTimeMillis();
        int randomStr = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "TXN-" + timestamp + "-" + randomStr;
    }
}
