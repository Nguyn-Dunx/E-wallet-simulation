-- USERS
create unique index idx_unique_users_phone
on users(phone)
where deleted_at is null;

-- ADMIN
create unique index idx_unique_admin_employee_code
on admin(employee_code)
where deleted_at is null;

-- ACCOUNTS
create unique index idx_unique_accounts_login_key
on accounts(login_key)
where deleted_at is null;

create index idx_accounts_role_id
on accounts(role_id);

create index idx_accounts_not_deleted
on accounts(id)
where deleted_at is null;

-- FK indexes
create index idx_users_id on users(id);
create index idx_admin_id on admin(id);
