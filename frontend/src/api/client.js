const API_BASE = 'http://localhost:8080/api/v1';

// ─── Auth Token helpers ───────────────────────────────────────────────────
export const getToken = () => localStorage.getItem('token');
export const setToken = (t) => localStorage.setItem('token', t);
export const removeToken = () => localStorage.removeItem('token');
export const getUser = () => {
  try { return JSON.parse(localStorage.getItem('user')); } catch { return null; }
};
export const setUser = (u) => localStorage.setItem('user', JSON.stringify(u));
export const removeUser = () => localStorage.removeItem('user');

// ─── Base fetcher ─────────────────────────────────────────────────────────
async function request(path, options = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    // Session expired
    if (res.status === 401 && token && path !== '/auth/logout') {
      removeToken(); removeUser();
      window.dispatchEvent(new CustomEvent('session-expired'));
    }
    throw { status: res.status, message: data.message || 'Đã có lỗi xảy ra' };
  }

  return data;
}

const get  = (path) => request(path, { method: 'GET' });
const post = (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) });
const del  = (path, body) => request(path, { method: 'DELETE', body: JSON.stringify(body) });

// ─── Auth ─────────────────────────────────────────────────────────────────
export const authApi = {
  login:          (body) => post('/auth/login', body),
  logout:         ()     => post('/auth/logout'),
  signupInit:     (body) => post('/auth/users/signup/init', body),
  signupVerify:   (body) => post('/auth/users/signup/verify', body),
  resetPasswordCreateOtp: (body) => post('/auth/users/reset-password/create-otp', body),
  resetPasswordVerifyOtp: (body) => post('/auth/users/reset-password/verify-otp', body),
  resetPasswordConfirm:   (body) => post('/auth/users/reset-password/confirm', body),
  changePassword: (body) => post('/auth/users/change-password', body),
  setPin:         (body) => post('/auth/users/set-pin', body),
  changePin:      (body) => post('/auth/users/change-pin', body),
};

// ─── Wallet ───────────────────────────────────────────────────────────────
export const walletApi = {
  getMyWallet:        () => get('/wallets/me'),
  getLinkedSources:   () => get('/wallets/me/banks'),
  addLinkedSource:    (body) => post('/wallets/me/banks', body),
  deleteLinkedSource: (sourceId) => del(`/wallets/me/banks/${sourceId}`),
};

// ─── Transaction ──────────────────────────────────────────────────────────
export const transactionApi = {
  transfer:     (body)             => post('/transactions/transfer', body),
  deposit:      (body)             => post('/transactions/deposit', body),
  withdraw:     (body)             => post('/transactions/withdraw', body),
  getHistory:   (page = 0, size = 10, filters = {}) => {
    let params = new URLSearchParams();
    if (typeof page === 'object' && page !== null) {
      const { page: p = 0, size: s = 10, startDate, endDate } = page;
      params.set('page', p);
      params.set('size', s);
      if (startDate) params.set('startDate', startDate);
      if (endDate) params.set('endDate', endDate);
    } else {
      params.set('page', page);
      params.set('size', size);
      if (filters.startDate) params.set('startDate', filters.startDate);
      if (filters.endDate) params.set('endDate', filters.endDate);
    }
    return get(`/transactions/history?${params.toString()}`);
  },
  getDetail:    (txnCode)          => get(`/transactions/${txnCode}`),
};

export const adminApi = {
  getAccounts: ({ page = 0, size = 10, keyword = '', status = '', role = '' } = {}) => {
    const params = new URLSearchParams();
    params.set('page', page);
    params.set('size', size);
    if (keyword) params.set('keyword', keyword);
    if (status) params.set('status', status);
    if (role) params.set('role', role);
    return get(`/admin/accounts?${params.toString()}`);
  },
  lockAccount: (accountId) => post(`/admin/accounts/${accountId}/lock`),
  unlockAccount: (accountId) => post(`/admin/accounts/${accountId}/unlock`),
  deleteAccount: (accountId) => del(`/admin/accounts/${accountId}`),
};

export default { authApi, walletApi, transactionApi, adminApi };
