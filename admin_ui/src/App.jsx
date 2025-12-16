import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import DashboardLayout from './components/DashboardLayout';
import LeadList from './components/LeadList';
import TrackingDetail from './components/TrackingDetail';
import LoginPage from './pages/LoginPage';
import ChangePasswordPage from './pages/ChangePasswordPage';
import AdminUsersPage from './pages/AdminUsersPage';
import { useCurrentUser } from './hooks/useCurrentUser';

function ProtectedRoutes() {
  const { isLoggedIn, requirePasswordChange } = useAuth();
  const currentUser = useCurrentUser();

  if (!isLoggedIn) {
    return <Navigate to="/login" replace />;
  }

  if (requirePasswordChange) {
    return (
      <Routes>
        <Route path="/change-password" element={<ChangePasswordPage />} />
        <Route path="*" element={<Navigate to="/change-password" replace />} />
      </Routes>
    );
  }

  const defaultPath = currentUser?.role === 'ADMIN' ? '/admin/users' : '/';

  return (
    <Routes>
      <Route path="/" element={<DashboardLayout />}>
        <Route index element={<LeadList />} />
        <Route path="tracking/:id" element={<TrackingDetail />} />
        <Route path="admin/users" element={<AdminUsersPage />} />
        <Route path="*" element={<Navigate to={defaultPath} replace />} />
      </Route>
      <Route path="*" element={<Navigate to={defaultPath} replace />} />
    </Routes>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="*" element={<ProtectedRoutes />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;