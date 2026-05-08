package org.example.backend.modules.identity.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CommandResponse(String message) {
}
