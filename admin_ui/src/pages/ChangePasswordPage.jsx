import React from 'react';
import { Card, Form, Input, Button, message, Alert, Typography } from 'antd';
import { LockOutlined, KeyOutlined } from '@ant-design/icons';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';
import { useNavigate } from 'react-router-dom';

const ChangePasswordPage = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const payload = token ? JSON.parse(atob(token.split('.')[1])) : {};
  const isFirstLogin = payload.isTemp === true;

  const onFinish = async (values) => {
    try {
      await api.post('api/auth/change-password', {  // Đổi endpoint giống TSX: /auth/change-password
        oldPassword: isFirstLogin ? undefined : values.oldPassword,
        newPassword: values.newPassword,
        confirmPassword: values.confirmPassword,  // Gửi confirmPassword để backend validate
      });

      message.success('Password changed successfully!');
      
      // Force full logout + redirect (giống TSX để clean state)
      localStorage.removeItem('token');
      window.location.href = '/login';  // Full reload

    } catch (err) {
      const msg = err.response?.data || 'Failed to change password';
      message.error(typeof msg === 'string' ? msg : msg.message || 'Error');
    }
  };

  const containerStyle = {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
    background:
      'radial-gradient(circle at 20% 20%, rgba(59,130,246,0.18), transparent 28%), radial-gradient(circle at 80% 0%, rgba(236,72,153,0.22), transparent 26%), linear-gradient(135deg, #0b1221 0%, #0f172a 55%, #111827 100%)',
  };

  const cardStyle = {
    width: 480,
    borderRadius: 18,
    boxShadow: '0 24px 70px rgba(15, 23, 42, 0.35)',
    border: '1px solid rgba(255,255,255,0.1)',
    background: 'rgba(255,255,255,0.96)',
    backdropFilter: 'blur(10px)',
  };

  const accentStyle = {
    height: 4,
    borderRadius: '999px',
    background: 'linear-gradient(90deg, #2563eb 0%, #7c3aed 50%, #ec4899 100%)',
    marginBottom: 20,
  };

  const titleStyle = {
    marginBottom: 8,
    color: '#0f172a',
    letterSpacing: 0.2,
  };

  const subtitleStyle = {
    color: '#6b7280',
  };

  return (
    <div style={containerStyle}>
      <Card style={cardStyle} bordered={false} bodyStyle={{ padding: 28 }}>
        <div style={accentStyle} />
        <div style={{ textAlign: 'center', marginBottom: 20 }}>
          <Typography.Title level={3} style={titleStyle}>
            {isFirstLogin ? 'Thiết lập mật khẩu mới' : 'Đổi mật khẩu'}
          </Typography.Title>
          <Typography.Text style={subtitleStyle}>
            Bảo mật tài khoản với mật khẩu mạnh và duy nhất
          </Typography.Text>
        </div>

        {isFirstLogin && (
          <Alert
            message="Đây là lần đăng nhập đầu tiên. Vui lòng tạo mật khẩu mới."
            type="warning"
            showIcon
            style={{ marginBottom: 16 }}
          />
        )}

        <Form form={form} onFinish={onFinish} layout="vertical" size="large" requiredMark={false}>
          {!isFirstLogin && (
            <Form.Item
              label="Mật khẩu hiện tại"
              name="oldPassword"
              rules={[{ required: true, message: 'Vui lòng nhập mật khẩu hiện tại' }]}
            >
              <Input.Password
                prefix={<KeyOutlined style={{ color: '#9ca3af' }} />}
                placeholder="Nhập mật khẩu hiện tại"
              />
            </Form.Item>
          )}

          <Form.Item
            label="Mật khẩu mới"
            name="newPassword"
            rules={[
              { required: true, message: 'Vui lòng nhập mật khẩu mới' },
              { min: 8, message: 'Ít nhất 8 ký tự' },
              { pattern: /[A-Z]/, message: 'Phải chứa ít nhất một chữ hoa' },
              { pattern: /\d/, message: 'Phải chứa ít nhất một số' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: '#9ca3af' }} />}
              placeholder="Tạo mật khẩu mới"
            />
          </Form.Item>

          <Form.Item
            label="Xác nhận mật khẩu"
            name="confirmPassword"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: 'Vui lòng xác nhận mật khẩu' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('Mật khẩu xác nhận không khớp'));
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: '#9ca3af' }} />}
              placeholder="Nhập lại mật khẩu mới"
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 8 }}>
            <Button
              type="primary"
              htmlType="submit"
              block
              size="large"
              style={{
                height: 48,
                borderRadius: 12,
                background: 'linear-gradient(135deg, #2563eb, #7c3aed)',
                boxShadow: '0 12px 30px rgba(37, 99, 235, 0.35)',
              }}
            >
              {isFirstLogin ? 'Thiết lập mật khẩu' : 'Cập nhật mật khẩu'}
            </Button>
          </Form.Item>
          <Typography.Text style={{ color: '#9ca3af', display: 'block', textAlign: 'center' }}>
            Mật khẩu mới của bạn sẽ được mã hóa và bảo vệ an toàn.
          </Typography.Text>
        </Form>
      </Card>
    </div>
  );
};

export default ChangePasswordPage;
