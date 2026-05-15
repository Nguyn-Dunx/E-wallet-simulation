package org.example.backend.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserRegisteredEvent {
    private final UUID userId;
}
