alter table accounts
add constraint fk_account_useers foreign key (user_id) references users(id);
