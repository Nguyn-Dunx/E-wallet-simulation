create unique index idx_unique_phone
on users(phone)
where deleted_at is null;

create unique index idx_unique_account_id
on account_role(account_id)
include (role_id);

create unique index idx_unique_employee_code
on admin(employee_code);
