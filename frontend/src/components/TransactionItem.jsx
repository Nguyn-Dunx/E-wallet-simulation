import { formatCurrency, formatDateTime, getTransactionLabel, getStatusLabel } from '../utils/format';
import { ArrowUpRight, ArrowDownLeft } from 'lucide-react';
import './TransactionItem.css';

export default function TransactionItem({ txn, walletId, onClick }) {
  const isTransfer = txn.type === 'TRANSFER';
  const isDeposit = txn.type === 'DEPOSIT';
  const isIncomingTransfer = isTransfer && walletId && txn.receiverWalletId && String(txn.receiverWalletId) === String(walletId);
  const isOutgoingTransfer = isTransfer && walletId && txn.senderWalletId && String(txn.senderWalletId) === String(walletId);
  const isPositive = isDeposit || isIncomingTransfer;
  const label = isTransfer
    ? (txn.description || (isIncomingTransfer ? 'Nhan tien' : 'Chuyen tien'))
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
