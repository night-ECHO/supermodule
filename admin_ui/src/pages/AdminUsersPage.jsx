import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Tag, Alert, Card, Typography } from 'antd';
import { PlusOutlined, KeyOutlined } from '@ant-design/icons';
import api from '../api/api';
import { useCurrentUser } from '../hooks/useCurrentUser';

const AdminUsersPage = () => {
  const currentUser = useCurrentUser();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [generatedPassword, setGeneratedPassword] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const res = await api.get('/api/admin/users');
      setUsers(res.data);
    } catch (err) {
      message.error('Không tải được danh sách người dùng');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (values) => {
    try {
      const payload = {
        username: values.username,
        email: values.email || null,
        role: values.role,
        org: values.org,
        initialPassword: values.initialPassword?.trim() || null,
      };

      const res = await api.post('/api/admin/users', payload);
      const newUser = res.data;

      // Thêm user mới vào danh sách, đánh dấu forceChangePassword = true
      setUsers([...users, { ...newUser, forceChangePassword: true }]);

      // Hiển thị mật khẩu nếu backend trả về (trường hợp tự sinh)
      if (newUser.initialPassword) {
        setGeneratedPassword(newUser.initialPassword);
      }

      message.success('Tạo người dùng thành công');
      form.resetFields();
    } catch (err) {
      message.error(err.response?.data?.message || 'Tạo thất bại');
    }
  };

  const handleResetPassword = async (userId) => {
    Modal.confirm({
      title: 'Đặt lại mật khẩu?',
      content: 'Người dùng sẽ phải đổi mật khẩu khi đăng nhập lần sau.',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await api.post(`/api/admin/users/${userId}/reset`);
          // Cập nhật trạng thái forceChangePassword thành true
          setUsers(users.map(u => u.id === userId ? { ...u, forceChangePassword: true } : u));
          message.success('Đã đặt lại mật khẩu thành công');
        } catch (err) {
          message.error('Thất bại');
        }
      },
    });
  };

  const columns = [
    { title: 'Username', dataIndex: 'username', key: 'username' },
    { title: 'Email', dataIndex: 'email', key: 'email', render: (text) => text || '-' },
    {
      title: 'Role',
      dataIndex: 'role',
      key: 'role',
      render: (role) => (
        <Tag color={role === 'ADMIN' ? 'purple' : role === 'MANAGER' ? 'blue' : 'default'}>
          {role}
        </Tag>
      ),
    },
    { title: 'Org', dataIndex: 'org', key: 'org' },
    {
      title: 'Status',
      key: 'status',
      render: (_, record) => (
        record.forceChangePassword ? (
          <Tag color="orange">Requires change</Tag>
        ) : (
          <Tag color="green">Active</Tag>
        )
      ),
    },
    {
      title: 'Action',
      key: 'action',
      render: (_, record) => (
        <Button
          icon={<KeyOutlined />}
          size="small"
          onClick={() => handleResetPassword(record.id)}
        >
          Reset Password
        </Button>
      ),
    },
  ];

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
    width: '100%',
    maxWidth: 1180,
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <div>
            <Typography.Title level={3} style={titleStyle}>
              Quản lý người dùng
            </Typography.Title>
            <Typography.Text style={subtitleStyle}>
              Theo dõi quyền truy cập và đặt lại mật khẩu khi cần
            </Typography.Text>
          </div>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            size="large"
            style={{
              borderRadius: 12,
              background: 'linear-gradient(135deg, #2563eb, #7c3aed)',
              boxShadow: '0 12px 30px rgba(37, 99, 235, 0.35)',
            }}
            onClick={() => {
              setModalOpen(true);
              setGeneratedPassword(null);
            }}
          >
            Tạo người dùng mới
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          style={{ borderRadius: 12, overflow: 'hidden' }}
        />
      </Card>

      <Modal
        title={<Typography.Title level={4} style={{ margin: 0 }}>Tạo người dùng mới</Typography.Title>}
        open={modalOpen}
        onCancel={() => {
          setModalOpen(false);
          setGeneratedPassword(null);
          form.resetFields();
        }}
        footer={null}
        width={640}
        bodyStyle={{ paddingTop: 12, paddingBottom: 8 }}
      >
        <Form form={form} onFinish={handleCreate} layout="vertical" size="large" requiredMark={false}>
          <Form.Item
            name="username"
            rules={[{ required: true, message: 'Vui lòng nhập username' }]}
            label="Username"
          >
            <Input placeholder="Username" allowClear />
          </Form.Item>

          <Form.Item name="email" label="Email">
            <Input type="email" placeholder="Email (tùy chọn)" allowClear />
          </Form.Item>

          <Form.Item name="role" initialValue="ASSOCIATE" label="Role">
            <Select>
              <Select.Option value="ASSOCIATE">ASSOCIATE</Select.Option>
              <Select.Option value="MANAGER">MANAGER</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item name="org" initialValue="ADFLEX" label="Organization">
            <Select>
              <Select.Option value="ADFLEX">ADFLEX</Select.Option>
              <Select.Option value="ULTRA">ULTRA</Select.Option>
              <Select.Option value="MB">MB</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="initialPassword"
            label="Mật khẩu ban đầu (tùy chọn)"
            extra="Để trống để hệ thống tự sinh mật khẩu ngẫu nhiên"
          >
            <Input placeholder="Để trống để tự sinh" allowClear />
          </Form.Item>

          <Form.Item style={{ marginBottom: 8 }}>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <Button
                onClick={() => {
                  setModalOpen(false);
                  setGeneratedPassword(null);
                  form.resetFields();
                }}
              >
                Hủy
              </Button>
              <Button
                type="primary"
                htmlType="submit"
                style={{
                  borderRadius: 10,
                  background: 'linear-gradient(135deg, #2563eb, #7c3aed)',
                  boxShadow: '0 12px 30px rgba(37, 99, 235, 0.35)',
                }}
              >
                Tạo
              </Button>
            </div>
          </Form.Item>
        </Form>

        {generatedPassword && (
          <Alert
            type="success"
            message={
              <div>
                <strong>Mật khẩu ban đầu đã được tạo tự động:</strong>
                <div style={{ margin: '12px 0' }}>
                  <code style={{ fontSize: 24, background: '#f0f0f0', padding: '8px 12px', borderRadius: 6 }}>
                    {generatedPassword}
                  </code>
                </div>
                <small style={{ color: '#d9363e' }}>
                  ⚠️ Người dùng phải đổi mật khẩu ngay lần đăng nhập đầu tiên. Hãy sao chép ngay vì mật khẩu này sẽ không hiển thị lại!
                </small>
              </div>
            }
            style={{ marginTop: 16 }}
          />
        )}
      </Modal>
    </div>
  );
};

export default AdminUsersPage;
