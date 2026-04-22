create table accounts(
	id uuid not null primary key,
	user_id uuid not null,
	password_hash text not null,
	pin_hash text,
	status varchar(20) not null default 'ACTIVE',
	login_failed_count int not null default 0,
	pin_failed_count int not null default 0,
	password_change_at timestamptz,
	create_at timestamptz default now(),
	updated_at timestamptz
)
