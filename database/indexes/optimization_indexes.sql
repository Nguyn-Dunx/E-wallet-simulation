-- USERS
create unique index idx_unique_users_phone
on identity.users(phone)
where deleted_at is null;

-- ADMIN
create unique index idx_unique_admin_employee_code
on identity.admin(employee_code)
where deleted_at is null;

-- ACCOUNTS
create unique index idx_unique_accounts_login_key
on identity.accounts(login_key)
where deleted_at is null;

create index idx_accounts_role_id
on identity.accounts(role_id);

create index idx_accounts_not_deleted
on identity.accounts(id)
where deleted_at is null;
