import { useEffect, useState } from 'react';
import { authApi, transactionApi, walletApi } from '../api/client';
import { useNavigate } from 'react-router-dom';
import PinPad from '../components/PinPad';
import { formatCurrency } from '../utils/format';
import { ArrowLeftRight, User, DollarSign, MessageSquare, CheckCircle, XCircle, ArrowRight, ChevronLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import './TransactionFlow.css';

const STEPS = ['Nhập thông tin', 'Xác nhận', 'Nhập PIN', 'Kết quả'];

const LAST_RECEIVER_STORAGE_KEY = 'ewallet.lastTransferReceiverLoginKey';

export default function TransferPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [wallet, setWallet] = useState(null);
  const [pin, setPin] = useState('');
  const [result, setResult] = useState(null);
  const [form, setForm] = useState(() => ({
    receiverLoginKey: sessionStorage.getItem(LAST_RECEIVER_STORAGE_KEY) || '',
    amount: '',
    description: '',
  }));
  const [errors, setErrors] = useState({});
  const currentBalance = wallet?.balance != null ? Number(wallet.balance) : null;
  const transferAmount = Number(form.amount) || 0;
  const balanceAfterTransfer = currentBalance != null ? currentBalance - transferAmount : null;

  useEffect(() => {
    let active = true;

    authApi.getPinStatus()
      .then((res) => {
        if (!active) return;
        const hasPin = res?.data?.hasPin ?? res?.hasPin;
        if (!hasPin) navigate('/settings?section=set-pin&requirePin=1', { replace: true });
      })
      .catch(() => {
        // ignore; transaction will fail on submit if PIN truly missing
      });

    return () => {
      active = false;
    };
  }, [navigate]);

  useEffect(() => {
    let active = true;

    walletApi.getMyWallet()
      .then((res) => {
        if (active) setWallet(res.data || res);
      })
      .catch(() => {
        if (active) setWallet(null);
      });

    return () => {
      active = false;
    };
  }, []);

  const validate = () => {
    const e = {};
    const amount = Number(form.amount);
    if (!form.receiverLoginKey.trim()) e.receiverLoginKey = 'Vui lòng nhập số điện thoại người nhận';
    if (!form.amount || amount <= 0) e.amount = 'Vui lòng nhập số tiền hợp lệ';
    else if (currentBalance == null) e.amount = 'Chưa tải được số dư ví, vui lòng thử lại sau vài giây';
    else if (amount > currentBalance) e.amount = `Số dư không đủ. Số dư hiện tại: ${formatCurrency(currentBalance)}`;
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleGoToPin = () => {
    if (!validate()) {
      setStep(0);
      return;
    }
    setStep(2);
  };

  const handleConfirm = (e) => {
    e.preventDefault();
    if (!validate()) return;
    setStep(1);
  };

  const handleSubmit = async () => {
    if (pin.length < 6) { toast.error('Nhập đủ 6 số PIN'); return; }
    setLoading(true);
    setStep(3);
    try {
      const res = await transactionApi.transfer({
        receiverLoginKey: form.receiverLoginKey.trim(),
        amount: Number(form.amount),
        description: form.description,
        pin,
      });
      const receiver = form.receiverLoginKey.trim();
      if (receiver) sessionStorage.setItem(LAST_RECEIVER_STORAGE_KEY, receiver);
      setResult({ success: true, data: res.data || res });
    } catch (err) {
      setResult({ success: false, message: err.message });
    } finally { setLoading(false); }
  };

  const handleTransferAgain = () => {
    const receiver = form.receiverLoginKey.trim();
    if (receiver) sessionStorage.setItem(LAST_RECEIVER_STORAGE_KEY, receiver);
    window.location.reload();
  };

  // Auto-submit when PIN is full
  const handlePinChange = (val) => {
    setPin(val);
    if (val.length === 6) {
      setTimeout(() => {
        setPin(val);
        // Trigger submit from the button
        document.getElementById('btn-confirm-pin')?.click();
      }, 300);
    }
  };

  return (
    <div className="page-wrapper animate-fade-in">
      {/* Header */}
      <div className="txn-flow-header">
        {step > 0 && step < 3 && (
          <button className="btn btn-ghost btn-sm" onClick={() => { setStep(s => s - 1); setPin(''); }}>
            <ChevronLeft size={16} /> Quay lại
          </button>
        )}
        <h1>Chuyển tiền</h1>
      </div>

      {/* Step indicator */}
      <div className="flow-steps">
        {STEPS.map((label, i) => (
          <div key={i} className={`flow-step ${i === step ? 'active' : i < step ? 'done' : ''}`}>
            <div className="flow-step-dot" />
            <span>{label}</span>
          </div>
        ))}
      </div>

      {/* Step 0: Enter info */}
      {step === 0 && (
        <div className="card txn-card animate-fade-in">
          <div className="txn-icon-header">
            <div className="txn-type-icon">
              <ArrowLeftRight size={24} />
            </div>
            <h2>Thông tin chuyển tiền</h2>
          </div>

          <div className="balance-summary">
            <span>Số dư hiện tại</span>
            <strong>{currentBalance == null ? 'Đang tải...' : formatCurrency(currentBalance)}</strong>
          </div>

          <form onSubmit={handleConfirm} className="txn-form">
            <div className="form-group">
              <label className="form-label">Số điện thoại người nhận</label>
              <div className="form-input-wrapper">
                <User size={16} className="form-input-icon" />
                <input
                  id="receiverLoginKey"
                  type="text"
                  className={`form-input ${errors.receiverLoginKey ? 'input-error' : ''}`}
                  placeholder="Nhập số điện thoại"
                  value={form.receiverLoginKey}
                  onChange={e => setForm({ ...form, receiverLoginKey: e.target.value })}
                />
              </div>
              {errors.receiverLoginKey && <p className="form-error">{errors.receiverLoginKey}</p>}
            </div>

            <div className="form-group">
              <label className="form-label">Số tiền (VND)</label>
              <div className="form-input-wrapper">
                <DollarSign size={16} className="form-input-icon" />
                <input
                  id="amount"
                  type="number"
                  className={`form-input ${errors.amount ? 'input-error' : ''}`}
                  placeholder="50,000"
                  min="1000"
                  value={form.amount}
                  onChange={e => setForm({ ...form, amount: e.target.value })}
                />
              </div>
              {errors.amount && <p className="form-error">{errors.amount}</p>}
              {currentBalance != null && (
                <p className="text-muted text-sm">Số dư khả dụng: {formatCurrency(currentBalance)}</p>
              )}
              {/* Quick amount buttons */}
              <div className="quick-amounts">
                {[50000, 100000, 200000, 500000].map(amt => (
                  <button key={amt} type="button" className="quick-amount-btn"
                    onClick={() => setForm({ ...form, amount: amt })}>
                    {formatCurrency(amt)}
                  </button>
                ))}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Lời nhắn (tuỳ chọn)</label>
              <div className="form-input-wrapper">
                <MessageSquare size={16} className="form-input-icon" />
                <input
                  id="description"
                  type="text"
                  className="form-input"
                  placeholder="Tiền cà phê, ăn trưa..."
                  value={form.description}
                  onChange={e => setForm({ ...form, description: e.target.value })}
                />
              </div>
            </div>

            <button type="submit" id="btn-next-confirm" className="btn btn-primary btn-full btn-lg">
              <ArrowRight size={18} /> Tiếp tục xác nhận
            </button>
          </form>
        </div>
      )}

      {/* Step 1: Review */}
      {step === 1 && (
        <div className="card txn-card animate-fade-in">
          <h2 className="text-center" style={{ marginBottom: 'var(--space-lg)' }}>Xác nhận giao dịch</h2>
          <div className="review-list">
            {[
              { label: 'Loại giao dịch', value: 'Chuyển tiền' },
              { label: 'Số điện thoại nhận', value: form.receiverLoginKey },
              { label: 'Số dư hiện tại', value: currentBalance == null ? 'Đang tải...' : formatCurrency(currentBalance) },
              { label: 'Số tiền', value: <strong className="text-xl text-danger">-{formatCurrency(form.amount)}</strong> },
              { label: 'Số dư sau chuyển', value: balanceAfterTransfer == null ? 'Đang tải...' : formatCurrency(balanceAfterTransfer) },
              { label: 'Lời nhắn', value: form.description || '—' },
            ].map(({ label, value }) => (
              <div key={label} className="review-row">
                <span className="review-label">{label}</span>
                <span className="review-value">{value}</span>
              </div>
            ))}
          </div>
          <div className="review-notice">
            ⚠️ Vui lòng kiểm tra kỹ thông tin trước khi xác nhận. Giao dịch không thể hoàn tác.
          </div>
          <button className="btn btn-primary btn-full btn-lg" id="btn-goto-pin" onClick={handleGoToPin}>
            <ArrowRight size={18} /> Nhập mã PIN
          </button>
        </div>
      )}

      {/* Step 2: PIN */}
      {step === 2 && (
        <div className="card txn-card animate-fade-in text-center">
          <div className="txn-type-icon" style={{ margin: '0 auto var(--space-md)' }}>
            🔐
          </div>
          <h2>Nhập mã PIN giao dịch</h2>
          <p className="text-muted" style={{ marginBottom: 'var(--space-xl)' }}>
            Nhập 6 số mã PIN để xác thực giao dịch <strong className="text-danger">{formatCurrency(form.amount)}</strong>
          </p>
          <PinPad value={pin} onChange={handlePinChange} />
          <button
            id="btn-confirm-pin"
            className="btn btn-primary btn-full btn-lg"
            style={{ marginTop: 'var(--space-xl)' }}
            onClick={handleSubmit}
            disabled={pin.length < 6 || loading}
          >
            {loading ? <><span className="spinner spinner-sm" /> Đang xử lý...</> : <><ArrowRight size={18} /> Xác nhận</>}
          </button>
        </div>
      )}

      {/* Step 3: Result */}
      {step === 3 && (
        <div className="card txn-card animate-scale-in text-center">
          {loading ? (
            <div style={{ padding: 'var(--space-2xl) 0', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div className="spinner spinner-lg" />
              <h3>Đang xử lý giao dịch...</h3>
              <p className="text-muted">Vui lòng không tắt trình duyệt</p>
            </div>
          ) : result?.success ? (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div className="result-icon result-icon--success">
                <CheckCircle size={48} />
              </div>
              <h2>Chuyển tiền thành công! 🎉</h2>
              <p className="text-muted">Giao dịch của bạn đã được thực hiện thành công.</p>
              <div className="result-amount">{formatCurrency(form.amount)}</div>
              <div style={{ display: 'flex', gap: 'var(--space-md)', width: '100%' }}>
                <button className="btn btn-secondary btn-full" onClick={() => navigate('/history')} id="btn-view-history">
                  Xem lịch sử
                </button>
                <button className="btn btn-primary btn-full" onClick={handleTransferAgain} id="btn-new-transfer">
                  Chuyển lại
                </button>
              </div>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div className="result-icon result-icon--fail">
                <XCircle size={48} />
              </div>
              <h2>Giao dịch thất bại</h2>
              <p className="text-danger">{result?.message}</p>
              <div style={{ display: 'flex', gap: 'var(--space-md)', width: '100%' }}>
                <button className="btn btn-secondary btn-full" onClick={() => navigate('/dashboard')} id="btn-back-home">
                  Về trang chủ
                </button>
                <button className="btn btn-primary btn-full" onClick={() => { setStep(2); setPin(''); setResult(null); }} id="btn-retry">
                  Thử lại
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
