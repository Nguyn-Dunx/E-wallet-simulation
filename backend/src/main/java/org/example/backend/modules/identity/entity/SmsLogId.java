package org.example.backend.modules.identity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsLogId implements Serializable {
    private UUID id;
    private Instant createdAt;
}