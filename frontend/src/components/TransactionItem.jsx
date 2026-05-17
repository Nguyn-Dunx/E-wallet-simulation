import { formatCurrency, formatDateTime, getTransactionLabel, getStatusLabel } from '../utils/format';
import { ArrowUpRight, ArrowDownLeft } from 'lucide-react';
import './TransactionItem.css';

export default function TransactionItem({ txn, walletId, onClick }) {
  const normalizeDirection = (raw) => {
    if (raw === null || raw === undefined) return null;
    const v = String(raw).trim().toUpperCase();
    if (!v) return null;

    if (['IN', 'INCOMING', 'CREDIT', '+', 'PLUS', 'RECEIVE', 'RECEIVED', 'RECEIVER'].includes(v)) return 'IN';
    if (['OUT', 'OUTGOING', 'DEBIT', '-', 'MINUS', 'SEND', 'SENT', 'SENDER'].includes(v)) return 'OUT';
    if (v === '1' || v === '+1') return 'IN';
    if (v === '-1') return 'OUT';

    return null;
  };

  const isTransfer = txn.type === 'TRANSFER';
  const isDeposit = txn.type === 'DEPOSIT';
  const direct = normalizeDirection(txn.direction ?? txn.direct);

  const isIncomingTransfer = isTransfer && walletId && txn.receiverWalletId && String(txn.receiverWalletId) === String(walletId);

  const isPositive = direct
    ? direct === 'IN'
    : (isDeposit || isIncomingTransfer);

  const label = isTransfer
    ? (txn.description || (isPositive ? 'Nhan tien' : 'Chuyen tien'))
    : getTransactionLabel(txn);

  const IconComponent = isPositive ? ArrowDownLeft : ArrowUpRight;
  const iconColor     = isPositive ? 'var(--success)' : 'var(--danger)';
  const iconBg        = isPositive ? 'var(--success-light)' : 'var(--danger-light)';

  return (
    <div className="txn-item" onClick={() => onClick?.(txn)}>
      <div className="txn-icon-wrap" style={{ background: iconBg }}>
        <IconComponent size={18} style={{ color: iconColor }} />
      </div>

      <div className="txn-info">
        <p className="txn-label">{label}</p>
        <p className="txn-date">{formatDateTime(txn.createdAt)}</p>
      </div>

      <div className="txn-amount-wrap">
        <p className={`txn-amount ${isPositive ? 'txn-amount--positive' : 'txn-amount--negative'}`}>
          {isPositive ? '+' : '-'}{formatCurrency(txn.amount)}
        </p>
        <span className={`badge badge-${txn.status === 'SUCCESS' ? 'success' : txn.status === 'FAILED' ? 'danger' : 'warning'}`}>
          {getStatusLabel(txn.status)}
        </span>
      </div>
    </div>
  );
}
