import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../api/client';
import { formatDateTime } from '../utils/format';
import {
  ChevronLeft,
  ChevronRight,
  Filter,
  Lock,
  RefreshCw,
  Search,
  ShieldCheck,
  Trash2,
  Unlock,
  UsersRound,
} from 'lucide-react';
import toast from 'react-hot-toast';
import './AdminAccountsPage.css';

const PAGE_SIZE = 10;

function getPageMeta(pageData) {
  return {
    totalPages: pageData.totalPages ?? pageData.page?.totalPages ?? 0,
    totalElements: pageData.totalElements ?? pageData.page?.totalElements ?? 0,
  };
}

function getStatusClass(status) {
  if (status === 'ACTIVE') return 'success';
  if (status === 'LOCKED') return 'warning';
  if (status === 'DISABLED') return 'danger';
  return 'primary';
}

export default function AdminAccountsPage() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionId, setActionId] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filters, setFilters] = useState({ keyword: '', status: '', role: 'USER' });

  const loadAccounts = useCallback(async () => {
    setLoading(true);
    try {
      const res = await adminApi.getAccounts({
        page,
        size: PAGE_SIZE,
        keyword: filters.keyword.trim(),
        status: filters.status,
        role: filters.role,
      });
      const data = res.data || res;
      const meta = getPageMeta(data);
      setAccounts(data.content || []);
      setTotalPages(meta.totalPages);
      setTotalElements(meta.totalElements);
    } catch (err) {
      setAccounts([]);
      toast.error(err.message || 'Khong the tai danh sach tai khoan');
    } finally {
      setLoading(false);
    }
  }, [filters.keyword, filters.role, filters.status, page]);

  useEffect(() => {
    loadAccounts();
  }, [loadAccounts]);

  const updateFilter = (key, value) => {
    setFilters((current) => ({ ...current, [key]: value }));
    setPage(0);
  };

  const runAction = async (account, action) => {
    const messages = {
      lock: `Khoa tai khoan ${account.loginKey}?`,
      unlock: `Mo khoa tai khoan ${account.loginKey}?`,
      delete: `Xoa tai khoan ${account.loginKey}?`,
    };

    if (!confirm(messages[action])) return;

    setActionId(account.id);
    try {
      if (action === 'lock') await adminApi.lockAccount(account.id);
      if (action === 'unlock') await adminApi.unlockAccount(account.id);
      if (action === 'delete') await adminApi.deleteAccount(account.id);
      toast.success('Cap nhat tai khoan thanh cong');
      await loadAccounts();
    } catch (err) {
      toast.error(err.message || 'Thao tac that bai');
    } finally {
      setActionId(null);
    }
  };

  return (
    <div className="page-wrapper admin-page animate-fade-in">
      <div className="admin-header">
        <div>
          <p className="admin-eyebrow">Admin panel</p>
          <h1>Quan ly tai khoan</h1>
          <p className="text-secondary">Theo doi va xu ly trang thai tai khoan nguoi dung.</p>
        </div>
        <button className="btn btn-secondary" onClick={loadAccounts} disabled={loading}>
          <RefreshCw size={16} />
          Lam moi
        </button>
      </div>

      <div className="admin-summary-grid">
        <div className="admin-summary-card">
          <div className="admin-summary-icon"><UsersRound size={20} /></div>
          <div>
            <p>Tong ket qua</p>
            <strong>{totalElements}</strong>
          </div>
        </div>
        <div className="admin-summary-card">
          <div className="admin-summary-icon admin-summary-icon--shield"><ShieldCheck size={20} /></div>
          <div>
            <p>Pham vi</p>
            <strong>{filters.role || 'ALL'}</strong>
          </div>
        </div>
      </div>

      <div className="admin-toolbar">
        <div className="form-input-wrapper admin-search">
          <Search size={16} className="form-input-icon" />
          <input
            className="form-input"
            placeholder="Tim login key, ten, so dien thoai..."
            value={filters.keyword}
            onChange={(e) => updateFilter('keyword', e.target.value)}
          />
        </div>
        <div className="admin-filter-group">
          <Filter size={16} />
          <select
            className="form-input"
            value={filters.status}
            onChange={(e) => updateFilter('status', e.target.value)}
          >
            <option value="">Moi trang thai</option>
            <option value="ACTIVE">Active</option>
            <option value="LOCKED">Locked</option>
            <option value="DISABLED">Disabled</option>
          </select>
          <select
            className="form-input"
            value={filters.role}
            onChange={(e) => updateFilter('role', e.target.value)}
          >
            <option value="">Moi vai tro</option>
            <option value="USER">User</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>
      </div>

      <div className="admin-table-card">
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Tai khoan</th>
                <th>Vai tro</th>
                <th>Trang thai</th>
                <th>Ngay tao</th>
                <th>Thao tac</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={5}>
                    <div className="admin-empty">Dang tai danh sach...</div>
                  </td>
                </tr>
              ) : accounts.length === 0 ? (
                <tr>
                  <td colSpan={5}>
                    <div className="admin-empty">Khong co tai khoan phu hop</div>
                  </td>
                </tr>
              ) : accounts.map((account) => {
                const isAdminAccount = account.role === 'ADMIN';
                const disabled = actionId === account.id || isAdminAccount || account.status === 'DISABLED';

                return (
                  <tr key={account.id}>
                    <td>
                      <div className="admin-account-cell">
                        <div className="admin-avatar">{account.displayName?.charAt(0)?.toUpperCase() || 'U'}</div>
                        <div>
                          <strong>{account.displayName || account.loginKey}</strong>
                          <span>{account.loginKey}</span>
                          {account.phone && <span>{account.phone}</span>}
                          {account.employeeCode && <span>{account.employeeCode}</span>}
                        </div>
                      </div>
                    </td>
                    <td><span className="badge badge-primary">{account.role}</span></td>
                    <td><span className={`badge badge-${getStatusClass(account.status)}`}>{account.status}</span></td>
                    <td>{formatDateTime(account.createdAt)}</td>
                    <td>
                      <div className="admin-actions">
                        {account.status === 'LOCKED' ? (
                          <button
                            className="btn btn-secondary btn-sm"
                            disabled={disabled}
                            onClick={() => runAction(account, 'unlock')}
                          >
                            <Unlock size={14} /> Mo
                          </button>
                        ) : (
                          <button
                            className="btn btn-secondary btn-sm"
                            disabled={disabled || account.status !== 'ACTIVE'}
                            onClick={() => runAction(account, 'lock')}
                          >
                            <Lock size={14} /> Khoa
                          </button>
                        )}
                        <button
                          className="btn btn-danger btn-sm"
                          disabled={disabled}
                          onClick={() => runAction(account, 'delete')}
                        >
                          <Trash2 size={14} /> Xoa
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button className="btn btn-secondary btn-sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            <ChevronLeft size={16} /> Truoc
          </button>
          <span className="pagination-info">Trang {page + 1} / {totalPages}</span>
          <button className="btn btn-secondary btn-sm" disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>
            Sau <ChevronRight size={16} />
          </button>
        </div>
      )}
    </div>
  );
}
