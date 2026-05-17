package org.example.backend.modules.identity.services.internal;

import java.util.UUID;

public interface IdentityInternalService {
    void verifyTransactionPin(UUID userId, String rawPin);
}
