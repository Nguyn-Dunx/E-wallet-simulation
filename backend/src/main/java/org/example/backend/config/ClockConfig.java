package org.example.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Configuration
@Slf4j
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public CommandLineRunner verifyClockOnStartup(Clock clock) {
        return args -> {
            log.info("=====================================================");
            log.info("VN Time: {}",
                    ZonedDateTime.now(ZoneId.of("Asia/Bangkok")));
            log.info("=====================================================");
        };
    }
}
