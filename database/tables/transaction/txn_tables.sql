-- Tạo Type Enum (Chạy trước khi tạo Table)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'trans_type') THEN
        CREATE TYPE txn.trans_type AS ENUM ('DEPOSIT', 'WITHDRAW', 'TRANSFER');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'trans_status') THEN
        CREATE TYPE txn.trans_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED');
    END IF;
END $$;

-- Table: transactions
CREATE TABLE txn.transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_code VARCHAR(50) NOT NULL UNIQUE,
    sender_wallet_id UUID,
    receiver_wallet_id UUID,
    amount DECIMAL(19,2) NOT NULL CHECK (amount > 0),
    fee DECIMAL(19,2) NOT NULL DEFAULT 0.00 CHECK (fee >= 0),
    type txn.trans_type NOT NULL,
    status txn.trans_status NOT NULL DEFAULT 'PENDING',
    description TEXT,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz
);