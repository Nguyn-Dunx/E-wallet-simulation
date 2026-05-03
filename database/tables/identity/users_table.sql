-- USERS
CREATE TABLE identity.users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    ekyc_status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    avatar_url TEXT,
    
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz
);

-- ADMINS
CREATE TABLE identity.admin (
    id UUID PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    department VARCHAR(100),
    internal_note TEXT,
    
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz
);