import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import {
  LayoutDashboard, ArrowLeftRight, ArrowUpRight, CreditCard,
  Settings, LogOut, Wallet, Shield
} from 'lucide-react';
import toast from 'react-hot-toast';
import './Sidebar.css';

const navItems = [
  { to: '/dashboard',     icon: LayoutDashboard, label: 'Tổng quan' },
  { to: '/transfer',      icon: ArrowLeftRight,  label: 'Chuyển tiền' },
  { to: '/deposit',       icon: Wallet,           label: 'Nạp tiền' },
  { to: '/withdraw',      icon: ArrowUpRight,     label: 'Rút tiền' },
  { to: '/history',       icon: CreditCard,       label: 'Lịch sử' },
  { to: '/settings',      icon: Settings,         label: 'Cài đặt' },
];

export default function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    toast.success('Đã đăng xuất thành công');
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      {/* Logo */}
      <div className="sidebar-logo">
        <div className="logo-icon">
          <Wallet size={22} />
        </div>
        <span className="logo-text">ViPay</span>
      </div>

      {/* User Card */}
      <div className="sidebar-user">
        <div className="user-avatar">
          {user?.displayName?.charAt(0)?.toUpperCase() || 'U'}
        </div>
        <div className="user-info">
          <p className="user-name">{user?.displayName || 'Người dùng'}</p>
          <p className="user-key">{user?.loginKey}</p>
        </div>
      </div>

      <hr className="sidebar-divider" />

      {/* Nav */}
      <nav className="sidebar-nav">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => `nav-item ${isActive ? 'nav-item--active' : ''}`}
          >
            <Icon size={18} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="security-badge">
          <Shield size={12} />
          <span>Bảo mật 2 lớp</span>
        </div>
        <button className="logout-btn" onClick={handleLogout}>
          <LogOut size={16} />
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>
  );
}
