package org.example.backend.modules.wallet.repository;

import org.example.backend.modules.wallet.entity.LinkedSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LinkedSourceRepository extends JpaRepository<LinkedSource, UUID> {

    // Note: AndDeletedAtIsNull vì co field deleteAt

    //Lấy danh sách các ngân hàng đã liên kết của một ví
    List<LinkedSource> findByWalletIdAndDeletedAtIsNull(UUID walletId);

    // check  một tk bank cụ thể đã dc liên kết với ví này chưa
    boolean existsByWalletIdAndAccountNumberAndBankNameAndDeletedAtIsNull(
            UUID walletId, String accountNumber, String bankName);
}
