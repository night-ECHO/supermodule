import React from 'react';
import { Layout, Menu, Button } from 'antd';
import { LogoutOutlined, UnorderedListOutlined, UserOutlined } from '@ant-design/icons';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCurrentUser } from '../hooks/useCurrentUser';

const { Header, Content, Sider } = Layout;

const DashboardLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();
  const currentUser = useCurrentUser();

  const menuItems = [
    {
      key: 'leads',
      icon: <UnorderedListOutlined />,
      label: 'Danh sách Lead',
      onClick: () => navigate('/'),
    },
  ];

  if (currentUser?.role === 'ADMIN') {
    menuItems.push({
      key: 'users',
      icon: <UserOutlined />,
      label: 'Quản lý người dùng',
      onClick: () => navigate('/admin/users'),
    });
  }

  const selectedKey = location.pathname.startsWith('/admin/users') ? 'users' : 'leads';

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider breakpoint="lg" collapsedWidth="0">
        <div className="logo" style={{ height: 64, display: 'flex', alignItems: 'center', padding: '0 16px', color: '#fff', fontWeight: 600, background: 'rgba(255,255,255,0.08)' }}>
          AdFlex Admin
        </div>
        <Menu theme="dark" mode="inline" selectedKeys={[selectedKey]} items={menuItems} />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', display: 'flex', justifyContent: 'flex-end', alignItems: 'center', padding: '0 24px', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <Button icon={<LogoutOutlined />} onClick={logout}>
            Đăng xuất
          </Button>
        </Header>
        <Content style={{ margin: '24px', minHeight: 'calc(100vh - 128px)' }}>
          <div style={{ background: '#fff', borderRadius: 12, padding: 24, boxShadow: '0 4px 18px rgba(0,0,0,0.04)' }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default DashboardLayout;