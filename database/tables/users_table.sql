-- USERS
create table users (
    id uuid primary key references accounts(id) on delete cascade,

    full_name varchar(100) not null,
    phone varchar(20) not null unique,

    ekyc_status varchar(20) not null default 'UNVERIFIED',
    avatar_url text,

    created_at timestamptz not null default now(),
    updated_at timestamptz,
    deleted_at timestamptz
);

-- ADMINS
create table admin (
    id uuid primary key references accounts(id) on delete cascade,

    employee_code varchar(20) not null unique,
    department varchar(100),
    internal_note text,

    created_at timestamptz not null default now(),
    updated_at timestamptz,
    deleted_at timestamptz
);
