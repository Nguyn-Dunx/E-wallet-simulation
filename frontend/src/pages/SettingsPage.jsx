import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { authApi, walletApi } from "../api/client";
import { useAuth } from "../contexts/AuthContext";
import {
  Lock,
  Eye,
  EyeOff,
  Shield,
  KeyRound,
  ChevronRight,
  User,
  CreditCard,
  Trash2,
  Plus,
} from "lucide-react";
import toast from "react-hot-toast";
import "./SettingsPage.css";

function LinkedBanksForm() {
  const [banks, setBanks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ bankName: "", accountNumber: "" });

  const loadBanks = useCallback(async () => {
    try {
      const res = await walletApi.getLinkedSources();
      setBanks(res.data || res || []);
    } catch {
      toast.error("Không thể tải danh sách ngân hàng");
    }
  }, []);

  useEffect(() => {
    loadBanks();
  }, [loadBanks]);

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!form.bankName.trim() || !form.accountNumber.trim()) {
      toast.error("Vui lòng nhập đầy đủ thông tin");
      return;
    }
    setLoading(true);
    try {
      await walletApi.addLinkedSource(form);
      toast.success("Đã liên kết ngân hàng thành công!");
      setForm({ bankName: "", accountNumber: "" });
      loadBanks();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleUnlink = async (id) => {
    if (!confirm("Bạn có chắc chắn muốn hủy liên kết ngân hàng này?")) return;
    try {
      await walletApi.deleteLinkedSource(id);
      toast.success("Hủy liên kết thành công");
      loadBanks();
    } catch (err) {
      toast.error(err.message);
    }
  };

  return (
    <div className="settings-form">
      <div
        className="linked-banks-list"
        style={{
          display: "flex",
          flexDirection: "column",
          gap: "var(--space-md)",
          marginBottom: "var(--space-lg)",
        }}
      >
        {banks.length === 0 ? (
          <p className="text-muted text-sm text-center">
            Bạn chưa liên kết ngân hàng nào.
          </p>
        ) : (
          banks.map((b) => (
            <div
              key={b.id}
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                padding: "12px",
                background: "var(--bg-glass)",
                border: "1px solid var(--border)",
                borderRadius: "var(--radius-md)",
              }}
            >
              <div>
                <p className="font-semibold">{b.bankName}</p>
                <p className="text-sm text-muted">{b.accountNumber}</p>
              </div>
              <button
                className="btn btn-ghost"
                style={{ color: "var(--danger)" }}
                onClick={() => handleUnlink(b.id)}
              >
                <Trash2 size={16} /> Hủy liên kết
              </button>
            </div>
          ))
        )}
      </div>

      <form
        onSubmit={handleAdd}
        style={{
          display: "flex",
          flexDirection: "column",
          gap: "var(--space-md)",
          borderTop: "1px solid var(--border)",
          paddingTop: "var(--space-lg)",
        }}
      >
        <h4>Liên kết ngân hàng mới</h4>
        <div className="form-group">
          <label className="form-label">Tên ngân hàng</label>
          <input
            type="text"
            className="form-input"
            placeholder="VD: Vietcombank"
            value={form.bankName}
            onChange={(e) => setForm({ ...form, bankName: e.target.value })}
          />
        </div>
        <div className="form-group">
          <label className="form-label">Số tài khoản / Số thẻ</label>
          <input
            type="text"
            className="form-input"
            placeholder="VD: 1012345678"
            value={form.accountNumber}
            onChange={(e) =>
              setForm({ ...form, accountNumber: e.target.value })
            }
          />
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? (
            <span className="spinner spinner-sm" />
          ) : (
            <Plus size={16} />
          )}{" "}
          Thêm liên kết
        </button>
      </form>
    </div>
  );
}

function ChangePasswordForm() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [show, setShow] = useState({ old: false, new: false });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.newPassword !== form.confirmPassword) {
      toast.error("Mật khẩu xác nhận không khớp");
      return;
    }
    if (form.newPassword.length < 6) {
      toast.error("Mật khẩu mới ít nhất 6 ký tự");
      return;
    }
    setLoading(true);
    try {
      await authApi.changePassword({
        oldPassword: form.oldPassword,
        newPassword: form.newPassword,
      });
      toast.success("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
      setForm({ oldPassword: "", newPassword: "", confirmPassword: "" });
      await logout({ localOnly: true });
      navigate("/login", { replace: true });
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="settings-form">
      <div className="form-group">
        <label className="form-label">Mật khẩu hiện tại</label>
        <div className="form-input-wrapper">
          <Lock size={16} className="form-input-icon" />
          <input
            id="oldPassword"
            type={show.old ? "text" : "password"}
            className="form-input"
            placeholder="••••••••"
            value={form.oldPassword}
            onChange={(e) => setForm({ ...form, oldPassword: e.target.value })}
          />
          <button
            type="button"
            className="form-input-action"
            onClick={() => setShow((s) => ({ ...s, old: !s.old }))}
          >
            {show.old ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>
      </div>
      <div className="form-group">
        <label className="form-label">Mật khẩu mới</label>
        <div className="form-input-wrapper">
          <Lock size={16} className="form-input-icon" />
          <input
            id="newPassword"
            type={show.new ? "text" : "password"}
            className="form-input"
            placeholder="Ít nhất 6 ký tự"
            value={form.newPassword}
            onChange={(e) => setForm({ ...form, newPassword: e.target.value })}
          />
          <button
            type="button"
            className="form-input-action"
            onClick={() => setShow((s) => ({ ...s, new: !s.new }))}
          >
            {show.new ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>
      </div>
      <div className="form-group">
        <label className="form-label">Xác nhận mật khẩu mới</label>
        <div className="form-input-wrapper">
          <Lock size={16} className="form-input-icon" />
          <input
            id="confirmPassword"
            type="password"
            className="form-input"
            placeholder="Nhập lại mật khẩu mới"
            value={form.confirmPassword}
            onChange={(e) =>
              setForm({ ...form, confirmPassword: e.target.value })
            }
          />
        </div>
      </div>
      <button
        type="submit"
        id="btn-change-password"
        className="btn btn-primary"
        disabled={loading}
      >
        {loading ? <span className="spinner spinner-sm" /> : null}
        Cập nhật mật khẩu
      </button>
    </form>
  );
}

function SetPinForm() {
  const [form, setForm] = useState({ pin: "", confirmPin: "" });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!/^\d{6}$/.test(form.pin)) {
      toast.error("Mã PIN phải là 6 chữ số");
      return;
    }
    if (form.pin !== form.confirmPin) {
      toast.error("Mã PIN xác nhận không khớp");
      return;
    }
    setLoading(true);
    try {
      await authApi.setPin({ pin: form.pin, confirmPin: form.confirmPin });
      toast.success("Đã thiết lập mã PIN giao dịch!");
      setForm({ pin: "", confirmPin: "" });
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="settings-form">
      <div className="form-group">
        <label className="form-label">Mã PIN mới (6 số)</label>
        <input
          id="pinNew"
          type="password"
          inputMode="numeric"
          maxLength={6}
          className="form-input"
          placeholder="••••••"
          value={form.pin}
          onChange={(e) =>
            setForm({ ...form, pin: e.target.value.replace(/\D/, "") })
          }
        />
      </div>
      <div className="form-group">
        <label className="form-label">Xác nhận mã PIN</label>
        <input
          id="pinConfirm"
          type="password"
          inputMode="numeric"
          maxLength={6}
          className="form-input"
          placeholder="••••••"
          value={form.confirmPin}
          onChange={(e) =>
            setForm({ ...form, confirmPin: e.target.value.replace(/\D/, "") })
          }
        />
      </div>
      <button
        type="submit"
        id="btn-set-pin"
        className="btn btn-primary"
        disabled={loading}
      >
        {loading ? <span className="spinner spinner-sm" /> : null}
        Thiết lập mã PIN
      </button>
    </form>
  );
}

function ChangePinForm() {
  const [form, setForm] = useState({
    oldPin: "",
    newPin: "",
    confirmNewPin: "",
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.newPin !== form.confirmNewPin) {
      toast.error("Mã PIN mới không khớp");
      return;
    }
    setLoading(true);
    try {
      await authApi.changePin({
        oldPin: form.oldPin,
        newPin: form.newPin,
        confirmNewPin: form.confirmNewPin,
      });
      toast.success("Đổi mã PIN thành công!");
      setForm({ oldPin: "", newPin: "", confirmNewPin: "" });
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="settings-form">
      <div className="form-group">
        <label className="form-label">PIN hiện tại</label>
        <input
          id="oldPin"
          type="password"
          inputMode="numeric"
          maxLength={6}
          className="form-input"
          placeholder="••••••"
          value={form.oldPin}
          onChange={(e) =>
            setForm({ ...form, oldPin: e.target.value.replace(/\D/, "") })
          }
        />
      </div>
      <div className="form-group">
        <label className="form-label">PIN mới (6 số)</label>
        <input
          id="newPin"
          type="password"
          inputMode="numeric"
          maxLength={6}
          className="form-input"
          placeholder="••••••"
          value={form.newPin}
          onChange={(e) =>
            setForm({ ...form, newPin: e.target.value.replace(/\D/, "") })
          }
        />
      </div>
      <div className="form-group">
        <label className="form-label">Xác nhận PIN mới</label>
        <input
          id="confirmNewPin"
          type="password"
          inputMode="numeric"
          maxLength={6}
          className="form-input"
          placeholder="••••••"
          value={form.confirmNewPin}
          onChange={(e) =>
            setForm({
              ...form,
              confirmNewPin: e.target.value.replace(/\D/, ""),
            })
          }
        />
      </div>
      <button
        type="submit"
        id="btn-change-pin"
        className="btn btn-primary"
        disabled={loading}
      >
        {loading ? <span className="spinner spinner-sm" /> : null}
        Đổi mã PIN
      </button>
    </form>
  );
}

export default function SettingsPage() {
  const { user } = useAuth();
  const [activeSection, setActiveSection] = useState(null);

  const sections = [
    {
      id: "profile",
      icon: User,
      title: "Thông tin tài khoản",
      description: "Xem thông tin tài khoản của bạn",
      content: (
        <div className="profile-info">
          <div className="profile-row">
            <span className="text-muted">Tên hiển thị</span>
            <span className="font-semibold">{user?.displayName}</span>
          </div>
          <div className="profile-row">
            <span className="text-muted">Số điện thoại</span>
            <span className="font-semibold">{user?.loginKey}</span>
          </div>
          <div className="profile-row">
            <span className="text-muted">Vai trò</span>
            <span className="badge badge-primary">
              {user?.roles?.[0]?.replace("ROLE_", "")}
            </span>
          </div>
        </div>
      ),
    },
    {
      id: "banks",
      icon: CreditCard,
      title: "Nguồn tiền",
      description: "Quản lý thẻ / tài khoản ngân hàng",
      content: <LinkedBanksForm />,
    },
    {
      id: "password",
      icon: Lock,
      title: "Đổi mật khẩu",
      description: "Cập nhật mật khẩu đăng nhập",
      content: <ChangePasswordForm />,
    },
    {
      id: "set-pin",
      icon: Shield,
      title: "Thiết lập mã PIN",
      description: "Đặt mã PIN lần đầu",
      content: <SetPinForm />,
    },
    {
      id: "change-pin",
      icon: KeyRound,
      title: "Đổi mã PIN",
      description: "Thay đổi mã PIN giao dịch",
      content: <ChangePinForm />,
    },
  ];

  return (
    <div className="page-wrapper animate-fade-in">
      <h1 style={{ marginBottom: "var(--space-xl)" }}>Cài đặt</h1>
      <div className="settings-layout">
        {/* Sections list */}
        <div className="settings-menu card">
          {sections.map((s) => (
            <button
              key={s.id}
              id={`settings-${s.id}`}
              className={`settings-menu-item ${activeSection === s.id ? "active" : ""}`}
              onClick={() =>
                setActiveSection(activeSection === s.id ? null : s.id)
              }
            >
              <div className="settings-menu-icon">
                <s.icon size={16} />
              </div>
              <div className="settings-menu-text">
                <span className="settings-menu-title">{s.title}</span>
                <span className="settings-menu-desc">{s.description}</span>
              </div>
              <ChevronRight
                size={16}
                className={`settings-chevron ${activeSection === s.id ? "rotated" : ""}`}
              />
            </button>
          ))}
        </div>

        {/* Active content */}
        {activeSection && (
          <div className="settings-content card animate-fade-in">
            {sections.find((s) => s.id === activeSection)?.content}
          </div>
        )}
      </div>
    </div>
  );
}
