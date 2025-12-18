import React, { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Alert, Button, Card, Col, Divider, Form, Input, Modal, Row, Space, Spin, Tag, Typography, message } from 'antd';
import { CheckCircleOutlined, ClockCircleOutlined, DownloadOutlined, LockOutlined } from '@ant-design/icons';
import { customerAuth, downloadCustomerDocument, fetchCustomerTracking } from '../api/customerApi';

const statusColor = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'green';
    case 'IN_PROGRESS':
      return 'blue';
    case 'WAITING_PAYMENT':
      return 'orange';
    case 'LOCKED':
      return 'default';
    default:
      return 'default';
  }
};

const CustomerPortalPage = () => {
  const { token } = useParams();
  const [form] = Form.useForm();

  const storageKey = useMemo(() => `customer_token_${token}`, [token]);
  const [customerToken, setCustomerToken] = useState(localStorage.getItem(storageKey) || '');
  const [loading, setLoading] = useState(false);
  const [tracking, setTracking] = useState(null);
  const [selectedStep, setSelectedStep] = useState(null);

  const loadTracking = async (jwt) => {
    setLoading(true);
    try {
      const data = await fetchCustomerTracking(jwt);
      setTracking(data);
    } catch (e) {
      localStorage.removeItem(storageKey);
      setCustomerToken('');
      setTracking(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (customerToken) {
      loadTracking(customerToken);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customerToken, token]);

  const timeline = tracking?.timeline || [];
  const documents = tracking?.documents || [];

  const coreFlow = useMemo(
    () => timeline.filter((s) => (s.milestoneType || '').toUpperCase() === 'CORE'),
    [timeline],
  );
  const docsByMilestone = useMemo(() => {
    const map = new Map();
    for (const doc of documents) {
      const key = doc.milestone_code || '';
      if (!map.has(key)) map.set(key, []);
      map.get(key).push(doc);
    }
    return map;
  }, [documents]);

  const handleLogin = async () => {
    const values = await form.validateFields();
    setLoading(true);
    try {
      const res = await customerAuth(token, values.accessCode);
      localStorage.setItem(storageKey, res.token);
      setCustomerToken(res.token);
      message.success('Xác thực thành công');
    } catch (err) {
      const status = err?.response?.status;
      if (status === 429) {
        message.error('Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau');
      } else {
        message.error('Sai passcode');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (doc) => {
    try {
      const blob = await downloadCustomerDocument(doc.id, customerToken);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = doc.name || 'document';
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (e) {
      message.error('Không thể tải file');
    }
  };

  if (!customerToken) {
    return (
      <div style={{ maxWidth: 420, margin: '80px auto', padding: 16 }}>
        <Card title="Nhập Passcode để xem tiến độ" bordered>
          <Form form={form} layout="vertical" onFinish={handleLogin}>
            <Form.Item
              label="Passcode"
              name="accessCode"
              rules={[{ required: true, message: 'Vui lòng nhập passcode' }]}
            >
              <Input prefix={<LockOutlined />} placeholder="Ví dụ: 888888" autoComplete="one-time-code" />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              Xác thực
            </Button>
          </Form>
          <div style={{ marginTop: 12, color: '#666' }}>
            Passcode được gửi qua Zalo khi hồ sơ được kích hoạt.
          </div>
        </Card>
      </div>
    );
  }

  if (loading && !tracking) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', marginTop: 80 }}>
        <Spin />
      </div>
    );
  }

  const companyName = tracking?.lead_info?.company_name || 'Hồ sơ';

  return (
    <div style={{ padding: 16, maxWidth: 1200, margin: '0 auto' }}>
      <Space direction="vertical" style={{ width: '100%' }} size={16}>
        <Card bordered>
          <Typography.Title level={4} style={{ margin: 0 }}>
            Tiến độ thành lập công ty {companyName}
          </Typography.Title>
          {tracking?.current_status && (
            <div style={{ marginTop: 8 }}>
              Trạng thái hiện tại:{' '}
              <Tag color={statusColor(tracking.current_status)} icon={tracking.current_status === 'COMPLETED' ? <CheckCircleOutlined /> : <ClockCircleOutlined />}>
                {tracking.current_status}
              </Tag>
            </div>
          )}
        </Card>

        <Card title="Quy trình xử lý (Core Flow)" bordered={false}>
          {coreFlow.length === 0 && <Alert type="info" message="Chưa có dữ liệu tiến trình" />}
          <Row gutter={[16, 16]}>
            {coreFlow.map((step) => (
              <Col xs={24} sm={12} key={step.code}>
                <Card
                  type="inner"
                  title={step.name || step.code}
                  extra={<a onClick={() => setSelectedStep(step)}>Chi tiết</a>}
                  style={{
                    borderLeft: `5px solid ${step.status === 'COMPLETED' ? '#52c41a' : step.status === 'IN_PROGRESS' ? '#faad14' : '#d9d9d9'}`,
                  }}
                >
                  <div style={{ marginBottom: 8 }}>
                    Trạng thái:{' '}
                    <Tag color={statusColor(step.status)}>
                      {step.status}
                    </Tag>
                  </div>
                </Card>
              </Col>
            ))}
          </Row>
        </Card>

        <Card title="Tài liệu đã công khai" bordered={false}>
          {documents.length === 0 && <div>Chưa có tài liệu công khai.</div>}
          {documents.map((doc) => (
            <div key={doc.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0' }}>
              <div>
                <div style={{ fontWeight: 600 }}>{doc.name}</div>
                <div style={{ color: '#666' }}>{doc.type || 'DOCUMENT'}</div>
              </div>
              <Button icon={<DownloadOutlined />} onClick={() => handleDownload(doc)}>
                Tải về
              </Button>
            </div>
          ))}
        </Card>
      </Space>

      <Modal
        title={`Chi tiết: ${selectedStep?.name || selectedStep?.code || ''}`}
        open={!!selectedStep}
        onCancel={() => setSelectedStep(null)}
        footer={[
          <Button key="close" onClick={() => setSelectedStep(null)}>
            Đóng
          </Button>,
        ]}
      >
        <div style={{ marginBottom: 8 }}>
          Trạng thái:{' '}
          <Tag color={statusColor(selectedStep?.status)}>{selectedStep?.status}</Tag>
        </div>

        {selectedStep?.note && (
          <>
            <Divider orientation="left">Ghi chú</Divider>
            <Alert type="info" message={selectedStep.note} />
          </>
        )}

        <Divider orientation="left">Tài liệu</Divider>
        {(docsByMilestone.get(selectedStep?.code || '') || []).length === 0 && <div>Không có tài liệu công khai cho bước này.</div>}
        {(docsByMilestone.get(selectedStep?.code || '') || []).map((doc) => (
          <div key={doc.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0' }}>
            <div style={{ fontWeight: 600 }}>{doc.name}</div>
            <Button icon={<DownloadOutlined />} onClick={() => handleDownload(doc)}>
              Tải về
            </Button>
          </div>
        ))}
      </Modal>
    </div>
  );
};

export default CustomerPortalPage;
