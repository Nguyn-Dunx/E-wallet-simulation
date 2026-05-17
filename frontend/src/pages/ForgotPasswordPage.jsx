import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/client';
import {
  Phone, Lock, Eye, EyeOff, Wallet,
  ArrowRight, CheckCircle, KeyRound,
} from 'lucide-react';
import toast from 'react-hot-toast';
import './Auth.css';

const STEPS = ['Số điện thoại', 'Xác thực OTP', 'Mật khẩu mới'];

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);

  // Step 0
  const [phone, setPhone] = useState('');
  const [phoneError, setPhoneError] = useState('');

  // Step 1 – OTP
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [receivedOtp, setReceivedOtp] = useState('');
  const [countdown, setCountdown] = useState(0);
  const [resetToken, setResetToken] = useState(''); // token returned by verify-otp

  // Step 2 – New password
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [pwErrors, setPwErrors] = useState({});

  // Countdown timer
  useEffect(() => {
    if (countdown <= 0) return;
    const t = setTimeout(() => setCountdown(c => c - 1), 1000);
    return () => clearTimeout(t);
  }, [countdown]);

  // ── Step 0: Request OTP ───────────────────────────────────────────────
  const handleRequestOtp = async (e) => {
    e?.preventDefault();
    if (!phone.trim()) { setPhoneError('Vui lòng nhập số điện thoại'); return; }
    setPhoneError('');
    setLoading(true);
    try {
      const res = await authApi.resetPasswordCreateOtp({ phone: phone.trim() });
      // Backend trả { data: { otpCode: "..." } } (DEV mode)
      const otpFromServer = res?.data?.otpCode || '';
      setReceivedOtp(String(otpFromServer));
      if (String(otpFromServer).length === 6) {
        setOtp(String(otpFromServer).split(''));
      }
      toast.success('Mã OTP đã được gửi!');
      setStep(1);
      setCountdown(60);
    } catch (err) {
      toast.error(err.message || 'Không thể gửi OTP, thử lại sau');
    } finally {
      setLoading(false);
    }
  };

  // ── Step 1: Verify OTP ────────────────────────────────────────────────
  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    const otpStr = otp.join('');
    if (otpStr.length < 6) { toast.error('Nhập đủ 6 số OTP'); return; }
    setLoading(true);
    try {
      const res = await authApi.resetPasswordVerifyOtp({ phone: phone.trim(), otp: otpStr });
      // Backend trả token (UUID) dùng ở bước 3
      const token = res?.data || '';
      setResetToken(token);
      setStep(2);
    } catch (err) {
      toast.error(err.message || 'OTP không đúng hoặc đã hết hạn');
    } finally {
      setLoading(false);
    }
  };

  // ── Step 2: Set new password ──────────────────────────────────────────
  const handleResetPassword = async (e) => {
    e.preventDefault();
    const errs = {};
    if (newPassword.length < 6) errs.newPassword = 'Mật khẩu ít nhất 6 ký tự';
    if (newPassword !== confirmPassword) errs.confirmPassword = 'Mật khẩu xác nhận không khớp';
    setPwErrors(errs);
    if (Object.keys(errs).length) return;

    setLoading(true);
    try {
      await authApi.resetPasswordConfirm({ token: resetToken, newPassword });
      setStep(3);
    } catch (err) {
      toast.error(err.message || 'Đổi mật khẩu thất bại');
    } finally {
      setLoading(false);
    }
  };

  // ── OTP box helpers ───────────────────────────────────────────────────
  const handleOtpChange = (val, idx) => {
    if (!/^\d?$/.test(val)) return;
    const next = [...otp];
    next[idx] = val;
    setOtp(next);
    if (val && idx < 5) document.getElementById(`fp-otp-${idx + 1}`)?.focus();
  };

  const handleOtpKeyDown = (e, idx) => {
    if (e.key === 'Backspace' && !otp[idx] && idx > 0) {
      document.getElementById(`fp-otp-${idx - 1}`)?.focus();
    }
  };

  const handleOtpPaste = (e) => {
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (pasted.length === 6) {
      setOtp(pasted.split(''));
      e.preventDefault();
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-bg-glow" />
      <div className="auth-container animate-fade-in">

        {/* Brand */}
        <div className="auth-brand">
          <div className="auth-logo"><Wallet size={28} /></div>
          <h1>ViPay</h1>
          <p>Khôi phục mật khẩu</p>
        </div>

        <div className="auth-card">
          {/* Step indicator – chỉ hiện khi chưa xong */}
          {step < 3 && (
            <div className="steps">
              {STEPS.map((label, i) => (
                <>
                  <div key={i} className={`step ${i === step ? 'active' : i < step ? 'done' : ''}`}>
                    <div className="step-circle">
                      {i < step ? <CheckCircle size={14} /> : i + 1}
                    </div>
                    <span className="step-label">{label}</span>
                  </div>
                  {i < STEPS.length - 1 && <div key={`line-${i}`} className="step-line" />}
                </>
              ))}
            </div>
          )}

          {/* ── Step 0: Nhập SĐT ───────────────────────────────────────── */}
          {step === 0 && (
            <form onSubmit={handleRequestOtp} className="auth-form" noValidate>
              <div className="auth-card-header" style={{ marginBottom: 0 }}>
                <h2>Quên mật khẩu?</h2>
                <p>Nhập số điện thoại để nhận mã OTP xác thực</p>
              </div>

              <div className="form-group">
                <label className="form-label">Số điện thoại</label>
                <div className="form-input-wrapper">
                  <Phone size={16} className="form-input-icon" />
                  <input
                    id="fp-phone"
                    type="tel"
                    className={`form-input ${phoneError ? 'input-error' : ''}`}
                    placeholder="0912 345 678"
                    value={phone}
                    onChange={e => { setPhone(e.target.value); setPhoneError(''); }}
                    autoComplete="tel"
                  />
                </div>
                {phoneError && <p className="form-error">{phoneError}</p>}
              </div>

              <button
                id="btn-fp-send-otp"
                type="submit"
                className="btn btn-primary btn-full btn-lg"
                disabled={loading}
              >
                {loading ? <span className="spinner spinner-sm" /> : <ArrowRight size={18} />}
                {loading ? 'Đang gửi...' : 'Gửi mã OTP'}
              </button>

              <p className="text-center text-sm text-muted" style={{ marginTop: 'var(--space-sm)' }}>
                Nhớ mật khẩu rồi? <Link to="/login">Đăng nhập</Link>
              </p>
            </form>
          )}

          {/* ── Step 1: Nhập OTP ───────────────────────────────────────── */}
          {step === 1 && (
            <form onSubmit={handleVerifyOtp} className="auth-form">
              <p className="text-center text-secondary" style={{ marginBottom: 'var(--space-md)' }}>
                Nhập mã OTP 6 số đã gửi đến{' '}
                <strong style={{ color: 'var(--text-primary)' }}>{phone}</strong>
              </p>

              {/* DEV MODE banner */}
              {receivedOtp && (
                <div style={{
                  background: 'rgba(245, 158, 11, 0.12)',
                  border: '1px dashed rgba(245, 158, 11, 0.5)',
                  borderRadius: 'var(--radius-md)',
                  padding: '10px 16px',
                  marginBottom: 'var(--space-md)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: 12,
                }}>
                  <div>
                    <p style={{ fontSize: '0.72rem', color: 'var(--warning)', fontWeight: 700, marginBottom: 2 }}>
                      🛠️ DEV MODE — Mã OTP của bạn:
                    </p>
                    <p style={{ fontSize: '1.5rem', fontWeight: 800, letterSpacing: 8, color: 'var(--text-primary)', fontFamily: 'monospace' }}>
                      {receivedOtp}
                    </p>
                  </div>
                  <button
                    type="button"
                    style={{ background: 'none', border: 'none', color: 'var(--warning)', cursor: 'pointer', fontSize: '0.8rem', fontWeight: 600, fontFamily: 'var(--font-body)', flexShrink: 0 }}
                    onClick={() => { navigator.clipboard.writeText(receivedOtp); toast.success('Đã copy OTP!'); }}
                  >
                    Copy
                  </button>
                </div>
              )}

              <div className="otp-inputs" onPaste={handleOtpPaste}>
                {otp.map((d, i) => (
                  <input
                    key={i}
                    id={`fp-otp-${i}`}
                    type="text"
                    inputMode="numeric"
                    maxLength={1}
                    className={`otp-input ${d ? 'otp-filled' : ''}`}
                    value={d}
                    onChange={e => handleOtpChange(e.target.value, i)}
                    onKeyDown={e => handleOtpKeyDown(e, i)}
                  />
                ))}
              </div>

              <div className="otp-countdown">
                {countdown > 0
                  ? <span>Gửi lại sau <strong>{countdown}s</strong></span>
                  : <span>Không nhận được? <button type="button" onClick={handleRequestOtp}>Gửi lại</button></span>
                }
              </div>

              <button
                id="btn-fp-verify-otp"
                type="submit"
                className="btn btn-primary btn-full btn-lg"
                disabled={loading}
              >
                {loading ? <span className="spinner spinner-sm" /> : <ArrowRight size={18} />}
                Xác nhận OTP
              </button>

              <button type="button" className="btn btn-ghost btn-full" onClick={() => setStep(0)}>
                Quay lại
              </button>
            </form>
          )}

          {/* ── Step 2: Nhập mật khẩu mới ─────────────────────────────── */}
          {step === 2 && (
            <form onSubmit={handleResetPassword} className="auth-form" noValidate>
              <div className="auth-card-header" style={{ marginBottom: 0 }}>
                <h2>Tạo mật khẩu mới</h2>
                <p>Mật khẩu mới phải khác với mật khẩu cũ</p>
              </div>

              <div className="form-group">
                <label className="form-label">Mật khẩu mới</label>
                <div className="form-input-wrapper">
                  <KeyRound size={16} className="form-input-icon" />
                  <input
                    id="fp-new-password"
                    type={showPw ? 'text' : 'password'}
                    className={`form-input ${pwErrors.newPassword ? 'input-error' : ''}`}
                    placeholder="Ít nhất 6 ký tự"
                    value={newPassword}
                    onChange={e => { setNewPassword(e.target.value); setPwErrors(p => ({ ...p, newPassword: '' })); }}
                    autoComplete="new-password"
                  />
                  <button type="button" className="form-input-action" onClick={() => setShowPw(!showPw)}>
                    {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
                {pwErrors.newPassword && <p className="form-error">{pwErrors.newPassword}</p>}
              </div>

              <div className="form-group">
                <label className="form-label">Xác nhận mật khẩu mới</label>
                <div className="form-input-wrapper">
                  <Lock size={16} className="form-input-icon" />
                  <input
                    id="fp-confirm-password"
                    type="password"
                    className={`form-input ${pwErrors.confirmPassword ? 'input-error' : ''}`}
                    placeholder="Nhập lại mật khẩu"
                    value={confirmPassword}
                    onChange={e => { setConfirmPassword(e.target.value); setPwErrors(p => ({ ...p, confirmPassword: '' })); }}
                    autoComplete="new-password"
                  />
                </div>
                {pwErrors.confirmPassword && <p className="form-error">{pwErrors.confirmPassword}</p>}
              </div>

              <button
                id="btn-fp-confirm"
                type="submit"
                className="btn btn-primary btn-full btn-lg"
                disabled={loading}
              >
                {loading ? <span className="spinner spinner-sm" /> : <ArrowRight size={18} />}
                {loading ? 'Đang xử lý...' : 'Đổi mật khẩu'}
              </button>
            </form>
          )}

          {/* ── Step 3: Thành công ─────────────────────────────────────── */}
          {step === 3 && (
            <div className="text-center animate-scale-in" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div style={{
                width: 80, height: 80, borderRadius: '50%',
                background: 'var(--success-light)',
                display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto',
              }}>
                <CheckCircle size={40} style={{ color: 'var(--success)' }} />
              </div>
              <div>
                <h3 style={{ marginBottom: 8 }}>Đổi mật khẩu thành công! 🎉</h3>
                <p className="text-muted">Vui lòng đăng nhập lại bằng mật khẩu mới của bạn.</p>
              </div>
              <button
                id="btn-fp-goto-login"
                className="btn btn-primary btn-full btn-lg"
                onClick={() => navigate('/login')}
              >
                <ArrowRight size={18} /> Đăng nhập ngay
              </button>
            </div>
          )}
        </div>

        <p className="auth-footer-note">🔒 Bảo mật bởi JWT + Mã PIN giao dịch 2 lớp</p>
      </div>
    </div>
  );
}
