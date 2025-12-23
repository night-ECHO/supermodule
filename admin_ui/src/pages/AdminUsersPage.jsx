import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Tag, Alert } from 'antd';
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

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Quản lý người dùng</h2>
        <Button
          type="primary"
          icon={<PlusOutlined />}
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
      />

      <Modal
        title="Tạo người dùng mới"
        open={modalOpen}
        onCancel={() => {
          setModalOpen(false);
          setGeneratedPassword(null);
          form.resetFields();
        }}
        footer={null}
        width={600}
      >
        <Form form={form} onFinish={handleCreate} layout="vertical">
          <Form.Item
            name="username"
            rules={[{ required: true, message: 'Vui lòng nhập username' }]}
            label="Username"
          >
            <Input placeholder="Username" />
          </Form.Item>

          <Form.Item name="email" label="Email">
            <Input type="email" placeholder="Email (tùy chọn)" />
          </Form.Item>

          <Form.Item name="role" initialValue="ASSOCIATE" label="Role">
            <Select>
              <Select.Option value="ASSOCIATE">ASSOCIATE</Select.Option>
              <Select.Option value="MANAGER">MANAGER</Select.Option>
              <Select.Option value="ADMIN">ADMIN</Select.Option>
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
            <Input placeholder="Để trống để tự sinh" />
          </Form.Item>

          <Form.Item>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <Button onClick={() => {
                setModalOpen(false);
                setGeneratedPassword(null);
                form.resetFields();
              }}>
                Hủy
              </Button>
              <Button type="primary" htmlType="submit">
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
                  <code style={{ fontSize: 24, background: '#f0f0f0', padding: '8px 12px', borderRadius: 4 }}>
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