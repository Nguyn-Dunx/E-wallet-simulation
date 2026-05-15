package org.example.backend.modules.wallet.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.event.UserRegisteredEvent;
import org.example.backend.modules.wallet.service.internal.WalletInternalService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletCreationListener {

    private final WalletInternalService walletInternalService;

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("catch event User {} signed up. Initialising the wallet......", event.getUserId());

        try {
            walletInternalService.createWallet(event.getUserId());
            log.info("Wallet successfully created for the User: {}", event.getUserId());
        } catch (Exception e) {
            log.error("error while created wallet for User {}: {}", event.getUserId(), e.getMessage());
        }
    }

}
