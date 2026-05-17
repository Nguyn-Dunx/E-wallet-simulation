export function formatCurrency(amount) {
  if (amount == null) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

export function formatDateTime(isoString) {
  if (!isoString) return '';
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  }).format(new Date(isoString));
}

export function formatDate(isoString) {
  if (!isoString) return '';
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
  }).format(new Date(isoString));
}

export function getTransactionLabel(txn) {
  switch (txn.type) {
    case 'DEPOSIT':  return `Nạp tiền vào ví`;
    case 'WITHDRAW': return `Rút tiền về ngân hàng`;
    case 'TRANSFER': return txn.description || `Chuyển tiền`;
    default:         return txn.type;
  }
}

export function getTransactionColor(type) {
  switch (type) {
    case 'DEPOSIT':  return 'var(--success)';
    case 'WITHDRAW': return 'var(--danger)';
    case 'TRANSFER': return 'var(--primary)';
    default:         return 'var(--text-muted)';
  }
}

export function getStatusLabel(status) {
  switch (status) {
    case 'SUCCESS': return 'Thành công';
    case 'FAILED':  return 'Thất bại';
    case 'PENDING': return 'Đang xử lý';
    default:        return status;
  }
}

export function maskWalletId(id) {
  if (!id) return '';
  return id.toString().slice(0, 8).toUpperCase() + '...';
}
