import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  KeyRound,
  Lock,
  Phone,
  ShieldCheck,
} from "lucide-react";
import toast from "react-hot-toast";
import { resetPasswordApi } from "../api/resetPasswordApi";
import "./Auth.css";

const PHONE_STORAGE_KEY = "forgotPasswordPhone";
const TOKEN_STORAGE_KEY = "forgotPasswordResetToken";
const OTP_LENGTH = 6;

function getInitialState() {
  const storedPhone = localStorage.getItem(PHONE_STORAGE_KEY) || "";
  const storedToken = localStorage.getItem(TOKEN_STORAGE_KEY) || "";
  const step = storedPhone && storedToken ? 3 : storedPhone ? 2 : 1;

  return { storedPhone, storedToken, step };
}

function createEmptyOtp() {
  return Array.from({ length: OTP_LENGTH }, () => "");
}

function normalizePhone(phone) {
  return phone.replace(/\s+/g, "").trim();
}

function getErrorMessage(error, fallback) {
  return error?.message || fallback;
}

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const initialState = getInitialState();
  const [step, setStep] = useState(initialState.step);
  const [phone, setPhone] = useState(initialState.storedPhone);
  const [resetToken, setResetToken] = useState(initialState.storedToken);
  const [otpDigits, setOtpDigits] = useState(createEmptyOtp);
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const otpRefs = useRef([]);

  useEffect(() => {
    if (step === 2) {
      otpRefs.current[0]?.focus();
    }
  }, [step]);

  const clearStorage = () => {
    localStorage.removeItem(PHONE_STORAGE_KEY);
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  };

  const resetToPhoneStep = () => {
    clearStorage();
    setStep(1);
    setPhone("");
    setResetToken("");
    setOtpDigits(createEmptyOtp());
    setNewPassword("");
    setConfirmPassword("");
    setErrors({});
  };

  const savePhone = (value) => {
    localStorage.setItem(PHONE_STORAGE_KEY, value);
  };

  const saveResetToken = (value) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, value);
  };

  const handlePhoneSubmit = async (event) => {
    event.preventDefault();
    const normalizedPhone = normalizePhone(phone);

    if (!normalizedPhone) {
      setErrors({ phone: "Vui lòng nhập số điện thoại" });
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      await resetPasswordApi.createOtp(normalizedPhone);
      savePhone(normalizedPhone);
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      setPhone(normalizedPhone);
      setResetToken("");
      setOtpDigits(createEmptyOtp());
      setStep(2);
      toast.success("Đã gửi OTP đến số điện thoại của bạn");
    } catch (error) {
      const message = getErrorMessage(error, "Không thể gửi OTP");
      toast.error(message);
      setErrors({ phone: message });
    } finally {
      setLoading(false);
    }
  };

  const handleOtpChange = (index, value) => {
    const digit = value.replace(/\D/g, "").slice(-1);
    const nextOtp = [...otpDigits];
    nextOtp[index] = digit;
    setOtpDigits(nextOtp);

    if (digit && index < OTP_LENGTH - 1) {
      otpRefs.current[index + 1]?.focus();
    }
  };

  const handleOtpKeyDown = (index, event) => {
    if (event.key === "Backspace" && !otpDigits[index] && index > 0) {
      otpRefs.current[index - 1]?.focus();
    }
  };

  const handleOtpPaste = (event) => {
    event.preventDefault();
    const pasted = event.clipboardData
      .getData("text")
      .replace(/\D/g, "")
      .slice(0, OTP_LENGTH)
      .split("");

    if (!pasted.length) return;

    const nextOtp = createEmptyOtp();
    pasted.forEach((digit, index) => {
      nextOtp[index] = digit;
    });

    setOtpDigits(nextOtp);
    otpRefs.current[Math.min(pasted.length, OTP_LENGTH - 1)]?.focus();
  };

  const handleOtpSubmit = async (event) => {
    event.preventDefault();
    const otp = otpDigits.join("");

    if (!phone) {
      toast.error("Thiếu số điện thoại để xác thực OTP");
      setStep(1);
      return;
    }

    if (otp.length !== OTP_LENGTH) {
      setErrors({ otp: "Vui lòng nhập đủ 6 số OTP" });
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      const response = await resetPasswordApi.verifyOtp(phone, otp);
      const token = response?.data || response?.token || response;

      if (!token) {
        throw new Error("Không nhận được reset token từ server");
      }

      saveResetToken(token);
      setResetToken(token);
      setStep(3);
      toast.success("Xác thực OTP thành công");
    } catch (error) {
      const message = getErrorMessage(error, "OTP không hợp lệ");
      toast.error(message);
      setErrors({ otp: message });
    } finally {
      setLoading(false);
    }
  };

  const handleResetSubmit = async (event) => {
    event.preventDefault();

    if (!resetToken) {
      toast.error("Thiếu reset token, vui lòng xác thực OTP lại");
      setStep(2);
      return;
    }

    if (!newPassword) {
      setErrors({ password: "Vui lòng nhập mật khẩu mới" });
      return;
    }

    if (newPassword.length < 6) {
      setErrors({ password: "Mật khẩu mới phải có ít nhất 6 ký tự" });
      return;
    }

    if (newPassword !== confirmPassword) {
      setErrors({ confirmPassword: "Mật khẩu xác nhận không khớp" });
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      await resetPasswordApi.confirmResetPassword(resetToken, newPassword);
      clearStorage();
      setPhone("");
      setResetToken("");
      setOtpDigits(createEmptyOtp());
      setNewPassword("");
      setConfirmPassword("");
      setStep(1);
      toast.success("Đặt lại mật khẩu thành công");
      navigate("/login", { replace: true });
    } catch (error) {
      const message = getErrorMessage(error, "Không thể đặt lại mật khẩu");
      toast.error(message);
      setErrors({ password: message });
    } finally {
      setLoading(false);
    }
  };

  const handleBackToPhone = () => {
    resetToPhoneStep();
  };

  const handleBackToOtp = () => {
    setStep(2);
    setErrors({});
  };

  const steps = [
    { id: 1, label: "Số điện thoại" },
    { id: 2, label: "OTP" },
    { id: 3, label: "Mật khẩu mới" },
  ];

  return (
    <div className="auth-page">
      <div className="auth-bg-glow" />

      <div className="auth-container animate-fade-in">
        <div className="auth-brand">
          <div className="auth-logo">
            <ShieldCheck size={28} />
          </div>
          <h1>ViPay</h1>
          <p>Khôi phục mật khẩu an toàn trong 3 bước</p>
        </div>

        <div className="auth-card">
          <div className="steps" aria-label="Quy trình quên mật khẩu">
            {steps.map((item, index) => (
              <div key={item.id} style={{ display: "contents" }}>
                <div
                  className={`step ${step > item.id ? "done" : ""} ${step === item.id ? "active" : ""}`}
                >
                  <div className="step-circle">{item.id}</div>
                  <div className="step-label">{item.label}</div>
                </div>
                {index < steps.length - 1 && <div className="step-line" />}
              </div>
            ))}
          </div>

          <div className="auth-card-header">
            {step === 1 && (
              <>
                <h2>Nhập số điện thoại</h2>
                <p>Hệ thống sẽ gửi mã OTP đến số bạn đăng ký.</p>
              </>
            )}
            {step === 2 && (
              <>
                <h2>Xác thực OTP</h2>
                <p>
                  Mã OTP đã được gửi đến {phone || "số điện thoại của bạn"}.
                </p>
              </>
            )}
            {step === 3 && (
              <>
                <h2>Đặt mật khẩu mới</h2>
                <p>Nhập mật khẩu mới để hoàn tất quá trình khôi phục.</p>
              </>
            )}
          </div>

          {step === 1 && (
            <form onSubmit={handlePhoneSubmit} className="auth-form" noValidate>
              <div className="form-group">
                <label className="form-label">Số điện thoại</label>
                <div className="form-input-wrapper">
                  <Phone size={16} className="form-input-icon" />
                  <input
                    id="forgotPhone"
                    type="tel"
                    className={`form-input ${errors.phone ? "input-error" : ""}`}
                    placeholder="039xxxxxxx"
                    value={phone}
                    onChange={(event) => setPhone(event.target.value)}
                    autoComplete="tel"
                  />
                </div>
                {errors.phone && <p className="form-error">{errors.phone}</p>}
              </div>

              <button
                type="submit"
                className="btn btn-primary btn-full btn-lg"
                disabled={loading}
              >
                {loading ? (
                  <span className="spinner spinner-sm" />
                ) : (
                  <ArrowRight size={18} />
                )}
                {loading ? "Đang gửi OTP..." : "Gửi OTP"}
              </button>
            </form>
          )}

          {step === 2 && (
            <form onSubmit={handleOtpSubmit} className="auth-form" noValidate>
              <div className="form-group">
                <label className="form-label">Mã OTP</label>
                <div className="otp-inputs" onPaste={handleOtpPaste}>
                  {otpDigits.map((digit, index) => (
                    <input
                      key={index}
                      ref={(element) => {
                        otpRefs.current[index] = element;
                      }}
                      type="text"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      maxLength={1}
                      className={`otp-input ${digit ? "otp-filled" : ""}`}
                      value={digit}
                      onChange={(event) =>
                        handleOtpChange(index, event.target.value)
                      }
                      onKeyDown={(event) => handleOtpKeyDown(index, event)}
                      aria-label={`OTP digit ${index + 1}`}
                    />
                  ))}
                </div>
                {errors.otp && (
                  <p className="form-error" style={{ textAlign: "center" }}>
                    {errors.otp}
                  </p>
                )}
              </div>

              <button
                type="submit"
                className="btn btn-primary btn-full btn-lg"
                disabled={loading}
              >
                {loading ? (
                  <span className="spinner spinner-sm" />
                ) : (
                  <KeyRound size={18} />
                )}
                {loading ? "Đang xác thực..." : "Xác thực OTP"}
              </button>

              <div style={{ display: "flex", gap: 12 }}>
                <button
                  type="button"
                  className="btn btn-secondary btn-full"
                  onClick={handleBackToPhone}
                  disabled={loading}
                >
                  <ArrowLeft size={16} />
                  Đổi số điện thoại
                </button>
                <Link
                  to="/login"
                  className="btn btn-ghost btn-full"
                  style={{ textAlign: "center" }}
                >
                  Về đăng nhập
                </Link>
              </div>
            </form>
          )}

          {step === 3 && (
            <form onSubmit={handleResetSubmit} className="auth-form" noValidate>
              <div className="form-group">
                <label className="form-label">Mật khẩu mới</label>
                <div className="form-input-wrapper">
                  <Lock size={16} className="form-input-icon" />
                  <input
                    id="newPassword"
                    type="password"
                    className={`form-input ${errors.password ? "input-error" : ""}`}
                    placeholder="Ít nhất 6 ký tự"
                    value={newPassword}
                    onChange={(event) => setNewPassword(event.target.value)}
                    autoComplete="new-password"
                  />
                </div>
                {errors.password && (
                  <p className="form-error">{errors.password}</p>
                )}
              </div>

              <div className="form-group">
                <label className="form-label">Xác nhận mật khẩu mới</label>
                <div className="form-input-wrapper">
                  <Lock size={16} className="form-input-icon" />
                  <input
                    id="confirmPassword"
                    type="password"
                    className={`form-input ${errors.confirmPassword ? "input-error" : ""}`}
                    placeholder="Nhập lại mật khẩu mới"
                    value={confirmPassword}
                    onChange={(event) => setConfirmPassword(event.target.value)}
                    autoComplete="new-password"
                  />
                </div>
                {errors.confirmPassword && (
                  <p className="form-error">{errors.confirmPassword}</p>
                )}
              </div>

              <button
                type="submit"
                className="btn btn-primary btn-full btn-lg"
                disabled={loading}
              >
                {loading ? (
                  <span className="spinner spinner-sm" />
                ) : (
                  <ShieldCheck size={18} />
                )}
                {loading ? "Đang cập nhật..." : "Đặt lại mật khẩu"}
              </button>

              <div style={{ display: "flex", gap: 12 }}>
                <button
                  type="button"
                  className="btn btn-secondary btn-full"
                  onClick={handleBackToOtp}
                  disabled={loading}
                >
                  <ArrowLeft size={16} />
                  Quay lại OTP
                </button>
                <button
                  type="button"
                  className="btn btn-ghost btn-full"
                  onClick={resetToPhoneStep}
                  disabled={loading}
                >
                  <Phone size={16} />
                  Đổi số điện thoại
                </button>
                <Link
                  to="/login"
                  className="btn btn-ghost btn-full"
                  style={{ textAlign: "center" }}
                >
                  Về đăng nhập
                </Link>
              </div>
            </form>
          )}
        </div>

        <p className="auth-footer-note">
          <Link to="/login">Quay lại trang đăng nhập</Link>
        </p>
      </div>
    </div>
  );
}
