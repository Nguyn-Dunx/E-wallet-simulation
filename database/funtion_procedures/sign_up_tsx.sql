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
    w_id UUID := gen_random_uuid();
BEGIN
    IF p_phone IS NULL OR p_phone = '' THEN
        RAISE EXCEPTION 'Phone is required';
    END IF;
    
    IF p_password_hash IS NULL THEN
        RAISE EXCEPTION 'Password is required';
    END IF;
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
        id,
        user_id
    )
    VALUES (
        w_id,
        v_account_id
    );

    RETURN v_account_id;

EXCEPTION
   WHEN unique_violation THEN
        RAISE EXCEPTION 'Duplicate data';
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
