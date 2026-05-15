package org.example.backend.modules.identity.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsPartitionManager {
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 23 * * *")
    public void manageSmsPartitions() {
        log.info("Starting automated partition management for sms_logs...");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        createPartition(tomorrow);

        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        dropPartition(threeDaysAgo);

        log.info("Partition management process completed.");
    }

    @PostConstruct
    public void initializerPartition() {
        createPartition(LocalDate.now());
    }

    private void createPartition(LocalDate targetDate) {
        String suffix = targetDate.format(DateTimeFormatter.ofPattern("'y'yyyy'_m'MM'_d'dd"));
        String partitionName = "identity.sms_logs_" + suffix;

        String fromDate = targetDate + " 00:00:00+07";
        String toDate = targetDate.plusDays(1) + " 00:00:00+07";

        String sql = String.format(
                "CREATE TABLE IF NOT EXISTS %s PARTITION OF identity.sms_logs FOR VALUES FROM ('%s') TO ('%s');",
                partitionName, fromDate, toDate
        );

        try {
            jdbcTemplate.execute(sql);
            log.info("Checked/Created partition: {}", partitionName);
        } catch (Exception e) {
            log.error("DDL error while creating partition {}: {}", partitionName, e.getMessage());
        }
    }



    private void dropPartition(LocalDate targetDate) {
        String suffix = targetDate.format(DateTimeFormatter.ofPattern("'y'yyyy'_m'MM'_d'dd"));
        String partitionName = "identity.sms_logs_" + suffix;

        String sql = String.format("DROP TABLE IF EXISTS %s;", partitionName);

        try {
            jdbcTemplate.execute(sql);
            log.info("Successfully dropped old partition: {}", partitionName);
        } catch (Exception e) {
            log.error("Error while dropping partition {}: {}", partitionName, e.getMessage());
        }
    }
}
