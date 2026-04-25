create table accounts(
	id uuid not null primary key default gen_random_uuid(),
	password_hash text not null,
	status varchar(20) not null default 'ACTIVE',
	login_failed_count int not null default 0,
	password_change_at timestamptz,
	create_at timestamptz default now(),
	updated_at timestamptz
)

