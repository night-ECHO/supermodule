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

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' }}>
      <Card style={{ width: 400, boxShadow: '0 4px 8px rgba(0,0,0,0.1)' }}>
        <div style={{ textAlign: 'center', marginBottom: 20 }}>
          <Typography.Title level={3}>AdFlex Admin</Typography.Title>
          <Typography.Text type="secondary">Đăng nhập hệ thống quản trị</Typography.Text>
        </div>
        <Form onFinish={onFinish}>
          <Form.Item name="username" rules={[{ required: true, message: 'Nhập Username!' }]}>
            <Input prefix={<UserOutlined />} placeholder="Username" size="large" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: 'Nhập Password!' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="Password" size="large" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large" loading={submitting}>
              Đăng nhập
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default Login;