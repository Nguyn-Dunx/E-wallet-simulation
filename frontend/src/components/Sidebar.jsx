import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import {
  LayoutDashboard, ArrowLeftRight, ArrowUpRight, CreditCard,
  Settings, LogOut, Wallet, Shield, UsersRound
} from 'lucide-react';
import toast from 'react-hot-toast';
import './Sidebar.css';

const userNavItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Tong quan' },
  { to: '/transfer', icon: ArrowLeftRight, label: 'Chuyen tien' },
  { to: '/deposit', icon: Wallet, label: 'Nap tien' },
  { to: '/withdraw', icon: ArrowUpRight, label: 'Rut tien' },
  { to: '/history', icon: CreditCard, label: 'Lich su' },
  { to: '/settings', icon: Settings, label: 'Cai dat' },
];

const adminNavItems = [
  { to: '/admin/accounts', icon: UsersRound, label: 'Tai khoan' },
];

export default function Sidebar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const navItems = isAdmin ? adminNavItems : userNavItems;

  const handleLogout = async () => {
    await logout();
    toast.success('Da dang xuat thanh cong');
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="logo-icon">
          <Wallet size={22} />
        </div>
        <span className="logo-text">ViPay</span>
      </div>

      <div className="sidebar-user">
        <div className="user-avatar">
          {user?.displayName?.charAt(0)?.toUpperCase() || 'U'}
        </div>
        <div className="user-info">
          <p className="user-name">{user?.displayName || 'Nguoi dung'}</p>
          <p className="user-key">{user?.loginKey}</p>
        </div>
      </div>

      <hr className="sidebar-divider" />

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
          <span>{isAdmin ? 'Quan tri he thong' : 'Bao mat 2 lop'}</span>
        </div>
        <button className="logout-btn" onClick={handleLogout}>
          <LogOut size={16} />
          <span>Dang xuat</span>
        </button>
      </div>
    </aside>
  );
}
