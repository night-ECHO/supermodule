import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import DashboardLayout from './components/DashboardLayout';
import LeadList from './components/LeadList';
import TrackingDetail from './components/TrackingDetail';
import LoginPage from './pages/LoginPage';
import ChangePasswordPage from './pages/ChangePasswordPage';
import AdminUsersPage from './pages/AdminUsersPage';
import AdminNotificationsPage from './pages/AdminNotificationsPage';
import { useCurrentUser } from './hooks/useCurrentUser';

// 👇 QUAN TRỌNG: Import các trang Portal (Nếu chưa có file thì phải tạo)
import PortalLogin from './pages/portal/PortalLogin';
import PortalDashboard from './pages/portal/PortalDashboard';

function ProtectedRoutes() {
    const { isLoggedIn, requirePasswordChange } = useAuth();
    const currentUser = useCurrentUser();

    // Nếu chưa login Admin -> Đá về trang login Admin
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
                <Route path="admin/notifications" element={<AdminNotificationsPage />} />
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
                    {/* ======================================================= */}
                    {/* 👇 KHU VỰC KHÁCH HÀNG (PORTAL) - BẠN ĐANG THIẾU CÁI NÀY */}
                    {/* ======================================================= */}

                    {/* 1. Trang khách nhập mã (Link từ Zalo) */}
                    <Route path="/track/:token" element={<PortalLogin />} />

                    {/* 2. Trang Dashboard khách (Sau khi đăng nhập) */}
                    <Route path="/portal/dashboard" element={<PortalDashboard />} />


                    {/* ======================================================= */}
                    {/* 👇 KHU VỰC ADMIN (LOGIN & PROTECTED)                   */}
                    {/* ======================================================= */}
                    <Route path="/login" element={<LoginPage />} />

                    {/* Các route yêu cầu quyền Admin */}
                    <Route path="*" element={<ProtectedRoutes />} />

                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;