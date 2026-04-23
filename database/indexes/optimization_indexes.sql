create unique index idx_unique_phone
on users(phone)
where updated_at is not null;

create index idx_phone_account
on accounts(user_id)
include (password_hash);
