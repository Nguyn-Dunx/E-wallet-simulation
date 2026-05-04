-- ROLE
CREATE TABLE identity.role (
    id SMALLSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO identity.role(name) VALUES ('USER'), ('ADMIN');

-- ACCOUNTS
CREATE TABLE identity.accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    login_key VARCHAR(50) NOT NULL,
    login_type VARCHAR(20) NOT NULL,  -- PHONE | EMPLOYEE_CODE

    password_hash TEXT NOT NULL,
    role_id SMALLINT NOT NULL,
    
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    login_failed_count INT NOT NULL DEFAULT 0,
    password_change_at timestamptz,
    token_version INT NOT NULL DEFAULT 0,
    
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz
);