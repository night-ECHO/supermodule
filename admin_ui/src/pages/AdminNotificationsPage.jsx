import React, { useEffect, useState } from 'react';
import { Table, Tag, Button, Space, message, Tooltip, Typography } from 'antd';
import { ReloadOutlined, SendOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { fetchNotifications, retryNotification } from '../api/notifications';

const { Text } = Typography;

const statusColor = {
  SENT: 'green',
  FAILED: 'red',
  PENDING: 'orange',
};

const channelColor = {
  TELEGRAM: 'blue',
  ZALO_ZNS: 'purple',
};

const AdminNotificationsPage = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const res = await fetchNotifications(0, 100);
      setData(res);
    } catch (e) {
      message.error('Không tải được notification logs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleRetry = async (record) => {
    try {
      await retryNotification(record.id, 'Retry from admin UI');
      message.success('Đã gửi lại');
      load();
    } catch (e) {
      message.error('Gửi lại thất bại');
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 240,
      render: (v) => <Text code copyable>{v}</Text>,
    },
    {
      title: 'Channel',
      dataIndex: 'channel',
      key: 'channel',
      width: 110,
      render: (c) => <Tag color={channelColor[c] || 'default'}>{c}</Tag>,
    },
    {
      title: 'Event',
      dataIndex: 'eventType',
      key: 'eventType',
      width: 150,
    },
    {
      title: 'Recipient',
      dataIndex: 'recipient',
      key: 'recipient',
      width: 180,
    },
    {
      title: 'Preview',
      dataIndex: 'contentPreview',
      key: 'contentPreview',
      ellipsis: true,
      render: (v) => (
        <Tooltip title={v}>
          <Text>{v}</Text>
        </Tooltip>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 110,
      render: (s) => <Tag color={statusColor[s] || 'default'}>{s}</Tag>,
    },
    {
      title: 'Sent at',
      dataIndex: 'sentAt',
      key: 'sentAt',
      width: 170,
      render: (v) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '—'),
    },
    {
      title: 'Error',
      dataIndex: 'errorMessage',
      key: 'errorMessage',
      ellipsis: true,
      render: (v) => (
        <Tooltip title={v}>
          <Text type="secondary">{v}</Text>
        </Tooltip>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 140,
      render: (_, record) => (
        <Space>
          <Button
            size="small"
            icon={<SendOutlined />}
            disabled={record.status === 'SENT'}
            onClick={() => handleRetry(record)}
          >
            Retry
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 12 }}>
        <Button icon={<ReloadOutlined />} onClick={load} loading={loading}>
          Reload
        </Button>
        <Text type="secondary">Zalo hiển thị sẵn, chờ cấu hình token</Text>
      </Space>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={data}
        loading={loading}
        pagination={{ pageSize: 20 }}
        size="small"
      />
    </div>
  );
};

export default AdminNotificationsPage;
