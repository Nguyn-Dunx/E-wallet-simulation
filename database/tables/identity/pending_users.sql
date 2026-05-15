CREATE TABLE pending_users (
    id uuid default gen_random_uuid() primary key,
    phone_number VARCHAR(15) not null, -- Số điện thoại là định danh duy nhất
    full_name varchar(100) not null,
    hashed_password TEXT NOT NULL,         -- Mật khẩu đã mã hóa (Bcrypt/Argon2)
    otp_code VARCHAR(10) NOT NULL,         -- Mã OTP (thường là 6 số)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Thời điểm yêu cầu đăng ký
    expired_at TIMESTAMP NOT NULL          -- Thời điểm hết hạn (created_at + 5-10 phút)
);

-- Xóa các pending user hết hạn nhanh hơn
CREATE INDEX idx_pending_users_expired_at
ON pending_users(expired_at);

-- Nếu hay query theo thời gian tạo
CREATE INDEX idx_pending_users_created_at
ON pending_users(created_at);

-- Nếu có flow verify OTP bằng phone + otp
CREATE INDEX idx_pending_users_phone_otp
ON pending_users(phone_number, otp_code);

create unique index unq_idx_phone_created_at
on pending_users(phone_number, created_at);

CREATE TABLE token_blacklist (
    jti UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_token_blacklist_user_id
ON token_blacklist(user_id);

CREATE INDEX idx_token_blacklist_expired_at
ON token_blacklist(expired_at);
