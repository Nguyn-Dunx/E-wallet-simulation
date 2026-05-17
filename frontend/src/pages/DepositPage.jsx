import { useState, useEffect } from 'react';
import { walletApi, transactionApi } from '../api/client';
import PinPad from '../components/PinPad';
import { formatCurrency } from '../utils/format';
import { ArrowDownLeft, DollarSign, CreditCard, MessageSquare, ArrowRight, ChevronLeft, CheckCircle, XCircle } from 'lucide-react';
import './TransactionFlow.css';

const STEPS = ['Nhập thông tin', 'Xác nhận', 'Nhập PIN', 'Kết quả'];

export default function DepositPage() {
  const [step, setStep]     = useState(0);
  const [loading, setLoading] = useState(false);
  const [pin, setPin]       = useState('');
  const [result, setResult] = useState(null);
  const [wallet, setWallet] = useState(null);
  const [sources, setSources] = useState([]);
  const [form, setForm]     = useState({ sourceId: '', amount: '', description: '' });
  const [errors, setErrors] = useState({});

  // Load linked sources once
  const loadSources = async () => {
    if (sources.length) return;
    try {
      const [walletRes, srcRes] = await Promise.all([walletApi.getMyWallet(), walletApi.getLinkedSources()]);
      setWallet(walletRes.data || walletRes);
      setSources(srcRes.data || srcRes || []);
    } catch (_) {}
  };

  useEffect(() => { loadSources(); }, []);

  const validate = () => {
    const e = {};
    if (!form.sourceId)                          e.sourceId = 'Vui lòng chọn nguồn tiền';
    if (!form.amount || Number(form.amount) <= 0) e.amount   = 'Nhập số tiền hợp lệ';
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSubmit = async () => {
    if (pin.length < 6) return;
    setLoading(true);
    setStep(3);
    try {
      const res = await transactionApi.deposit({ sourceId: form.sourceId, amount: Number(form.amount), description: form.description, pin });
      setResult({ success: true, data: res.data || res });
    } catch (err) {
      setResult({ success: false, message: err.message });
    } finally { setLoading(false); }
  };

  const handlePinChange = (val) => {
    setPin(val);
    if (val.length === 6) setTimeout(() => document.getElementById('btn-deposit-pin')?.click(), 300);
  };

  return (
    <div className="page-wrapper animate-fade-in">
      <div className="txn-flow-header">
        {step > 0 && step < 3 && (
          <button className="btn btn-ghost btn-sm" onClick={() => { setStep(s => s - 1); setPin(''); }}>
            <ChevronLeft size={16} /> Quay lại
          </button>
        )}
        <h1>Nạp tiền</h1>
      </div>

      <div className="flow-steps">
        {STEPS.map((label, i) => (
          <div key={i} className={`flow-step ${i === step ? 'active' : i < step ? 'done' : ''}`}>
            <div className="flow-step-dot" />
            <span>{label}</span>
          </div>
        ))}
      </div>

      {step === 0 && (
        <div className="card txn-card animate-fade-in">
          <div className="txn-icon-header">
            <div className="txn-type-icon"><ArrowDownLeft size={24} /></div>
            <h2>Nạp tiền vào ví</h2>
          </div>
          <div className="txn-form">
            <div className="form-group">
              <label className="form-label">Nguồn tiền</label>
              {sources.length === 0 ? (
                <div className="empty-state" style={{ padding: 'var(--space-lg)' }}>
                  <p className="text-muted text-sm">Chưa có nguồn tiền liên kết. Vui lòng liên kết ngân hàng/thẻ trước.</p>
                </div>
              ) : (
                <select id="sourceId" className={`form-input ${errors.sourceId ? 'input-error' : ''}`}
                  value={form.sourceId} onChange={e => setForm({ ...form, sourceId: e.target.value })}>
                  <option value="">-- Chọn nguồn tiền --</option>
                  {sources.map(s => (
                    <option key={s.id} value={s.id}>
                      {(s.bankName ? `${s.bankName} - ` : '')}{s.accountNumber}
                    </option>
                  ))}
                </select>
              )}
              {errors.sourceId && <p className="form-error">{errors.sourceId}</p>}
            </div>
            <div className="form-group">
              <label className="form-label">Số tiền nạp (VND)</label>
              <div className="form-input-wrapper">
                <DollarSign size={16} className="form-input-icon" />
                <input id="depositAmount" type="number" className={`form-input ${errors.amount ? 'input-error' : ''}`}
                  placeholder="100,000" min="10000" value={form.amount}
                  onChange={e => setForm({ ...form, amount: e.target.value })} />
              </div>
              {errors.amount && <p className="form-error">{errors.amount}</p>}
              <div className="quick-amounts">
                {[100000, 200000, 500000, 1000000].map(amt => (
                  <button key={amt} type="button" className="quick-amount-btn" onClick={() => setForm({ ...form, amount: amt })}>{formatCurrency(amt)}</button>
                ))}
              </div>
            </div>
            <button className="btn btn-primary btn-full btn-lg" id="btn-deposit-next"
              onClick={() => { if (validate()) setStep(1); }}>
              <ArrowRight size={18} /> Tiếp tục
            </button>
          </div>
        </div>
      )}

      {step === 1 && (
        <div className="card txn-card animate-fade-in">
          <h2 className="text-center" style={{ marginBottom: 'var(--space-lg)' }}>Xác nhận nạp tiền</h2>
          <div className="review-list">
            {[
              { label: 'Nguồn tiền', value: (() => {
                const selected = sources.find(s => s.id === form.sourceId);
                if (!selected) return form.sourceId;
                return `${selected.bankName || 'Ngan hang'} - ${selected.accountNumber}`;
              })() },
              { label: 'Số tiền nạp', value: <strong className="text-xl text-success">+{formatCurrency(form.amount)}</strong> },
            ].map(({ label, value }) => (
              <div key={label} className="review-row">
                <span className="review-label">{label}</span>
                <span className="review-value">{value}</span>
              </div>
            ))}
          </div>
          <button className="btn btn-primary btn-full btn-lg" id="btn-deposit-goto-pin" onClick={() => setStep(2)}>
            <ArrowRight size={18} /> Nhập mã PIN
          </button>
        </div>
      )}

      {step === 2 && (
        <div className="card txn-card animate-fade-in text-center">
          <div className="txn-type-icon" style={{ margin: '0 auto var(--space-md)', fontSize: '1.5rem' }}>🔐</div>
          <h2>Nhập mã PIN giao dịch</h2>
          <p className="text-muted" style={{ marginBottom: 'var(--space-xl)' }}>
            Nạp <strong className="text-success">{formatCurrency(form.amount)}</strong> vào ví
          </p>
          <PinPad value={pin} onChange={handlePinChange} />
          <button id="btn-deposit-pin" className="btn btn-primary btn-full btn-lg" style={{ marginTop: 'var(--space-xl)' }}
            onClick={handleSubmit} disabled={pin.length < 6 || loading}>
            {loading ? <><span className="spinner spinner-sm" /> Đang xử lý...</> : <><ArrowRight size={18} /> Xác nhận nạp tiền</>}
          </button>
        </div>
      )}

      {step === 3 && (
        <div className="card txn-card animate-scale-in text-center">
          {loading ? (
            <div style={{ padding: 'var(--space-2xl) 0', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div className="spinner spinner-lg" />
              <h3>Đang xử lý...</h3>
            </div>
          ) : result?.success ? (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div className="result-icon result-icon--success"><CheckCircle size={48} /></div>
              <h2>Nạp tiền thành công! 🎉</h2>
              <div className="result-amount">+{formatCurrency(form.amount)}</div>
              <button className="btn btn-primary btn-full btn-lg" onClick={() => { setStep(0); setForm({ sourceId: '', amount: '', description: '' }); setPin(''); setResult(null); }} id="btn-deposit-again">
                Nạp tiền tiếp
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div className="result-icon result-icon--fail"><XCircle size={48} /></div>
              <h2>Nạp tiền thất bại</h2>
              <p className="text-danger">{result?.message}</p>
              <button className="btn btn-primary btn-full" onClick={() => { setStep(2); setPin(''); setResult(null); }} id="btn-deposit-retry">
                Thử lại
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
