create table users(
	id uuid not null primary key references accounts(id) on delete cascade,
	full_name varchar(100) not null,
	phone varchar(20) not null unique,
	ekyc_status varchar(20) not null default 'UNVERIFIED',
	avatar_url text,
	status varchar(20) not null default 'ACTIVE',
	created_at timestamptz not null default now(),
	updated_at timestamptz,
	deleted_at timestamptz
)
