-- Combined SQL for E-wallet (generated)
-- Sources: database/tables, database/constraints, database/indexes, database/funtion_procedures
-- Note: sign_up_tsx.sql was included in the functions/procedures section.

-- Tạo 3 schema cho 3 module
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS wallet;
CREATE SCHEMA IF NOT EXISTS txn;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ================================
-- TABLES: identity
-- ================================
-- ROLE
CREATE TABLE identity.role (
    id SMALLSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO identity.role(name) VALUES ('USER'), ('ADMIN')
ON CONFLICT (name) DO NOTHING;

-- ACCOUNTS
CREATE TABLE identity.accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    login_key VARCHAR(50) NOT NULL ,
    login_type VARCHAR(20) NOT NULL,  -- PHONE | EMPLOYEE_CODE

    password_hash TEXT NOT NULL,
    role_id SMALLINT NOT NULL,
    
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    login_failed_count INT NOT NULL DEFAULT 0,
    password_change_at timestamptz,
    token_version INT NOT NULL DEFAULT 0,
    pin_hash TEXT,
    pin_failed_count INT NOT NULL DEFAULT 0,
    
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz
);

-- USERS
CREATE TABLE identity.users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL ,
    ekyc_status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    avatar_url TEXT,
    
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz
);

-- ADMINS
CREATE TABLE identity.admin (
    id UUID PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL,
    department VARCHAR(100),
    internal_note TEXT,
    
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz
);

-- ================================
-- TABLES: wallet
-- ================================
-- Table: wallets
CREATE TABLE wallet.wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE, 
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    currency VARCHAR(5) NOT NULL DEFAULT 'VND',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 0, -- Chống Race Condition
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz
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

-- ================================
-- TABLES: transaction
-- ================================
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

-- ================================
-- CONSTRAINTS
-- ================================
-- ==========================================
--  FOREIGN KEYS (Khóa ngoại)
-- ==========================================
-- Account trỏ tới Role
ALTER TABLE identity.accounts 
ADD CONSTRAINT fk_accounts_role FOREIGN KEY (role_id) REFERENCES identity.role(id);

-- User & Admin trỏ tới Account (Shared PK, tự động xóa khi Account bị xóa)
ALTER TABLE identity.users 
ADD CONSTRAINT fk_users_account FOREIGN KEY (id) REFERENCES identity.accounts(id) ON DELETE CASCADE;

ALTER TABLE identity.admin 
ADD CONSTRAINT fk_admin_account FOREIGN KEY (id) REFERENCES identity.accounts(id) ON DELETE CASCADE;

-- ==========================================
--  CHECK CONSTRAINTS (Bảo vệ Logic Nghiệp vụ)
-- ==========================================
-- 1. Ép kiểu đăng nhập chỉ được nằm trong danh sách cho phép
ALTER TABLE identity.accounts
ADD CONSTRAINT chk_accounts_login_type CHECK (login_type IN ('PHONE', 'EMPLOYEE_CODE'));

-- 2. Trạng thái tài khoản không thể nhận giá trị linh tinh
ALTER TABLE identity.accounts
ADD CONSTRAINT chk_accounts_status CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED'));

-- 3. Số lần đăng nhập sai không bao giờ được âm
ALTER TABLE identity.accounts
ADD CONSTRAINT chk_accounts_login_failed CHECK (login_failed_count >= 0);

-- 4. Trạng thái định danh (eKYC) của User
ALTER TABLE identity.users
ADD CONSTRAINT chk_users_ekyc CHECK (ekyc_status IN ('UNVERIFIED', 'VERIFIED', 'REJECTED'));

-- Module Wallet: Foreign Keys
ALTER TABLE wallet.wallets
ADD CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES identity.users(id);

ALTER TABLE wallet.linked_sources
ADD CONSTRAINT fk_linked_source_wallet FOREIGN KEY (wallet_id) REFERENCES wallet.wallets(id) ON DELETE CASCADE;

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

-- ================================
-- INDEXES
-- ================================
-- USERS
create unique index idx_unique_users_phone
on identity.users(phone)
where deleted_at is null;

-- ADMIN
create unique index idx_unique_admin_employee_code
on identity.admin(employee_code)
where deleted_at is null;

-- ACCOUNTS
create unique index idx_unique_accounts_login_key
on identity.accounts(login_key)
where deleted_at is null;

create index idx_accounts_role_id
on identity.accounts(role_id);

create index idx_accounts_not_deleted
on identity.accounts(id)
where deleted_at is null;

--Tìm ví theo user_id
CREATE INDEX idx_wallets_user_id ON wallet.wallets(user_id);

-- Tìm kiếm giao dịch nhanh theo mã code và theo ví
CREATE INDEX idx_txn_code ON txn.transactions(transaction_code);
CREATE INDEX idx_txn_sender ON txn.transactions(sender_wallet_id);
CREATE INDEX idx_txn_receiver ON txn.transactions(receiver_wallet_id);

-- ================================
-- FUNCTIONS / PROCEDURES
-- ================================
--Sign up user
CREATE OR REPLACE FUNCTION identity.signup_user(
    p_phone VARCHAR,
    p_password_hash TEXT,
    p_full_name VARCHAR
)
RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
    v_account_id UUID;
    v_role_user_id SMALLINT;
BEGIN
    -- 1. Lấy role USER
    SELECT id INTO v_role_user_id
    FROM identity.role
    WHERE name = 'USER';

    IF v_role_user_id IS NULL THEN
        RAISE EXCEPTION 'Role USER not found';
    END IF;

    -- 2. Tạo account
    INSERT INTO identity.accounts (
        login_key,
        login_type,
        password_hash,
        role_id
    )
    VALUES (
        p_phone,
        'PHONE',
        p_password_hash,
        v_role_user_id
    )
    RETURNING id INTO v_account_id;

    -- 3. Tạo user profile
    INSERT INTO identity.users (
        id,
        full_name,
        phone
    )
    VALUES (
        v_account_id,
        p_full_name,
        p_phone
    );

    -- 4. Tạo wallet
    INSERT INTO wallet.wallets (
        user_id
    )
    VALUES (
        v_account_id
    );

    RETURN v_account_id;

EXCEPTION
    WHEN unique_violation THEN
        RAISE EXCEPTION 'Phone already exists';
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Signup user failed: %', SQLERRM;
END;
$$;

-- Sign up admin
CREATE OR REPLACE FUNCTION identity.signup_admin(
    p_employee_code VARCHAR,
    p_password_hash TEXT,
    p_department VARCHAR,
    p_note TEXT
)
RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
    v_account_id UUID;
    v_role_admin_id SMALLINT;
BEGIN
    -- 1. Lấy role ADMIN
    SELECT id INTO v_role_admin_id
    FROM identity.role
    WHERE name = 'ADMIN';

    IF v_role_admin_id IS NULL THEN
        RAISE EXCEPTION 'Role ADMIN not found';
    END IF;

    -- 2. Tạo account
    INSERT INTO identity.accounts (
        login_key,
        login_type,
        password_hash,
        role_id
    )
    VALUES (
        p_employee_code,
        'EMPLOYEE_CODE',
        p_password_hash,
        v_role_admin_id
    )
    RETURNING id INTO v_account_id;

    -- 3. Tạo admin profile
    INSERT INTO identity.admin (
        id,
        employee_code,
        department,
        internal_note
    )
    VALUES (
        v_account_id,
        p_employee_code,
        p_department,
        p_note
    );

    RETURN v_account_id;

EXCEPTION
    WHEN unique_violation THEN
        RAISE EXCEPTION 'Employee code already exists';
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Signup admin failed: %', SQLERRM;
END;
$$;

create or replace procedure register_user_account(
    p_full_name varchar,
    p_phone varchar,
    p_pass text
)
language plpgsql
as $$
declare
    v_account_id uuid := gen_random_uuid();
    v_role_id smallint;
begin
    -- 1. Lấy role USER
    select id into v_role_id
    from identity.role
    where name = 'USER';

    if v_role_id is null then
        raise exception 'Role USER does not exist!';
    end if;

    -- 2. Insert account
    insert into identity.accounts(
        id,
        login_key,
        login_type,
        password_hash,
        role_id
    )
    values (
        v_account_id,
        p_phone,
        'PHONE',
        p_pass,
        v_role_id
    );

    -- 3. Insert user
    insert into identity.users(
        id,
        full_name,
        phone
    )
    values (
        v_account_id,
        p_full_name,
        p_phone
    );

    -- 4. Tạo wallet
    INSERT INTO wallet.wallets(
        user_id
    )
    VALUES (
        v_account_id
    );

    raise notice 'Registration successful for Account ID: %', v_account_id;

exception
    when unique_violation then
        raise exception 'Phone already exists!';
    when others then
        raise exception 'System error during registration: %', SQLERRM;
end;
$$;

-- ================================
-- TRIGGERS
-- ================================
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
