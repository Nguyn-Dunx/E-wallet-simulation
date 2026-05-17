import { useState, useEffect, useCallback } from 'react';
import { transactionApi, walletApi } from '../api/client';
import TransactionItem from '../components/TransactionItem';
import { formatCurrency, formatDateTime, getStatusLabel } from '../utils/format';
import { Search, ChevronLeft, ChevronRight } from 'lucide-react';
import './HistoryPage.css';

export default function HistoryPage() {
  const [txns, setTxns]   = useState([]);
  const [wallet, setWallet] = useState(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage]   = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');
  const [range, setRange] = useState('all');
  const [selected, setSelected] = useState(null);
  const PAGE_SIZE = 10;

  const getRangeDates = (value) => {
    if (value === 'all') return {};
    const now = new Date();
    let start = new Date(now);
    if (value === 'day') {
      start.setHours(0, 0, 0, 0);
    } else if (value === 'week') {
      const dayIndex = (now.getDay() + 6) % 7;
      start.setDate(now.getDate() - dayIndex);
      start.setHours(0, 0, 0, 0);
    } else if (value === 'month') {
      start = new Date(now.getFullYear(), now.getMonth(), 1);
    } else if (value === 'year') {
      start = new Date(now.getFullYear(), 0, 1);
    }
    return { startDate: start.toISOString(), endDate: now.toISOString() };
  };

  const fetchWallet = useCallback(async () => {
    try { const res = await walletApi.getMyWallet(); setWallet(res.data || res); } catch (_) {}
  }, []);

  const fetchHistory = useCallback(async () => {
    setLoading(true);
    try {
      const { startDate, endDate } = getRangeDates(range);
      const res = await transactionApi.getHistory({
        page,
        size: PAGE_SIZE,
        startDate,
        endDate,
      });
      const d = res.data || res;
      setTxns(d.content || []);
      setTotalPages(d.totalPages || 0);
    } catch (_) { setTxns([]); }
    finally { setLoading(false); }
  }, [page, range]);

  useEffect(() => { fetchWallet(); }, [fetchWallet]);
  useEffect(() => { fetchHistory(); }, [fetchHistory]);

  const filtered = txns.filter(t =>
    !search || t.transactionCode?.toLowerCase().includes(search.toLowerCase()) ||
    t.description?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="page-wrapper animate-fade-in">
      <div className="history-header">
        <h1>Lịch sử giao dịch</h1>
        <p className="text-secondary">Theo dõi toàn bộ giao dịch của bạn</p>
      </div>

      {/* Search */}
      <div className="history-toolbar">
        <div className="form-input-wrapper" style={{ flex: 1 }}>
          <Search size={16} className="form-input-icon" />
          <input
            id="search-txn"
            type="text"
            className="form-input"
            placeholder="Tìm kiếm mã giao dịch, lời nhắn..."
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
        <div className="filter-pills">
          {[
            { value: 'all', label: 'Tất cả' },
            { value: 'day', label: 'Ngày' },
            { value: 'week', label: 'Tuần' },
            { value: 'month', label: 'Tháng' },
            { value: 'year', label: 'Năm' },
          ].map(opt => (
            <button
              key={opt.value}
              className={`filter-pill ${range === opt.value ? 'active' : ''}`}
              onClick={() => { setRange(opt.value); setPage(0); }}
              type="button"
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      {/* List */}
      <div className="card history-list">
        {loading ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} style={{ display: 'flex', gap: 12, alignItems: 'center', padding: '12px 0' }}>
                <div className="skeleton skeleton-circle" style={{ width: 42, height: 42, flexShrink: 0 }} />
                <div style={{ flex: 1 }}>
                  <div className="skeleton skeleton-text" style={{ width: '45%', marginBottom: 6 }} />
                  <div className="skeleton skeleton-text" style={{ width: '25%', height: 10 }} />
                </div>
                <div className="skeleton skeleton-text" style={{ width: 80, height: 18 }} />
              </div>
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">🔍</div>
            <h3>Không tìm thấy giao dịch nào</h3>
            <p>{search ? `Không có kết quả cho "${search}"` : 'Bạn chưa có giao dịch nào'}</p>
          </div>
        ) : (
          <>
            {filtered.map(txn => (
              <TransactionItem
                key={txn.transactionCode}
                txn={txn}
                walletId={wallet?.id}
                onClick={setSelected}
              />
            ))}
          </>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button className="btn btn-secondary btn-sm" disabled={page === 0} onClick={() => setPage(p => p - 1)} id="btn-prev-page">
            <ChevronLeft size={16} /> Trước
          </button>
          <span className="pagination-info">Trang {page + 1} / {totalPages}</span>
          <button className="btn btn-secondary btn-sm" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} id="btn-next-page">
            Sau <ChevronRight size={16} />
          </button>
        </div>
      )}

      {/* Detail Modal */}
      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal animate-scale-in" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Chi tiết giao dịch</h3>
              <button className="modal-close" onClick={() => setSelected(null)}>✕</button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
              {[
                { label: 'Mã giao dịch', value: selected.transactionCode },
                { label: 'Loại giao dịch', value: selected.type },
                { label: 'Số tiền', value: formatCurrency(selected.amount) },
                { label: 'Trạng thái', value: (
                  <span className={`badge badge-${selected.status === 'SUCCESS' ? 'success' : selected.status === 'FAILED' ? 'danger' : 'warning'}`}>
                    {getStatusLabel(selected.status)}
                  </span>
                )},
                { label: 'Lời nhắn', value: selected.description || '—' },
                { label: 'Thời gian', value: formatDateTime(selected.createdAt) },
              ].map(({ label, value }) => (
                <div key={label} className="flex justify-between items-center" style={{ borderBottom: '1px solid var(--border)', paddingBottom: 10 }}>
                  <span className="text-muted text-sm">{label}</span>
                  <span className="font-semibold text-sm">{value}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
