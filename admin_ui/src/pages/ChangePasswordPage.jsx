import React from 'react';
import { Card, Form, Input, Button, message, Alert } from 'antd';
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

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' }}>
      <Card style={{ width: 420 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <h2>{isFirstLogin ? 'Thiết lập mật khẩu mới' : 'Đổi mật khẩu'}</h2>
          {isFirstLogin && (
            <Alert
              message="Đây là lần đăng nhập đầu tiên. Vui lòng tạo mật khẩu mới."
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}
        </div>

        <Form form={form} onFinish={onFinish} layout="vertical">
          {!isFirstLogin && (
            <Form.Item
              name="oldPassword"
              rules={[{ required: true, message: 'Vui lòng nhập mật khẩu hiện tại' }]}
            >
              <Input.Password placeholder="Mật khẩu hiện tại" />
            </Form.Item>
          )}

          <Form.Item
            name="newPassword"
            rules={[
              { required: true, message: 'Vui lòng nhập mật khẩu mới' },
              { min: 8, message: 'Ít nhất 8 ký tự' },
              { pattern: /[A-Z]/, message: 'Phải chứa ít nhất một chữ hoa' },
              { pattern: /\d/, message: 'Phải chứa ít nhất một số' },
            ]}
          >
            <Input.Password placeholder="Mật khẩu mới" />
          </Form.Item>

          <Form.Item
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
            <Input.Password placeholder="Xác nhận mật khẩu mới" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              {isFirstLogin ? 'Thiết lập mật khẩu' : 'Cập nhật mật khẩu'}
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default ChangePasswordPage;