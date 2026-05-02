-- Module Wallet: Foreign Keys
ALTER TABLE wallet.wallets
ADD CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES identity.users(id);

ALTER TABLE wallet.linked_sources
ADD CONSTRAINT fk_linked_source_wallet FOREIGN KEY (wallet_id) REFERENCES wallet.wallets(id) ON DELETE CASCADE;