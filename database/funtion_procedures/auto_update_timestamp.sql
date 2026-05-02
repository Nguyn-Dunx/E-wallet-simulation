-- Hàm trigger tự động cập nhật thời gian updated_at mỗi khi có record bị sửa
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = now();
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Gắn trigger vào bảng wallets
CREATE TRIGGER trg_wallets_updated_at
BEFORE UPDATE ON wallet.wallets
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- Gắn trigger vào bảng transactions
CREATE TRIGGER trg_txn_updated_at
BEFORE UPDATE ON txn.transactions
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();