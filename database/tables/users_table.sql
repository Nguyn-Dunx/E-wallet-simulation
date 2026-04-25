create table users(
	id uuid not null primary key,
	fullname varchar(100) not null,
	phone varchar(20) not null unique,
	email varchar(100) unique,
	ekyc_status varchar(20) not null default 'UNVERIFIED',
	avatar_url text,
	status varchar(20) not null default 'ACTIVE',
	create_at timestamptz not null default now(),
	updated_at timestamptz,
	deleted_at timestamptz
)
