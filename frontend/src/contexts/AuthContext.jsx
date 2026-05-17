import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi, getToken, setToken, setUser, getUser, removeToken, removeUser } from '../api/client';
import toast from 'react-hot-toast';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUserState] = useState(getUser);
  const [token, setTokenState] = useState(getToken);
  const [loading, setLoading] = useState(false);

  const clearSession = useCallback(() => {
    removeToken(); removeUser();
    setTokenState(null); setUserState(null);
  }, []);

  const login = useCallback(async (loginKey, password, loginType = 'PHONE') => {
    setLoading(true);
    try {
      const data = await authApi.login({ loginKey, password, loginType });
      setToken(data.token);
      setUser(data);
      setTokenState(data.token);
      setUserState(data);
      return data;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async (options = {}) => {
    if (!options.localOnly) {
      try { await authApi.logout(); } catch (_) {}
    }
    clearSession();
  }, [clearSession]);

  // Listen for session-expired events dispatched by api/client.js
  useEffect(() => {
    const handle = () => {
      clearSession();
      toast.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
    };
    window.addEventListener('session-expired', handle);
    return () => window.removeEventListener('session-expired', handle);
  }, [clearSession]);

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
