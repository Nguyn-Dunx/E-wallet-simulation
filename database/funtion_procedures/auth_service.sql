-- transaction procedure for register
create or replace procedure register_user_account(
	p_full_name varchar,
	p_phone varchar,
	p_pass text
)
language plpgsql
as $$
declare
	v_user_id uuid := gen_random_uuid();
begin
	insert into users(id, fullname, phone)
	values (v_user_id, p_fullname, p_phone);

	insert into accounts(id, user_id, password_hash)
	values (gen_random_uuid(), v_user_id, p_pass);

	raise notice 'Registration successful for User ID: %', v_user_id;

exception
	when unique_violation then
		raise exception 'Phone number already exists in the system!';
	when others then
		raise exception 'System error during registration: %', SQLERM;
end;
$$;
