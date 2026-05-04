-- Table: wallets
CREATE TABLE wallet.wallets (
    id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID           NOT NULL UNIQUE,
    balance     DECIMAL(19,2)  NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    currency    VARCHAR(5)     NOT NULL DEFAULT 'VND',
    status      VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    version     INT            NOT NULL DEFAULT 0,
    created_at  timestamptz    NOT NULL DEFAULT now(),
    updated_at  timestamptz    NOT NULL DEFAULT now(),
    deleted_at  timestamptz
);

-- Table: linked_sources
CREATE TABLE wallet.linked_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL,
    bank_name VARCHAR(50) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'VERIFIED',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz
);