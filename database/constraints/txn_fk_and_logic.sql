-- Module Txn: 
-- Foreign Keys
ALTER TABLE txn.transactions
ADD CONSTRAINT fk_txn_sender FOREIGN KEY (sender_wallet_id) REFERENCES wallet.wallets(id),
ADD CONSTRAINT fk_txn_receiver FOREIGN KEY (receiver_wallet_id) REFERENCES wallet.wallets(id);

-- Logic Constraints (Bảo vệ dữ liệu cốt lõi)
ALTER TABLE txn.transactions
ADD CONSTRAINT chk_transaction_logic CHECK (
    (type = 'TRANSFER' AND sender_wallet_id IS NOT NULL AND receiver_wallet_id IS NOT NULL) OR
    (type = 'DEPOSIT' AND sender_wallet_id IS NULL AND receiver_wallet_id IS NOT NULL) OR
    (type = 'WITHDRAW' AND sender_wallet_id IS NOT NULL AND receiver_wallet_id IS NULL)
);