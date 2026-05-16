package org.example.backend.modules.identity.dto.request;

public record LockAccountRequest(
        String loginKey
) {
}
