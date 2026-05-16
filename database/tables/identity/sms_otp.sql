-- 1. Master Table rút gọn
CREATE TABLE identity.sms_logs (
    id UUID DEFAULT gen_random_uuid(),
    phone VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    otp_code VARCHAR(10) NOT NULL,
    expired_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),

    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- 2. Index cơ bản để query nhanh
CREATE INDEX idx_sms_logs_phone_created_at ON identity.sms_logs (phone, created_at DESC);

-- 3. Bảng Default hứng dữ liệu lỗi/chưa có partition
CREATE TABLE identity.sms_logs_default PARTITION OF identity.sms_logs DEFAULT;

-- 4. Bảng khởi tạo cho ngày hôm nay (13/05/2026)
CREATE TABLE identity.sms_logs_y2026_m05_d13 PARTITION OF identity.sms_logs
    FOR VALUES FROM ('2026-05-13 00:00:00+07') TO ('2026-05-14 00:00:00+07');

CREATE TABLE identity.reset_password_sessions (
    id int PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES identity.accounts(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_reset_token ON identity.reset_password_sessions(token);
