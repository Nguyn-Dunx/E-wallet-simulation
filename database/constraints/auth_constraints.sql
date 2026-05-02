-- ==========================================
-- A. UNIQUE CONSTRAINTS
-- ==========================================
ALTER TABLE identity.role ADD CONSTRAINT uq_role_name UNIQUE (name);
ALTER TABLE identity.accounts ADD CONSTRAINT uq_accounts_login_key UNIQUE (login_key);
ALTER TABLE identity.users ADD CONSTRAINT uq_users_phone UNIQUE (phone);
ALTER TABLE identity.admin ADD CONSTRAINT uq_admin_employee_code UNIQUE (employee_code);

-- ==========================================
-- B. FOREIGN KEYS (Khóa ngoại)
-- ==========================================
-- Account trỏ tới Role
ALTER TABLE identity.accounts 
ADD CONSTRAINT fk_accounts_role FOREIGN KEY (role_id) REFERENCES identity.role(id);

-- User & Admin trỏ tới Account (Shared PK, tự động xóa khi Account bị xóa)
ALTER TABLE identity.users 
ADD CONSTRAINT fk_users_account FOREIGN KEY (id) REFERENCES identity.accounts(id) ON DELETE CASCADE;

ALTER TABLE identity.admin 
ADD CONSTRAINT fk_admin_account FOREIGN KEY (id) REFERENCES identity.accounts(id) ON DELETE CASCADE;

-- ==========================================
-- C. CHECK CONSTRAINTS (Bảo vệ Logic Nghiệp vụ)
-- ==========================================
-- 1. Ép kiểu đăng nhập chỉ được nằm trong danh sách cho phép
ALTER TABLE identity.accounts
ADD CONSTRAINT chk_accounts_login_type CHECK (login_type IN ('PHONE', 'EMPLOYEE_CODE'));

-- 2. Trạng thái tài khoản không thể nhận giá trị linh tinh
ALTER TABLE identity.accounts
ADD CONSTRAINT chk_accounts_status CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED'));

-- 3. Số lần đăng nhập sai không bao giờ được âm
ALTER TABLE identity.accounts
ADD CONSTRAINT chk_accounts_login_failed CHECK (login_failed_count >= 0);

-- 4. Trạng thái định danh (eKYC) của User
ALTER TABLE identity.users
ADD CONSTRAINT chk_users_ekyc CHECK (ekyc_status IN ('UNVERIFIED', 'VERIFIED', 'REJECTED'));