package org.example.backend.modules.identity.repository;

import org.example.backend.modules.identity.common.enums.SmsLogType;
import org.example.backend.modules.identity.entity.SmsLog;
import org.example.backend.modules.identity.entity.SmsLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, SmsLogId> {
    Optional<SmsLog> findFirstByPhoneAndTypeOrderByCreatedAtDesc(String phone, SmsLogType type);
}

