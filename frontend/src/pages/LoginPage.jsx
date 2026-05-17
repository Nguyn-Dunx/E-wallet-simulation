import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Phone, Lock, Eye, EyeOff, Wallet, ArrowRight } from 'lucide-react';
import toast from 'react-hot-toast';
import './Auth.css';

export default function LoginPage() {
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ loginKey: '', password: '' });
  const [showPw, setShowPw] = useState(false);
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!form.loginKey) e.loginKey = 'Vui lòng nhập số điện thoại';
    if (!form.password)  e.password = 'Vui lòng nhập mật khẩu';
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      await login(form.loginKey.trim(), form.password.trim(), 'PHONE');
      toast.success('Đăng nhập thành công! 🎉');
      navigate('/dashboard');
    } catch (err) {
      toast.error(err.message || 'Đăng nhập thất bại');
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-bg-glow" />

      <div className="auth-container animate-fade-in">
        {/* Brand */}
        <div className="auth-brand">
          <div className="auth-logo">
            <Wallet size={28} />
          </div>
          <h1>ViPay</h1>
          <p>Ví điện tử thông minh của bạn</p>
        </div>

        {/* Card */}
        <div className="auth-card">
          <div className="auth-card-header">
            <h2>Chào mừng trở lại</h2>
            <p>Đăng nhập để quản lý ví của bạn</p>
          </div>

          <form onSubmit={handleSubmit} className="auth-form" noValidate>
            <div className="form-group">
              <label className="form-label">Số điện thoại</label>
              <div className="form-input-wrapper">
                <Phone size={16} className="form-input-icon" />
                <input
                  id="loginKey"
                  type="tel"
                  className={`form-input ${errors.loginKey ? 'input-error' : ''}`}
                  placeholder="0912 345 678"
                  value={form.loginKey}
                  onChange={e => setForm({ ...form, loginKey: e.target.value })}
                  autoComplete="tel"
                />
              </div>
              {errors.loginKey && <p className="form-error">{errors.loginKey}</p>}
            </div>

            <div className="form-group">
              <label className="form-label">Mật khẩu</label>
              <div className="form-input-wrapper">
                <Lock size={16} className="form-input-icon" />
                <input
                  id="password"
                  type={showPw ? 'text' : 'password'}
                  className={`form-input ${errors.password ? 'input-error' : ''}`}
                  placeholder="••••••••"
                  value={form.password}
                  onChange={e => setForm({ ...form, password: e.target.value })}
                  autoComplete="current-password"
                />
                <button type="button" className="form-input-action" onClick={() => setShowPw(!showPw)}>
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {errors.password && <p className="form-error">{errors.password}</p>}
            </div>

            <div className="auth-options">
              <Link to="/forgot-password" className="auth-link">Quên mật khẩu?</Link>
            </div>

            <button type="submit" className="btn btn-primary btn-full btn-lg" disabled={loading} id="btn-login">
              {loading ? <span className="spinner spinner-sm" /> : <ArrowRight size={18} />}
              {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
            </button>
          </form>

          <div className="divider-text" style={{ marginTop: '24px' }}>
            <span>Chưa có tài khoản?</span>
          </div>

          <Link to="/register" className="btn btn-secondary btn-full" style={{ marginTop: '12px', textDecoration: 'none' }} id="btn-goto-register">
            Tạo tài khoản mới
          </Link>
        </div>

        <p className="auth-footer-note">🔒 Bảo mật bởi JWT + Mã PIN giao dịch 2 lớp</p>
      </div>
    </div>
  );
}
