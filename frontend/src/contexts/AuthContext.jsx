import { useState, useEffect, useCallback } from "react";
import {
  authApi,
  setToken as saveToken,
  setUser as saveUser,
  getUser,
  removeToken,
  removeUser,
  walletApi,
} from "../api/client";
import { AuthContext } from "./authContext";
import toast from "react-hot-toast";

export function AuthProvider({ children }) {
  const [user, setUserState] = useState(getUser);
  const [token, setToken] = useState(localStorage.getItem("token"));
  const [loading, setLoading] = useState(false);

  const clearSession = useCallback(() => {
    removeToken();
    removeUser();
    setToken(null);
    setUserState(null);
  }, []);

  const login = useCallback(async (loginKey, password, loginType = "PHONE") => {
    setLoading(true);
    try {
      const data = await authApi.login({ loginKey, password, loginType });

      saveToken(data.token);
      localStorage.setItem("loginTime", String(Date.now()));
      setToken(data.token);

      const wallet = await walletApi.getMyWallet();

      const walletData = wallet.data || wallet;

      const userData = {
        ...data,
        walletId: walletData.id,
      };

      saveUser(userData);
      setUserState(userData);

      return userData;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    clearSession();
    window.location.href = "/login";
  }, [clearSession]);

  useEffect(() => {
    if (!token) return;

    const loginTime = Number(localStorage.getItem("loginTime"));

    if (!loginTime) {
      setTimeout(() => {
        logout();
      }, 0);
      return;
    }

    const elapsed = Date.now() - loginTime;
    const remaining = 15 * 60 * 1000 - elapsed;

    if (remaining <= 0) {
      setTimeout(() => {
        logout();
      }, 0);
      return;
    }

    const timer = setTimeout(() => {
      logout();
    }, remaining);

    return () => clearTimeout(timer);
  }, [token, logout]);

  // Listen for session-expired events dispatched by api/client.js
  useEffect(() => {
    const handle = () => {
      clearSession();
      toast.error("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
    };
    window.addEventListener("session-expired", handle);
    return () => window.removeEventListener("session-expired", handle);
  }, [clearSession]);

  const isAuthenticated = !!token;
  const isAdmin = user?.roles?.includes("ROLE_ADMIN");

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        isAuthenticated,
        isAdmin,
        login,
        logout,
        setUserState,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
