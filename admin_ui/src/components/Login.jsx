import React, { useState } from 'react';
import { Card, Form, Input, Button, message, Typography } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import api from '../api/api';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const { login } = useAuth();
  const [submitting, setSubmitting] = useState(false);

  const onFinish = async (values) => {
    setSubmitting(true);
    try {
      const res = await api.post('/api/auth/login', values);
      const { token, requirePasswordChange } = res.data || {};
      if (!token) throw new Error('No token returned');
      login(token, requirePasswordChange);
    } catch (err) {
      const msg = err.response?.data?.message || 'Sai tài khoản hoặc mật khẩu';
      message.error(msg);
    } finally {
      setSubmitting(false);
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
    width: 420,
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
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Typography.Title level={3} style={titleStyle}>
            AdFlex Admin
          </Typography.Title>
          <Typography.Text style={subtitleStyle}>
            Đăng nhập để quản trị chiến dịch và báo cáo
          </Typography.Text>
        </div>
        <Form layout="vertical" size="large" onFinish={onFinish} requiredMark={false}>
          <Form.Item
            label="Tên đăng nhập"
            name="username"
            rules={[{ required: true, message: 'Nhập Username!' }]}
          >
            <Input
              prefix={<UserOutlined style={{ color: '#9ca3af' }} />}
              placeholder="Nhập username"
              allowClear
            />
          </Form.Item>
          <Form.Item
            label="Mật khẩu"
            name="password"
            rules={[{ required: true, message: 'Nhập Password!' }]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: '#9ca3af' }} />}
              placeholder="Nhập mật khẩu"
            />
          </Form.Item>
          <Form.Item style={{ marginBottom: 8 }}>
            <Button
              type="primary"
              htmlType="submit"
              block
              size="large"
              loading={submitting}
              style={{
                height: 48,
                borderRadius: 12,
                background: 'linear-gradient(135deg, #2563eb, #7c3aed)',
                boxShadow: '0 12px 30px rgba(37, 99, 235, 0.35)',
              }}
            >
              Đăng nhập
            </Button>
          </Form.Item>
          <Typography.Text style={{ color: '#9ca3af', display: 'block', textAlign: 'center' }}>
            Welcome.
          </Typography.Text>
        </Form>
      </Card>
    </div>
  );
};

export default Login;
