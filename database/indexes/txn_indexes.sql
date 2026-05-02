-- Tìm kiếm giao dịch nhanh theo mã code và theo ví
CREATE INDEX idx_txn_code ON txn.transactions(transaction_code);
CREATE INDEX idx_txn_sender ON txn.transactions(sender_wallet_id);
CREATE INDEX idx_txn_receiver ON txn.transactions(receiver_wallet_id);