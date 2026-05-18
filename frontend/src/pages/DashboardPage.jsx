import { useState, useEffect, useCallback } from 'react';
import { walletApi, transactionApi } from '../api/client';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import TransactionItem from '../components/TransactionItem';
import { formatCurrency } from '../utils/format';
import {
  Eye, EyeOff, ArrowUpRight, ArrowDownLeft, Plus,
  TrendingUp, TrendingDown, ArrowLeftRight, ChevronRight
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import toast from 'react-hot-toast';
import './Dashboard.css';

function SkeletonCard() {
  return (
    <div className="wallet-card skeleton-card">
      <div className="skeleton skeleton-title" style={{ width: '40%', marginBottom: 12 }} />
      <div className="skeleton skeleton-title" style={{ width: '65%', height: 36, marginBottom: 24 }} />
      <div style={{ display: 'flex', gap: 12 }}>
        <div className="skeleton skeleton-text" style={{ width: '30%' }} />
        <div className="skeleton skeleton-text" style={{ width: '30%' }} />
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [wallet, setWallet]       = useState(null);
  const [txns, setTxns]           = useState([]);
  const [loading, setLoading]     = useState(true);
  const [txnLoading, setTxnLoading] = useState(true);
  const [summaryLoading, setSummaryLoading] = useState(true);
  const [summary, setSummary] = useState({ incoming: 0, outgoing: 0, chart: [] });
  const [showBalance, setShowBalance] = useState(true);
  const [selectedTxn, setSelectedTxn] = useState(null);

  const fetchWallet = useCallback(async () => {
    try {
      const res = await walletApi.getMyWallet();
      setWallet(res.data || res);
    } catch (err) {
      toast.error(err.message);
    } finally { setLoading(false); }
  }, []);

  const fetchTxns = useCallback(async () => {
    try {
      const res = await transactionApi.getHistory(0, 8);
      setTxns(res.data?.content || res.content || []);
    } catch {
      setTxns([]);
    } finally { setTxnLoading(false); }
  }, []);

  const normalizeDirection = useCallback((raw) => {
    if (raw === null || raw === undefined) return null;
    const v = String(raw).trim().toUpperCase();
    if (!v) return null;
    if (['IN', 'INCOMING', 'CREDIT', '+', 'PLUS', 'RECEIVE', 'RECEIVED', 'RECEIVER'].includes(v)) return 'IN';
    if (['OUT', 'OUTGOING', 'DEBIT', '-', 'MINUS', 'SEND', 'SENT', 'SENDER'].includes(v)) return 'OUT';
    if (v === '1' || v === '+1') return 'IN';
    if (v === '-1') return 'OUT';
    return null;
  }, []);

  const getTxnDirection = useCallback((txn, walletId) => {
    const direct = normalizeDirection(txn.direction ?? txn.direct);
    if (direct) return direct;

    if (txn.type === 'DEPOSIT') return 'IN';
    if (txn.type === 'WITHDRAW') return 'OUT';
    if (txn.type === 'TRANSFER' && walletId) {
      const wid = String(walletId);
      if (txn.receiverWalletId && String(txn.receiverWalletId) === wid) return 'IN';
      if (txn.senderWalletId && String(txn.senderWalletId) === wid) return 'OUT';
    }
    return null;
  }, [normalizeDirection]);

  const buildSummary = useCallback((items, walletId) => {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfToday = new Date(now);
    startOfToday.setHours(0, 0, 0, 0);
    const start7 = new Date(startOfToday);
    start7.setDate(start7.getDate() - 6);
    const dayLabels = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
    const chart = Array.from({ length: 7 }, (_, i) => {
      const d = new Date(start7);
      d.setDate(start7.getDate() + i);
      return { day: dayLabels[d.getDay()], thu: 0, chi: 0 };
    });

    let incoming = 0;
    let outgoing = 0;

    items.forEach((txn) => {
      if (txn.status && txn.status !== 'SUCCESS') return;
      if (!txn.createdAt) return;
      const amount = Number(txn.amount || 0);
      if (Number.isNaN(amount)) return;

      const createdAt = new Date(txn.createdAt);
      const direction = getTxnDirection(txn, walletId);

      if (createdAt >= startOfMonth && direction) {
        if (direction === 'IN') incoming += amount;
        if (direction === 'OUT') outgoing += amount;
      }

      if (createdAt >= start7 && createdAt <= now && direction) {
        const dayStart = new Date(createdAt.getFullYear(), createdAt.getMonth(), createdAt.getDate());
        const dayIndex = Math.floor((dayStart - start7) / (24 * 60 * 60 * 1000));
        if (dayIndex >= 0 && dayIndex < chart.length) {
          if (direction === 'IN') chart[dayIndex].thu += amount;
          if (direction === 'OUT') chart[dayIndex].chi += amount;
        }
      }
    });

    return { incoming, outgoing, chart };
  }, [getTxnDirection]);

  const fetchSummary = useCallback(async (walletId) => {
    if (!walletId) return;
    setSummaryLoading(true);
    try {
      const now = new Date();
      const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
      const res = await transactionApi.getHistory({
        page: 0,
        size: 200,
        startDate: startOfMonth.toISOString(),
        endDate: now.toISOString(),
      });
      const list = res.data?.content || res.content || [];
      setSummary(buildSummary(list, walletId));
    } catch {
      setSummary(buildSummary([], walletId));
    } finally { setSummaryLoading(false); }
  }, [buildSummary]);

  useEffect(() => { fetchWallet(); fetchTxns(); }, [fetchWallet, fetchTxns]);
  useEffect(() => { if (wallet?.id) fetchSummary(wallet.id); }, [wallet?.id, fetchSummary]);

  return (
    <div className="page-wrapper animate-fade-in">
      {/* Header */}
      <div className="dash-header">
        <div>
          <p className="dash-greeting">Xin chào 👋</p>
          <h1>{user?.displayName || 'Người dùng'}</h1>
        </div>
      </div>

      {/* Wallet Card */}
      <div className="dash-grid-top">
        {loading ? <SkeletonCard /> : (
          <div className="wallet-card">
            <div className="wallet-card-glow" />
            <div className="wallet-card-header">
              <span className="wallet-label">Số dư ví</span>
              <button className="balance-toggle" onClick={() => setShowBalance(s => !s)} id="btn-toggle-balance">
                {showBalance ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>

            <div className="wallet-balance">
              {showBalance
                ? <span>{formatCurrency(wallet?.balance ?? 0)}</span>
                : <span className="balance-hidden">••••••••</span>
              }
            </div>

            {/* Quick Actions */}
            <div className="quick-actions">
              <button className="qa-btn" onClick={() => navigate('/transfer')} id="btn-qa-transfer">
                <ArrowLeftRight size={18} />
                <span>Chuyển</span>
              </button>
              <button className="qa-btn" onClick={() => navigate('/deposit')} id="btn-qa-deposit">
                <ArrowDownLeft size={18} />
                <span>Nạp</span>
              </button>
              <button className="qa-btn" onClick={() => navigate('/withdraw')} id="btn-qa-withdraw">
                <ArrowUpRight size={18} />
                <span>Rút</span>
              </button>
            </div>
          </div>
        )}

        {/* Stats */}
        <div className="dash-stats">
          <div className="stat-card">
            <div className="stat-icon stat-icon--green">
              <TrendingUp size={18} />
            </div>
            <div>
              <p className="stat-label">Tiền vào tháng này</p>
              <p className="stat-value text-success">
                {summaryLoading ? '—' : `+${formatCurrency(summary.incoming)}`}
              </p>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon stat-icon--red">
              <TrendingDown size={18} />
            </div>
            <div>
              <p className="stat-label">Tiền ra tháng này</p>
              <p className="stat-value text-danger">
                {summaryLoading ? '—' : `-${formatCurrency(summary.outgoing)}`}
              </p>
            </div>
          </div>

          {/* Chart */}
          <div className="chart-card">
            <p className="chart-title">Biến động 7 ngày qua</p>
            <ResponsiveContainer width="100%" height={120}>
              <LineChart data={summary.chart.length ? summary.chart : [{ day: 'CN', thu: 0, chi: 0 }]}>
                <XAxis dataKey="day" tick={{ fill: 'var(--text-muted)', fontSize: 11 }} axisLine={false} tickLine={false} />
                <YAxis hide />
                <Tooltip
                  contentStyle={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 8, fontSize: 12 }}
                  formatter={(val) => formatCurrency(val)}
                />
                <Line type="monotone" dataKey="thu" stroke="var(--success)" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="chi" stroke="var(--danger)" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
            <div className="chart-legend">
              <span className="legend-item legend-item--green">Thu</span>
              <span className="legend-item legend-item--red">Chi</span>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Transactions */}
      <div className="card" style={{ marginTop: 'var(--space-lg)' }}>
        <div className="flex justify-between items-center" style={{ marginBottom: 'var(--space-md)' }}>
          <h3>Giao dịch gần đây</h3>
          <button className="btn btn-ghost btn-sm" onClick={() => navigate('/history')} id="btn-view-all-txn">
            Xem tất cả <ChevronRight size={14} />
          </button>
        </div>

        {txnLoading ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {[1,2,3,4].map(i => (
              <div key={i} style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                <div className="skeleton skeleton-circle" style={{ width: 42, height: 42, flexShrink: 0 }} />
                <div style={{ flex: 1 }}>
                  <div className="skeleton skeleton-text" style={{ width: '50%', marginBottom: 6 }} />
                  <div className="skeleton skeleton-text" style={{ width: '30%', height: 10 }} />
                </div>
                <div className="skeleton skeleton-text" style={{ width: 80 }} />
              </div>
            ))}
          </div>
        ) : txns.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon" style={{ fontSize: 32 }}>💳</div>
            <h3>Chưa có giao dịch nào</h3>
            <p>Hãy thực hiện giao dịch đầu tiên của bạn</p>
            <button className="btn btn-primary" onClick={() => navigate('/deposit')} id="btn-deposit-now">
              <Plus size={16} /> Nạp tiền ngay
            </button>
          </div>
        ) : (
          <div>
            {txns.map((txn) => (
              <TransactionItem
                key={txn.transactionCode}
                txn={txn}
                walletId={wallet?.id}
                onClick={setSelectedTxn}
              />
            ))}
          </div>
        )}
      </div>

      {/* Transaction Detail Modal */}
      {selectedTxn && (
        <div className="modal-overlay" onClick={() => setSelectedTxn(null)}>
          <div className="modal animate-scale-in" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Chi tiết giao dịch</h3>
              <button className="modal-close" onClick={() => setSelectedTxn(null)}>✕</button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
              {[
                { label: 'Mã giao dịch', value: selectedTxn.transactionCode },
                { label: 'Loại', value: selectedTxn.type },
                { label: 'Số tiền', value: formatCurrency(selectedTxn.amount) },
                { label: 'Trạng thái', value: selectedTxn.status },
                { label: 'Lời nhắn', value: selectedTxn.description || '—' },
              ].map(({ label, value }) => (
                <div key={label} className="flex justify-between" style={{ borderBottom: '1px solid var(--border)', paddingBottom: 10 }}>
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
