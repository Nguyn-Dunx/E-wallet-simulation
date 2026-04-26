-- ROLE
create table role (
    id smallserial primary key,
    name varchar(50) not null unique
);

-- ACCOUNTS
create table accounts (
    id uuid primary key default gen_random_uuid(),

    login_key varchar(50) not null unique,
    login_type varchar(20) not null, -- PHONE | EMPLOYEE_CODE

    password_hash text not null,
    role_id smallint not null references role(id),

    status varchar(20) not null default 'ACTIVE', -- ACTIVE | LOCKED | DISABLED
    login_failed_count int not null default 0,
    password_change_at timestamptz,

    created_at timestamptz not null default now(),
    updated_at timestamptz,
    deleted_at timestamptz
);
