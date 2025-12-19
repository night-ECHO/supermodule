import axios from 'axios';

// Tạo instance riêng cho Portal để không dính dáng đến interceptor của Admin
const portalApi = axios.create({
    baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8081',
});

// Hàm đăng nhập cho khách
export const customerLogin = async (trackingToken, accessCode) => {
    const res = await portalApi.post('/api/customer/auth/login', {
        trackingToken,
        accessCode
    });
    return res.data;
};

// Hàm lấy dữ liệu Dashboard (Timeline + Thông tin)
export const getCustomerDashboard = async (leadId) => {
    const res = await portalApi.get(`/api/customer/dashboard/${leadId}`);
    return res.data;
};

// Hàm lấy danh sách tài liệu công khai
export const getCustomerDocuments = async (leadId) => {
    const res = await portalApi.get(`/api/documents/list/${leadId}`);
    return res.data;
};