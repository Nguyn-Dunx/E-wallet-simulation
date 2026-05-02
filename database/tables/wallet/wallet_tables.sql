-- Table: wallets
CREATE TABLE wallet.wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE, 
    balance DECIMAL(19,2) DEFAULT 0.00 CHECK (balance >= 0),
    currency VARCHAR(5) DEFAULT 'VND',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    version INT DEFAULT 0, -- Chống Race Condition
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

-- Table: linked_sources
CREATE TABLE wallet.linked_sources (
    id SERIAL PRIMARY KEY,
    wallet_id UUID NOT NULL,
    bank_name VARCHAR(50) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'VERIFIED',
    created_at timestamptz NOT NULL DEFAULT now()
);