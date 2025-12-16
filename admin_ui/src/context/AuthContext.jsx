import React, { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { message } from 'antd';

const AuthContext = createContext(undefined);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [requirePasswordChange, setRequirePasswordChange] = useState(false);
  const navigate = useNavigate();

  const login = (newToken, requireChange = false) => {
    setToken(newToken);
    setRequirePasswordChange(requireChange);
    localStorage.setItem('token', newToken);
    message.success('Đăng nhập thành công!');

    if (requireChange) {
      navigate('/change-password');
    } else {
      navigate('/');
    }
  };

  const logout = () => {
    setToken(null);
    setRequirePasswordChange(false);
    localStorage.removeItem('token');
    message.success('Đã đăng xuất');
    navigate('/login');
  };

  const isLoggedIn = !!token;

  useEffect(() => {
    if (token) {
      // Optional: validate token silently if needed later
    }
  }, [token]);

  return (
    <AuthContext.Provider value={{ token, isLoggedIn, requirePasswordChange, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};