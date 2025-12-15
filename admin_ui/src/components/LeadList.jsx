import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Table, Tag, Space, Button, Input, message, Modal, Form, InputNumber, Divider } from 'antd';
import { SearchOutlined, EyeOutlined, PlusOutlined } from '@ant-design/icons';
import { Link } from 'react-router-dom';
import { createLead, fetchLeads } from '../api/leads';

const leadStatusMap = {
  NEW: { label: 'Lead mới', color: 'blue' },
  PROCESSING: { label: 'Đang xử lý', color: 'geekblue' },
  DUPLICATE: { label: 'Trùng', color: 'orange' },
  WON: { label: 'Chốt', color: 'green' },
};

const paymentStatusMap = {
  PAID: { label: 'Đã thanh toán', color: 'green' },
  PENDING: { label: 'Chờ thanh toán', color: 'orange' },
};

const LeadList = () => {
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [leads, setLeads] = useState([]);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const loadLeads = useCallback(
    async (search) => {
      setLoading(true);
      try {
        const data = await fetchLeads(search);
        setLeads(Array.isArray(data) ? data : []);
      } catch (err) {
        message.error('Không tải được danh sách lead');
        setLeads([]);
      } finally {
        setLoading(false);
      }
    },
    [],
  );

  useEffect(() => {
    loadLeads();
  }, [loadLeads]);

  useEffect(() => {
    const timer = setTimeout(() => loadLeads(keyword), 400);
    return () => clearTimeout(timer);
  }, [keyword, loadLeads]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      const payload = {
        ten_nguoi_gui: values.fullName,
        sdt: values.phone,
        email: values.email,
        dia_chi_dn: values.businessAddress,
        ten_dn_option_1: values.businessNames?.[0],
        ten_dn_option_2: values.businessNames?.[1],
        ten_dn_option_3: values.businessNames?.[2],
        ten_dn_option_4: values.businessNames?.[3],
        ten_dn_option_5: values.businessNames?.[4],
        nganh_nghe: values.industry,
        nhu_cau: values.needs,
        mb_ref_id: values.mbRefId,
        charter_capital: values.charterCapital,
      };
      await createLead(payload);
      message.success('Tạo lead thành công');
      setCreateModalOpen(false);
      form.resetFields();
      loadLeads(keyword);
    } catch (err) {
      if (err?.errorFields) return;
      message.error('Không tạo được lead');
    } finally {
      setSubmitting(false);
    }
  };

  const columns = useMemo(
    () => [
      { title: 'Lead ID', dataIndex: 'id', key: 'id' },
      { title: 'Khách hàng', dataIndex: 'fullName', key: 'fullName' },
      { title: 'Số điện thoại', dataIndex: 'phone', key: 'phone' },
      { title: 'Email', dataIndex: 'email', key: 'email' },
      {
        title: 'Gói',
        dataIndex: 'packageCode',
        key: 'packageCode',
        render: (pkg) => (pkg ? <Tag color="geekblue">{pkg}</Tag> : <Tag>Chưa chọn</Tag>),
      },
      {
        title: 'Thanh toán',
        dataIndex: 'paymentStatus',
        key: 'paymentStatus',
        render: (status) =>
          status ? (
            <Tag color={paymentStatusMap[status]?.color || 'blue'}>{paymentStatusMap[status]?.label || status}</Tag>
          ) : (
            <Tag color="default">N/A</Tag>
          ),
      },
      {
        title: 'Trạng thái lead',
        dataIndex: 'status',
        key: 'status',
        render: (status) => (
          <Tag color={leadStatusMap[status]?.color || 'default'}>{leadStatusMap[status]?.label || status}</Tag>
        ),
      },
      { title: 'Ngày tạo', dataIndex: 'createdAt', key: 'createdAt' },
      {
        title: 'Hành động',
        key: 'action',
        render: (_, record) => (
          <Space>
            <Link to={`/tracking/${record.id}`}>
              <EyeOutlined style={{ marginRight: 4 }} />
              Theo dõi
            </Link>
          </Space>
        ),
      },
    ],
    [],
  );

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <div>
          <h2 style={{ margin: 0 }}>Danh sách Lead</h2>
          <p style={{ margin: 0, color: '#64748b' }}>Dữ liệu lấy từ API backend</p>
        </div>
        <Space>
          <Input
            placeholder="Tìm theo tên, sđt hoặc ID..."
            allowClear
            prefix={<SearchOutlined />}
            style={{ width: 280 }}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalOpen(true)}>
            Thêm lead
          </Button>
        </Space>
      </div>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={leads}
        loading={loading}
        pagination={{ pageSize: 10, showSizeChanger: false }}
      />

      <Modal
        title="Nhập lead thủ công"
        open={createModalOpen}
        onCancel={() => setCreateModalOpen(false)}
        onOk={handleCreate}
        confirmLoading={submitting}
        width={680}
      >
        <Form layout="vertical" form={form}>
          <Form.Item
            label="Họ tên"
            name="fullName"
            rules={[{ required: true, message: 'Nhập họ tên' }]}
          >
            <Input placeholder="Nguyễn Văn A" />
          </Form.Item>
          <Form.Item
            label="Số điện thoại"
            name="phone"
            rules={[
              { required: true, message: 'Nhập số điện thoại' },
              { pattern: /^(0[3|5|7|8|9])[0-9]{8}$/, message: 'Số điện thoại không hợp lệ' },
            ]}
          >
            <Input placeholder="098xxxxxxx" />
          </Form.Item>
          <Form.Item label="Email" name="email" rules={[{ type: 'email', message: 'Email không hợp lệ' }]}>
            <Input placeholder="email@example.com" />
          </Form.Item>
          <Form.Item label="Địa chỉ doanh nghiệp" name="businessAddress">
            <Input placeholder="Số 1 Trần Duy Hưng..." />
          </Form.Item>
          <Form.Item label="Tên doanh nghiệp (tối đa 5 option)">
            <Form.List name="businessNames">
              {(fields, { add, remove }) => (
                <>
                  {fields.map((field, idx) => (
                    <Space key={field.key} style={{ display: 'flex', marginBottom: 8 }} align="baseline">
                      <Form.Item name={field.name} fieldKey={field.key} noStyle>
                        <Input placeholder={`Option ${idx + 1}`} style={{ width: 380 }} />
                      </Form.Item>
                      <Button onClick={() => remove(field.name)}>Xoá</Button>
                    </Space>
                  ))}
                  {fields.length < 5 && (
                    <Button type="dashed" onClick={() => add()} block>
                      Thêm tên DN
                    </Button>
                  )}
                </>
              )}
            </Form.List>
          </Form.Item>
          <Divider />
          <Form.Item label="Ngành nghề" name="industry">
            <Input placeholder="Ngành nghề kinh doanh" />
          </Form.Item>
          <Form.Item label="Nhu cầu" name="needs">
            <Input.TextArea rows={3} placeholder="Mô tả nhu cầu khách hàng" />
          </Form.Item>
          <Form.Item label="Vốn điều lệ" name="charterCapital">
            <InputNumber controls={false} style={{ width: '100%' }} placeholder="vd: 100000000" />
          </Form.Item>
          <Form.Item label="MB Ref ID" name="mbRefId">
            <Input placeholder="Mã tham chiếu ngân hàng (nếu có)" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default LeadList;
