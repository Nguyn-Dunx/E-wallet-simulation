import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/client';
import { Phone, Lock, Eye, EyeOff, User, Wallet, ArrowRight, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import './Auth.css';

const STEPS = ['Thông tin', 'Xác thực OTP', 'Hoàn tất'];

export default function RegisterPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [showPw, setShowPw] = useState(false);
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [receivedOtp, setReceivedOtp] = useState(''); // OTP from backend (dev only)
  const [countdown, setCountdown] = useState(0);
  const [form, setForm] = useState({ fullName: '', loginKey: '', password: '', confirmPassword: '' });
  const [errors, setErrors] = useState({});

  // Countdown timer
  useEffect(() => {
    if (countdown <= 0) return;
    const t = setTimeout(() => setCountdown(c => c - 1), 1000);
    return () => clearTimeout(t);
  }, [countdown]);

  const validateStep0 = () => {
    const e = {};
    if (!form.fullName.trim())   e.fullName = 'Vui lòng nhập họ tên';
    if (!form.loginKey.trim())   e.loginKey = 'Vui lòng nhập số điện thoại';
    if (form.password.length < 6) e.password = 'Mật khẩu ít nhất 6 ký tự';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Mật khẩu xác nhận không khớp';
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSendOtp = async (e) => {
    e.preventDefault();
    if (!validateStep0()) return;
    setLoading(true);
    try {
      const res = await authApi.signupInit(form);
      const otpFromServer = res.data || '';
      setReceivedOtp(otpFromServer);
      // auto-fill OTP boxes
      if (otpFromServer && otpFromServer.length === 6) {
        setOtp(otpFromServer.split(''));
      }
      toast.success('OTP đã được gửi!');
      setStep(1);
      setCountdown(60);
    } catch (err) {
      toast.error(err.message || 'Gửi OTP thất bại');
    } finally { setLoading(false); }
  };

  const handleVerify = async (e) => {
    e.preventDefault();
    const otpStr = otp.join('');
    if (otpStr.length < 6) { toast.error('Nhập đủ 6 số OTP'); return; }
    setLoading(true);
    try {
      await authApi.signupVerify({ phone: form.loginKey, otp: otpStr });
      setStep(2);
    } catch (err) {
      toast.error(err.message || 'OTP không đúng');
    } finally { setLoading(false); }
  };

  const handleOtpChange = (val, idx) => {
    if (!/^\d?$/.test(val)) return;
    const next = [...otp];
    next[idx] = val;
    setOtp(next);
    if (val && idx < 5) document.getElementById(`otp-${idx + 1}`)?.focus();
  };

  const handleOtpKeyDown = (e, idx) => {
    if (e.key === 'Backspace' && !otp[idx] && idx > 0) {
      document.getElementById(`otp-${idx - 1}`)?.focus();
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
          <p>Tạo tài khoản mới</p>
        </div>

        <div className="auth-card">
          {/* Steps */}
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

          {/* Step 0: Info */}
          {step === 0 && (
            <form onSubmit={handleSendOtp} className="auth-form" noValidate>
              <div className="form-group">
                <label className="form-label">Họ và tên</label>
                <div className="form-input-wrapper">
                  <User size={16} className="form-input-icon" />
                  <input id="fullName" type="text" className={`form-input ${errors.fullName ? 'input-error' : ''}`}
                    placeholder="Nguyễn Văn A" value={form.fullName}
                    onChange={e => setForm({ ...form, fullName: e.target.value })} />
                </div>
                {errors.fullName && <p className="form-error">{errors.fullName}</p>}
              </div>
              <div className="form-group">
                <label className="form-label">Số điện thoại</label>
                <div className="form-input-wrapper">
                  <Phone size={16} className="form-input-icon" />
                  <input id="loginKey" type="tel" className={`form-input ${errors.loginKey ? 'input-error' : ''}`}
                    placeholder="0912 345 678" value={form.loginKey}
                    onChange={e => setForm({ ...form, loginKey: e.target.value })} />
                </div>
                {errors.loginKey && <p className="form-error">{errors.loginKey}</p>}
              </div>
              <div className="form-group">
                <label className="form-label">Mật khẩu</label>
                <div className="form-input-wrapper">
                  <Lock size={16} className="form-input-icon" />
                  <input id="password" type={showPw ? 'text' : 'password'} className={`form-input ${errors.password ? 'input-error' : ''}`}
                    placeholder="Ít nhất 6 ký tự" value={form.password}
                    onChange={e => setForm({ ...form, password: e.target.value })} />
                  <button type="button" className="form-input-action" onClick={() => setShowPw(!showPw)}>
                    {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
                {errors.password && <p className="form-error">{errors.password}</p>}
              </div>
              <div className="form-group">
                <label className="form-label">Xác nhận mật khẩu</label>
                <div className="form-input-wrapper">
                  <Lock size={16} className="form-input-icon" />
                  <input id="confirmPassword" type="password" className={`form-input ${errors.confirmPassword ? 'input-error' : ''}`}
                    placeholder="Nhập lại mật khẩu" value={form.confirmPassword}
                    onChange={e => setForm({ ...form, confirmPassword: e.target.value })} />
                </div>
                {errors.confirmPassword && <p className="form-error">{errors.confirmPassword}</p>}
              </div>
              <button type="submit" id="btn-send-otp" className="btn btn-primary btn-full btn-lg" disabled={loading}>
                {loading ? <span className="spinner spinner-sm" /> : <ArrowRight size={18} />}
                Gửi mã OTP
              </button>
            </form>
          )}

          {/* Step 1: OTP */}
          {step === 1 && (
            <form onSubmit={handleVerify} className="auth-form">
              <p className="text-center text-secondary" style={{ marginBottom: 'var(--space-md)' }}>
                Nhập mã OTP 6 số đã gửi đến <strong style={{ color: 'var(--text-primary)' }}>{form.loginKey}</strong>
              </p>

              {/* DEV ONLY: hiển thị OTP nhận từ backend */}
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
                  <button type="button"
                    style={{ background: 'none', border: 'none', color: 'var(--warning)', cursor: 'pointer', fontSize: '0.8rem', fontWeight: 600, fontFamily: 'var(--font-body)', flexShrink: 0 }}
                    onClick={() => { navigator.clipboard.writeText(receivedOtp); toast.success('Đã copy OTP!'); }}>
                    Copy
                  </button>
                </div>
              )}
              <div className="otp-inputs">
                {otp.map((d, i) => (
                  <input key={i} id={`otp-${i}`} type="text" inputMode="numeric" maxLength={1}
                    className={`otp-input ${d ? 'otp-filled' : ''}`}
                    value={d}
                    onChange={e => handleOtpChange(e.target.value, i)}
                    onKeyDown={e => handleOtpKeyDown(e, i)} />
                ))}
              </div>
              <div className="otp-countdown">
                {countdown > 0
                  ? <span>Gửi lại sau <strong>{countdown}s</strong></span>
                  : <span>Không nhận được? <button type="button" onClick={handleSendOtp}>Gửi lại</button></span>
                }
              </div>
              <button type="submit" id="btn-verify-otp" className="btn btn-primary btn-full btn-lg" disabled={loading}>
                {loading ? <span className="spinner spinner-sm" /> : <ArrowRight size={18} />}
                Xác nhận OTP
              </button>
              <button type="button" className="btn btn-ghost btn-full" onClick={() => setStep(0)}>
                Quay lại
              </button>
            </form>
          )}

          {/* Step 2: Done */}
          {step === 2 && (
            <div className="text-center animate-scale-in" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div style={{ width: 80, height: 80, borderRadius: '50%', background: 'var(--success-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto' }}>
                <CheckCircle size={40} style={{ color: 'var(--success)' }} />
              </div>
              <div>
                <h3 style={{ marginBottom: 8 }}>Tạo tài khoản thành công! 🎉</h3>
                <p className="text-muted">Chào mừng bạn đến với ViPay, {form.fullName.split(' ').pop()}!</p>
              </div>
              <button className="btn btn-primary btn-full btn-lg" onClick={() => navigate('/login')} id="btn-goto-login">
                <ArrowRight size={18} /> Đăng nhập ngay
              </button>
            </div>
          )}

          {step < 2 && (
            <p className="text-center text-sm text-muted" style={{ marginTop: 'var(--space-md)' }}>
              Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
