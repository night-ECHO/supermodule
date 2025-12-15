import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import DashboardLayout from './components/DashboardLayout';
import LeadList from './components/LeadList';
import TrackingDetail from './components/TrackingDetail';

const App = () => {
  // Giả lập trạng thái đăng nhập (Lưu trong localStorage)
  const [isAuthenticated, setIsAuthenticated] = useState(localStorage.getItem('isLoggedIn') === 'true');

  const handleLogin = (status) => {
    setIsAuthenticated(status);
    if (status) localStorage.setItem('isLoggedIn', 'true');
    else localStorage.removeItem('isLoggedIn');
  };

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={!isAuthenticated ? <Login onLogin={() => handleLogin(true)} /> : <Navigate to="/" />} />
        
        {/* Các route yêu cầu đăng nhập */}
        <Route path="/" element={isAuthenticated ? <DashboardLayout onLogout={() => handleLogin(false)} /> : <Navigate to="/login" />}>
          <Route index element={<LeadList />} />
          <Route path="tracking/:id" element={<TrackingDetail />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

export default App;