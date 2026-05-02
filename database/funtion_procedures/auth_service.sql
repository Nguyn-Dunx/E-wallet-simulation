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
    from identity.role
    where name = 'USER';

    if v_role_id is null then
        raise exception 'Role USER does not exist!';
    end if;

    -- 2. Insert account
    insert into identity.accounts(
        id,
        login_key,
        login_type,
        password_hash,
        role_id
    )
    values (
        v_account_id,
        p_phone,
        'PHONE',
        p_pass,
        v_role_id
    );

    -- 3. Insert user
    insert into identity.users(
        id,
        full_name,
        phone
    )
    values (
        v_account_id,
        p_full_name,
        p_phone
    );

    raise notice 'Registration successful for Account ID: %', v_account_id;

exception
    when unique_violation then
        raise exception 'Phone already exists!';
    when others then
        raise exception 'System error during registration: %', SQLERRM;
end;
$$;
