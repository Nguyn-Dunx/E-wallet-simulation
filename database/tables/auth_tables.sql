create table accounts (
    id uuid not null primary key default gen_random_uuid(),
    login_key varchar(20) not null unique,
    password_hash text not null,
    status varchar(20) not null default 'ACTIVE',
    login_failed_count int not null default 0,
    password_change_at timestamptz,
    created_at timestamptz default now(),
    updated_at timestamptz,
    deleted_at timestamptz
);

create table role (
    id smallserial not null primary key,
    name varchar not null
);

create table account_role (
    id uuid not null primary key default gen_random_uuid(),
    account_id uuid not null unique references accounts(id),
    role_id smallint not null references role(id)
);

create table admin (
    id uuid not null primary key references accounts(id),
    employee_code varchar(20) not null unique,
    department varchar(100),
    internal_note text,
    created_at timestamptz default now(),
    updated_at timestamptz,
    deleted_at timestamptz
);
