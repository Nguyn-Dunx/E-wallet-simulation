package org.example.backend.modules.wallet.service;


import org.example.backend.modules.wallet.dto.request.LinkBankRequest;
import org.example.backend.modules.wallet.dto.response.LinkedSourceResponse;
import org.example.backend.modules.wallet.dto.response.WalletResponse;

import java.util.List;
import java.util.UUID;

public interface WalletService {

    // Controller sẽ gọi qua đây chứ ko gọi qua Internal

    WalletResponse getMyWallet(UUID userId);

    List<LinkedSourceResponse> getMyLinkedSources(UUID userId);

    LinkedSourceResponse linkBankAccount(UUID userId, LinkBankRequest request);

    void unlinkBankAccount(UUID userId, UUID sourceId);
}
