import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Form, Input, Button, message, Typography } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import { customerLogin } from '../../api/portal';

const PortalLogin = () => {
    const { token } = useParams(); // Lấy token từ URL: /track/:token
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);

    const onFinish = async (values) => {
        setLoading(true);
        try {
            // Gọi API đăng nhập dành cho khách
            const data = await customerLogin(token, values.accessCode);

            // Lưu thông tin khách vào sessionStorage (Tắt trình duyệt là tự logout - Bảo mật)
            sessionStorage.setItem('customer_lead_id', data.leadId);
            sessionStorage.setItem('customer_name', data.customerName);
            // Nếu API trả về token JWT thì lưu luôn
            if (data.token) {
                sessionStorage.setItem('customer_token', data.token);
            }

            message.success('Đăng nhập thành công!');
            navigate('/portal/dashboard');

        } catch (err) {
            console.error("Chi tiết lỗi:", err);

            let errorMsg = 'Hệ thống đang bận, vui lòng thử lại sau.';

            if (err.response) {
                const status = err.response.status;
                const data = err.response.data;

                // 👇 XỬ LÝ LỖI CHI TIẾT THEO MÃ HTTP
                if (status === 401) {
                    errorMsg = '❌ Mã xác thực không đúng! Vui lòng kiểm tra lại.';
                } else if (status === 404) {
                    errorMsg = '⚠️ Hồ sơ này không tồn tại hoặc đường dẫn bị sai.';
                } else if (status === 403) {
                    errorMsg = '⛔ Hệ thống từ chối truy cập (Lỗi 403).';
                }
                // Nếu server có trả về tin nhắn cụ thể dạng text hoặc json
                else if (typeof data === 'string') {
                    errorMsg = data;
                } else if (data && data.message) {
                    errorMsg = data.message;
                }
            } else if (err.request) {
                errorMsg = 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng.';
            }

            message.error(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ minHeight: '100vh', background: '#e6f7ff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Card style={{ width: 400, textAlign: 'center', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
                <Typography.Title level={3} style={{ color: '#1890ff' }}>Cổng Thông Tin Khách Hàng</Typography.Title>
                <Typography.Text type="secondary">Vui lòng nhập mã truy cập được gửi qua Zalo</Typography.Text>

                <Form onFinish={onFinish} style={{ marginTop: 24 }}>
                    <Form.Item
                        name="accessCode"
                        rules={[{ required: true, message: 'Vui lòng nhập mã xác thực!' }]}
                    >
                        <Input.Password
                            prefix={<LockOutlined />}
                            placeholder="Nhập mã xác thực (Ví dụ: 123456)"
                            size="large"
                            style={{ textAlign: 'center' }}
                        />
                    </Form.Item>
                    <Button type="primary" htmlType="submit" block size="large" loading={loading} shape="round">
                        Tra cứu hồ sơ
                    </Button>
                </Form>
            </Card>
        </div>
    );
};

export default PortalLogin;