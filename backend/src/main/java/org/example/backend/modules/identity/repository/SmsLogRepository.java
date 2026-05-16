package org.example.backend.modules.identity.repository;

import org.example.backend.modules.identity.common.enums.SmsLogType;
import org.example.backend.modules.identity.entity.SmsLog;
import org.example.backend.modules.identity.entity.SmsLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, SmsLogId> {
    @Query(value = "SELECT s.phone, s.type, s.otp_code, s.expired_at FROM identity.sms_logs s " +
            "WHERE s.phone = :phone AND s.type = :type " +
            "ORDER BY s.created_at DESC LIMIT 1",
            nativeQuery = true)
    Optional<SmsLog> findSmsLog(@Param("phone") String phone, @Param("type") SmsLogType type);
}
