import { useState, useEffect } from 'react';
import { walletApi, transactionApi } from '../api/client';
import PinPad from '../components/PinPad';
import { formatCurrency } from '../utils/format';
import { ArrowUpRight, DollarSign, MessageSquare, ArrowRight, ChevronLeft, CheckCircle, XCircle } from 'lucide-react';
import './TransactionFlow.css';

const STEPS = ['Nhap thong tin', 'Xac nhan', 'Nhap PIN', 'Ket qua'];

export default function WithdrawPage() {
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pin, setPin] = useState('');
  const [result, setResult] = useState(null);
  const [wallet, setWallet] = useState(null);
  const [sources, setSources] = useState([]);
  const [form, setForm] = useState({ sourceId: '', amount: '', description: '' });
  const [errors, setErrors] = useState({});

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
    if (!form.sourceId) e.sourceId = 'Vui long chon nguon tien';
    if (!form.amount || Number(form.amount) <= 0) e.amount = 'Nhap so tien hop le';
    if (wallet?.balance != null && Number(form.amount) > Number(wallet.balance)) {
      e.amount = 'So du khong du de rut';
    }
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSubmit = async () => {
    if (pin.length < 6) return;
    setLoading(true);
    setStep(3);
    try {
      const res = await transactionApi.withdraw({
        sourceId: form.sourceId,
        amount: Number(form.amount),
        description: form.description,
        pin,
      });
      setResult({ success: true, data: res.data || res });
    } catch (err) {
      setResult({ success: false, message: err.message });
    } finally { setLoading(false); }
  };

  const handlePinChange = (val) => {
    setPin(val);
    if (val.length === 6) setTimeout(() => document.getElementById('btn-withdraw-pin')?.click(), 300);
  };

  const selectedSource = sources.find(s => s.id === form.sourceId);

  return (
    <div className="page-wrapper animate-fade-in">
      <div className="txn-flow-header">
        {step > 0 && step < 3 && (
          <button className="btn btn-ghost btn-sm" onClick={() => { setStep(s => s - 1); setPin(''); }}>
            <ChevronLeft size={16} /> Quay lai
          </button>
        )}
        <h1>Rut tien</h1>
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
            <div className="txn-type-icon"><ArrowUpRight size={24} /></div>
            <h2>Rut tien ve ngan hang</h2>
          </div>

          {wallet && (
            <div className="info-chip" style={{ marginBottom: 'var(--space-md)' }}>
              So du hien tai: <strong>{formatCurrency(wallet.balance)}</strong>
            </div>
          )}

          <div className="txn-form">
            <div className="form-group">
              <label className="form-label">Tai khoan nhan</label>
              {sources.length === 0 ? (
                <div className="empty-state" style={{ padding: 'var(--space-lg)' }}>
                  <p className="text-muted text-sm">Chua co nguon tien lien ket. Vui long lien ket truoc.</p>
                </div>
              ) : (
                <select
                  id="withdrawSource"
                  className={`form-input ${errors.sourceId ? 'input-error' : ''}`}
                  value={form.sourceId}
                  onChange={e => setForm({ ...form, sourceId: e.target.value })}
                >
                  <option value="">-- Chon nguon tien --</option>
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
              <label className="form-label">So tien rut (VND)</label>
              <div className="form-input-wrapper">
                <DollarSign size={16} className="form-input-icon" />
                <input
                  id="withdrawAmount"
                  type="number"
                  className={`form-input ${errors.amount ? 'input-error' : ''}`}
                  placeholder="200,000"
                  min="1000"
                  value={form.amount}
                  onChange={e => setForm({ ...form, amount: e.target.value })}
                />
              </div>
              {errors.amount && <p className="form-error">{errors.amount}</p>}
              <div className="quick-amounts">
                {[100000, 200000, 500000, 1000000].map(amt => (
                  <button
                    key={amt}
                    type="button"
                    className="quick-amount-btn"
                    onClick={() => setForm({ ...form, amount: amt })}
                  >
                    {formatCurrency(amt)}
                  </button>
                ))}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Loi nhan (tuy chon)</label>
              <div className="form-input-wrapper">
                <MessageSquare size={16} className="form-input-icon" />
                <input
                  id="withdrawDescription"
                  type="text"
                  className="form-input"
                  placeholder="Rut tien ve tai khoan"
                  value={form.description}
                  onChange={e => setForm({ ...form, description: e.target.value })}
                />
              </div>
            </div>

            <button
              className="btn btn-primary btn-full btn-lg"
              id="btn-withdraw-next"
              onClick={() => { if (validate()) setStep(1); }}
            >
              <ArrowRight size={18} /> Tiep tuc
            </button>
          </div>
        </div>
      )}

      {step === 1 && (
        <div className="card txn-card animate-fade-in">
          <h2 className="text-center" style={{ marginBottom: 'var(--space-lg)' }}>Xac nhan rut tien</h2>
          <div className="review-list">
            {[
              { label: 'Tai khoan nhan', value: selectedSource ? `${selectedSource.bankName || 'Ngan hang'} - ${selectedSource.accountNumber}` : form.sourceId },
              { label: 'So tien rut', value: <strong className="text-xl text-danger">-{formatCurrency(form.amount)}</strong> },
              { label: 'Loi nhan', value: form.description || '-' },
            ].map(({ label, value }) => (
              <div key={label} className="review-row">
                <span className="review-label">{label}</span>
                <span className="review-value">{value}</span>
              </div>
            ))}
          </div>
          <button className="btn btn-primary btn-full btn-lg" id="btn-withdraw-goto-pin" onClick={() => setStep(2)}>
            <ArrowRight size={18} /> Nhap ma PIN
          </button>
        </div>
      )}

      {step === 2 && (
        <div className="card txn-card animate-fade-in text-center">
          <div className="txn-type-icon" style={{ margin: '0 auto var(--space-md)' }}>
            <ArrowUpRight size={24} />
          </div>
          <h2>Nhap ma PIN giao dich</h2>
          <p className="text-muted" style={{ marginBottom: 'var(--space-xl)' }}>
            Rut <strong className="text-danger">{formatCurrency(form.amount)}</strong> ve tai khoan
          </p>
          <PinPad value={pin} onChange={handlePinChange} />
          <button
            id="btn-withdraw-pin"
            className="btn btn-primary btn-full btn-lg"
            style={{ marginTop: 'var(--space-xl)' }}
            onClick={handleSubmit}
            disabled={pin.length < 6 || loading}
          >
            {loading ? <><span className="spinner spinner-sm" /> Dang xu ly...</> : <><ArrowRight size={18} /> Xac nhan rut tien</>}
          </button>
        </div>
      )}

      {step === 3 && (
        <div className="card txn-card animate-scale-in text-center">
          {loading ? (
            <div style={{ padding: 'var(--space-2xl) 0', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div className="spinner spinner-lg" />
              <h3>Dang xu ly...</h3>
            </div>
          ) : result?.success ? (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div className="result-icon result-icon--success"><CheckCircle size={48} /></div>
              <h2>Rut tien thanh cong!</h2>
              <div className="result-amount">-{formatCurrency(form.amount)}</div>
              <button
                className="btn btn-primary btn-full btn-lg"
                onClick={() => { setStep(0); setForm({ sourceId: '', amount: '', description: '' }); setPin(''); setResult(null); }}
                id="btn-withdraw-again"
              >
                Rut tien tiep
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-lg)' }}>
              <div className="result-icon result-icon--fail"><XCircle size={48} /></div>
              <h2>Rut tien that bai</h2>
              <p className="text-danger">{result?.message}</p>
              <button className="btn btn-primary btn-full" onClick={() => { setStep(2); setPin(''); setResult(null); }} id="btn-withdraw-retry">
                Thu lai
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
