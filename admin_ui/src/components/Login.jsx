import React from 'react';
import { Card, Form, Input, Button, message, Typography } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';

const Login = ({ onLogin }) => {
  const onFinish = (values) => {
    // Hardcode tài khoản mẫu
    if (values.username === 'admin' && values.password === '123456') {
      message.success('Đăng nhập thành công!');
      onLogin();
    } else {
      message.error('Sai tài khoản hoặc mật khẩu (admin/123456)');
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
            <Input prefix={<UserOutlined />} placeholder="Username (admin)" size="large" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: 'Nhập Password!' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="Password (123456)" size="large" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large">Đăng nhập</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default Login;