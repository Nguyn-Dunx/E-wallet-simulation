package org.example.backend.modules.wallet.service;


import org.example.backend.modules.wallet.dto.response.WalletResponse;

import java.util.UUID;

public interface WalletService {

    // Controller sẽ gọi qua đây chứ ko gọi qua Internal

    WalletResponse getMyWallet(UUID userId);
}
