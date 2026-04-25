create or replace procedure register_user_account(
    p_full_name varchar,
    p_phone varchar,
    p_pass text
)
language plpgsql
as $$
declare
    v_account_id uuid := gen_random_uuid();
    v_role_id smallint;
begin
    -- 1. Lấy role USER
    select id into v_role_id
    from role
    where name = 'USER';

    if v_role_id is null then
        raise exception 'Role USER does not exist!';
    end if;

    -- 2. Insert account (root entity)
    insert into accounts(id, login_key, password_hash)
    values (v_account_id, p_phone, p_pass);

    -- 3. Insert user (extension của account)
    insert into users(id, full_name, phone)
    values (v_account_id, p_full_name, p_phone);

    -- 4. Gán role
    insert into account_role(id, account_id, role_id)
    values (gen_random_uuid(), v_account_id, v_role_id);

    raise notice 'Registration successful for Account ID: %', v_account_id;

exception
    when unique_violation then
        raise exception 'Phone number already exists or duplicate data!';
    when others then
        raise exception 'System error during registration: %', SQLERRM;
end;
$$;
