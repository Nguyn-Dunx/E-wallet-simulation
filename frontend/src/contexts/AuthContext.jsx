import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import { authApi, getToken, setToken, setUser, getUser, removeToken, removeUser } from '../api/client';
import { useNavigate } from 'react-router-dom';

const AuthContext = createContext(null);
const SESSION_EXPIRED_MESSAGE_KEY = 'session-expired-message';
const SESSION_EXPIRED_MESSAGE = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';

// Helper: decode JWT payload (no verification, client-side only)
function getTokenExpiry(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp ? payload.exp * 1000 : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUserState] = useState(getUser);
  const [token, setTokenState] = useState(getToken);
  const [loading, setLoading] = useState(false);
  const logoutTimerRef = useRef(null);
  const navigate = useNavigate();

  const clearSession = useCallback(() => {
    removeToken();
    removeUser();
    setTokenState(null);
    setUserState(null);

    if (logoutTimerRef.current) {
      clearTimeout(logoutTimerRef.current);
      logoutTimerRef.current = null;
    }
  }, []);

  const hardRedirectToLogin = useCallback((message = SESSION_EXPIRED_MESSAGE) => {
    sessionStorage.setItem(SESSION_EXPIRED_MESSAGE_KEY, message);
    window.location.replace('/login');
  }, []);

  const scheduleAutoLogout = useCallback((jwtToken) => {
    if (logoutTimerRef.current) clearTimeout(logoutTimerRef.current);

    const expiry = getTokenExpiry(jwtToken);
    if (!expiry) return;

    const delay = expiry - Date.now();
    if (delay <= 0) {
      clearSession();
      hardRedirectToLogin();
      return;
    }

    logoutTimerRef.current = setTimeout(() => {
      clearSession();
      hardRedirectToLogin();
    }, delay);
  }, [clearSession, hardRedirectToLogin]);

  useEffect(() => {
    const stored = getToken();
    if (stored) scheduleAutoLogout(stored);

    return () => {
      if (logoutTimerRef.current) clearTimeout(logoutTimerRef.current);
    };
  }, [scheduleAutoLogout]);

  const login = useCallback(async (loginKey, password, loginType = 'PHONE') => {
    setLoading(true);
    try {
      const data = await authApi.login({ loginKey, password, loginType });
      setToken(data.token);
      setUser(data);
      setTokenState(data.token);
      setUserState(data);
      scheduleAutoLogout(data.token);
      return data;
    } finally {
      setLoading(false);
    }
  }, [scheduleAutoLogout]);

  const logout = useCallback(async (options = {}) => {
    if (!options.localOnly) {
      try {
        await authApi.logout();
      } catch (_) {}
    }

    clearSession();
    navigate('/login', { replace: true });
  }, [clearSession, navigate]);

  useEffect(() => {
    const handle = () => {
      clearSession();
      hardRedirectToLogin();
    };

    window.addEventListener('session-expired', handle);
    return () => window.removeEventListener('session-expired', handle);
  }, [clearSession, hardRedirectToLogin]);

  const isAuthenticated = !!token;
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  return (
    <AuthContext.Provider value={{ user, token, loading, isAuthenticated, isAdmin, login, logout, setUserState }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
};
