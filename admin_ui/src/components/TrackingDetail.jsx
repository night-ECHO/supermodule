import React, { useEffect, useMemo, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Button, Row, Col, Tag, Descriptions, Modal, Alert, message, Divider, Spin, Input, Form, Select, Checkbox, Space, Upload } from 'antd';
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  DollarOutlined,
  ArrowLeftOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import {
  confirmPayment,
  confirmPackage,
  fetchLeadDetail,
  fetchProgress,
  updateDocumentVisibility,
  updateProgress,
  uploadProof,
  initProgress,
  uploadDocument,
  fetchDocuments,
  updateDocument,
  ensureCustomerPortal,
  resetCustomerPortalPasscode,
  confirmOrderContract,
  uploadContract,
} from '../api/leads';
import { useCurrentUser } from '../hooks/useCurrentUser'; // Điều chỉnh đường dẫn nếu cần
import { useAuth } from '../context/AuthContext';

const TrackingDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const currentUser = useCurrentUser(); // Lấy user hiện tại: { username, role }
  const isAdmin = currentUser?.role === 'ADMIN'; // Chỉ admin mới thấy nút xác nhận thanh toán
  const { token } = useAuth();

  const [lead, setLead] = useState(null);
  const [progress, setProgress] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedStep, setSelectedStep] = useState(null);
  const [note, setNote] = useState('');
  const [proofDocId, setProofDocId] = useState('');
  const [proofFileName, setProofFileName] = useState('');
  const [proofPreviewUrl, setProofPreviewUrl] = useState('');
  const [proofIsPublic, setProofIsPublic] = useState(false);
  const [packageModalOpen, setPackageModalOpen] = useState(false);
  const [packageForm] = Form.useForm();

  // Documents and portal state
  const [documents, setDocuments] = useState([]);
  const [docsLoading, setDocsLoading] = useState(false);
  const [portalModalVisible, setPortalModalVisible] = useState(false);
  const [portalData, setPortalData] = useState(null);
  const [contractUploading, setContractUploading] = useState(false);

const addonOptions = [
    { label: 'Zalo OA', value: 'ZALO_OA', level: 1 },
    { label: 'Website', value: 'WEBSITE', level: 2 },
    { label: 'Dịch vụ kế toán thuế 3 tháng', value: 'ADDON_TAX_3M', level: 1 },
    { label: 'Google Business', value: 'ADDON_GOOGLE_BUSINESS', level: 1 },
    { label: 'Zalo MiniApp', value: 'ADDON_ZALO_MINIAPP', level: 2 },
  ];

  const packageLevels = {
    GOI_1: 1,
    GOI_2: 2,
  };

  const loadData = async () => {
    setLoading(true);
    setDocsLoading(true);
    try {
      const [leadRes, progressRes, docsRes] = await Promise.all([
        fetchLeadDetail(id),
        fetchProgress(id),
        fetchDocuments(id),
      ]);
      setLead(leadRes);
      setProgress(progressRes || []);
      setDocuments(docsRes || []);
    } catch (err) {
      message.error('Không tải được dữ liệu lead');
    } finally {
      setLoading(false);
      setDocsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [id]);

  const coreFlow = useMemo(
    () =>
      [...progress.filter((p) => p.milestoneType === 'CORE')].sort(
        (a, b) => (a.sequenceOrder || 0) - (b.sequenceOrder || 0),
      ),
    [progress],
  );

  const addOns = useMemo(
    () => progress.filter((p) => p.milestoneType === 'ADDON'),
    [progress],
  );

  const getStepStatus = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'finish';
      case 'IN_PROGRESS':
        return 'process';
      case 'WAITING_PAYMENT':
        return 'error';
      case 'LOCKED':
        return 'wait';
      default:
        return 'wait';
    }
  };

  const handleStepClick = (step) => {
    if (step.status === 'LOCKED') {
      message.warning('Bước này đang bị khóa. Hãy hoàn thành bước trước đó!');
      return;
    }
    if (step.status === 'WAITING_PAYMENT') {
      message.warning('Cần xác nhận thanh toán để mở khóa bước này');
      return;
    }
    setSelectedStep(step);
    setNote(step.note || '');
    setProofDocId(step.proofDocId || '');
    setProofPreviewUrl(step.fileLink || (step.proofDocId ? `/api/admin/proofs/${step.proofDocId}` : ''));
    setProofFileName('');
    setProofIsPublic(false);
    setModalVisible(true);
  };

  const handleConfirmPayment = async () => {
    if (!lead?.orderId) {
      message.error('Không tìm thấy đơn hàng để xác nhận thanh toán');
      return;
    }
    try {
      await confirmPayment(lead.orderId);
      message.success('Đã xác nhận thanh toán');
      loadData();
    } catch (err) {
      message.error('Không xác nhận được thanh toán');
    }
  };

  const handleCompleteStep = async () => {
    if (!selectedStep) return;
    if (selectedStep.requiredProof && !proofDocId) {
      message.warning('Bước này yêu cầu Proof. Vui lòng upload/nhập Proof Doc ID.');
      return;
    }
    try {
      if (proofIsPublic && proofDocId) {
        await updateDocumentVisibility(proofDocId, true);
      }
      await updateProgress(id, selectedStep.milestoneCode, {
        action: 'COMPLETE',
        note,
        proof_doc_id: proofDocId || undefined,
        proof_file_link: proofPreviewUrl || undefined,
      });
      message.success(`Đã hoàn thành bước: ${selectedStep.milestoneName || selectedStep.milestoneCode}`);
      setModalVisible(false);
      setSelectedStep(null);
      loadData();
    } catch (err) {
      message.error('Không cập nhật được bước');
    }
  };

  const handleStartStep = async (step) => {
    try {
      await updateProgress(id, step.milestoneCode, { action: 'START' });
      message.success('Đã bắt đầu bước');
      loadData();
    } catch (err) {
      message.error('Không thể bắt đầu bước');
    }
  };

  const handleOpenPackageModal = () => {
    packageForm.resetFields();
    setPackageModalOpen(true);
  };

  const [packageSaving, setPackageSaving] = useState(false);

  const handleConfirmPackage = async () => {
    try {
      setPackageSaving(true);
      const values = await packageForm.validateFields();
      const addons = (values.addons || []).map((a) => (a || '').toString().toUpperCase());
      const payload = {
        package_code: values.packageCode || null,
        addons,
        is_paid: values.isPaid || false,
      };
      const res = await confirmPackage(id, payload);
      message.success('Đã cập nhật gói/addon');
      setPackageModalOpen(false);
      loadData();
      return res;
    } catch (err) {
      // If AntD validation error, let form show it and don't show generic toast
      if (err?.errorFields) return;

      console.error('confirmPackage error', err);
      const serverMsg = err?.response?.data?.message || (typeof err?.response?.data === 'string' ? err?.response?.data : null) || err?.message;
      message.error(serverMsg || 'Không cập nhật được gói');
    } finally {
      setPackageSaving(false);
    }
  };

  // Documents
  const handleUploadDocument = async (file) => {
    try {
      const res = await uploadDocument(file, { leadId: id, type: 'CUSTOMER', isPublic: false });
      message.success('Đã upload document');
      loadData();
      return res;
    } catch (e) {
      message.error('Upload document thất bại');
      throw e;
    }
  };

  const handleToggleDocVisibility = async (doc) => {
    try {
      await updateDocument(doc.id, { isPublic: !doc.isPublic });
      message.success('Đã cập nhật trạng thái tài liệu');
      loadData();
    } catch (e) {
      message.error('Không cập nhật được tài liệu');
    }
  };

  // Customer portal
  const handleEnsurePortal = async () => {
    try {
      const res = await ensureCustomerPortal(id);
      setPortalData(res);
      Modal.info({
        title: 'Customer portal credentials',
        content: (
          <div>
            {res.link && <div>Link: <a href={res.link} target="_blank" rel="noreferrer">{res.link}</a></div>}
            {res.accessCode && <div>Passcode: <b>{res.accessCode}</b></div>}
          </div>
        ),
      });
    } catch (e) {
      message.error('Không tạo được portal');
    }
  };

  const handleResetPasscode = async () => {
    try {
      const res = await resetCustomerPortalPasscode(id);
      Modal.info({
        title: 'Customer portal passcode reset',
        content: (
          <div>
            {res.link && <div>Link: <a href={res.link} target="_blank" rel="noreferrer">{res.link}</a></div>}
            {res.accessCode && <div>New passcode: <b>{res.accessCode}</b></div>}
          </div>
        ),
      });
      loadData();
    } catch (e) {
      message.error('Không reset passcode');
    }
  };

  // Contract actions
  const handleConfirmOrderContract = async () => {
    if (!lead?.orderId) return message.error('Không tìm thấy đơn hàng');
    try {
      await confirmOrderContract(lead.orderId);
      message.success('Đã xác nhận hợp đồng');
      loadData();
    } catch (e) {
      message.error('Không thể xác nhận hợp đồng');
    }
  };

  const handleUploadContract = async (file) => {
    if (!lead?.orderId) return message.error('Không tìm thấy đơn hàng');
    try {
      setContractUploading(true);
      await uploadContract(lead.orderId, file);
      message.success('Tải hợp đồng lên thành công');
      loadData();
    } catch (e) {
      message.error('Upload hợp đồng thất bại');
      throw e;
    } finally {
      setContractUploading(false);
    }
  };

  const handleInitProgress = async () => {
    try {
      await initProgress(id);
      message.success('Đã khởi tạo tiến trình');
      loadData();
    } catch (e) {
      message.error('Không thể khởi tạo tiến trình');
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', marginTop: 80 }}>
        <Spin />
      </div>
    );
  }

  if (!lead) {
    return <Alert type="error" message="Không tìm thấy lead" />;
  }

  const paymentPaid = lead.paymentStatus === 'PAID';

  return (
    <div>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/')} style={{ marginBottom: 16 }}>
        Quay lại
      </Button>

      <Card title="Thông tin Hồ Sơ" bordered={false} style={{ marginBottom: 24 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="Khách hàng">
            <b>{lead.fullName}</b>
          </Descriptions.Item>
          <Descriptions.Item label="Số điện thoại">{lead.phone}</Descriptions.Item>
          <Descriptions.Item label="Email">{lead.email || '—'}</Descriptions.Item>
          <Descriptions.Item label="Gói dịch vụ">
            {lead.packageCode ? <Tag color="geekblue">{lead.packageCode}</Tag> : <Tag>Chưa chọn gói</Tag>}
          </Descriptions.Item>
          <Descriptions.Item label="Trạng thái Thanh toán">
            {paymentPaid ? (
              <Tag color="success" icon={<CheckCircleOutlined />}>
                ĐÃ THANH TOÁN
              </Tag>
            ) : (
              <Tag color="error" icon={<ClockCircleOutlined />}>
                CHƯA THANH TOÁN
              </Tag>
            )}
            {/* Chỉ hiển thị nút xác nhận thanh toán nếu chưa thanh toán và user là admin */}
            {!paymentPaid && isAdmin && (
              <Button
                size="small"
                type="primary"
                onClick={handleConfirmPayment}
                icon={<DollarOutlined />}
                style={{ marginLeft: 12 }}
              >
                Xác nhận tiền về
              </Button>
            )}
          </Descriptions.Item>
          <Descriptions.Item label="Địa chỉ DN">{lead.businessAddress || '—'}</Descriptions.Item>
          <Descriptions.Item label="Tên DN gợi ý">
            {lead.businessNameOptions?.length ? lead.businessNameOptions.join(', ') : '—'}
          </Descriptions.Item>
          <Descriptions.Item label="Nhu cầu/Ngành nghề">{lead.industryNeeds || '—'}</Descriptions.Item>
          <Descriptions.Item label="MB Ref ID">{lead.mbRefId || '—'}</Descriptions.Item>
          <Descriptions.Item label="Chọn gói/dịch vụ">
            <Space direction="vertical">
              <Space>
                <Button type="primary" onClick={handleOpenPackageModal} size="small">
                  Cập nhật gói / Add-on
                </Button>
                {lead.packageCode && <Tag color="geekblue">{lead.packageCode}</Tag>}
                {progress.length === 0 && (
                  <Button size="small" onClick={handleInitProgress}>
                    Khởi tạo tiến trình
                  </Button>
                )}
              </Space>

              <Space>
                <Button size="small" onClick={handleConfirmOrderContract} disabled={!lead.orderId}>
                  Xác nhận hợp đồng
                </Button>

                <Upload
                  accept="application/pdf"
                  customRequest={async ({ file, onSuccess, onError }) => {
                    try {
                      await handleUploadContract(file);
                      onSuccess(null, file);
                    } catch (e) {
                      onError(e);
                    }
                  }}
                  showUploadList={false}
                  disabled={!lead.orderId}
                >
                  <Button size="small" icon={<UploadOutlined />}>Tải hợp đồng (PDF)</Button>
                </Upload>
              </Space>
            </Space>
          </Descriptions.Item>

          <Descriptions.Item label="Cổng khách hàng">
            <Space>
              <Button size="small" onClick={handleEnsurePortal}>Sinh link / Gửi</Button>
              <Button size="small" onClick={handleResetPasscode}>Reset passcode</Button>
              {lead.trackingToken && (
                <a href={`${import.meta.env.VITE_PORTAL_BASE || 'https://portal.adflex.vn/track'}/${lead.trackingToken}`} target="_blank" rel="noreferrer">Mở link</a>
              )}
            </Space>
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Row gutter={24}>
        <Col span={16}>
          <Card title="Quy trình xử lý (Core Flow)" bordered={false}>
            {coreFlow.find((s) => s.status === 'WAITING_PAYMENT') && (
              <Alert
                message="Đang chờ thanh toán"
                description="Bước tiếp theo đang bị tạm dừng cho đến khi khách hàng hoàn tất thanh toán."
                type="error"
                showIcon
                style={{ marginBottom: 20 }}
              />
            )}

            <Row gutter={[16, 16]}>
              {coreFlow.map((step) => (
                <Col span={12} key={step.milestoneCode}>
                  <Card
                    type="inner"
                    title={step.milestoneName || step.milestoneCode}
                    extra={<a onClick={() => handleStepClick(step)}>Chi tiết</a>}
                    style={{
                      borderLeft: `5px solid ${
                        step.status === 'COMPLETED' ? '#52c41a' : step.status === 'IN_PROGRESS' ? '#1890ff' : '#d9d9d9'
                      }`,
                    }}
                  >
                    <div style={{ marginBottom: 8 }}>
                      Trạng thái:{' '}
                      <Tag color={step.status === 'COMPLETED' ? 'green' : step.status === 'IN_PROGRESS' ? 'blue' : 'default'}>
                        {step.status}
                      </Tag>
                    </div>
                    <Space>
                      {step.status === 'IN_PROGRESS' && (
                        <Button size="small" type="primary" onClick={() => handleStepClick(step)}>
                          Nhập ghi chú / Hoàn thành
                        </Button>
                      )}
                    </Space>
                  </Card>
                </Col>
              ))}
            </Row>
          </Card>
        </Col>

        <Col span={8}>
          <Card title="Dịch vụ cộng thêm (Add-ons)" bordered={false}>
            {addOns.length === 0 && <div>Chưa có addon nào.</div>}
            {addOns.map((addon) => (
              <Card
                key={addon.milestoneCode}
                type="inner"
                title={addon.milestoneName || addon.milestoneCode}
                extra={<a onClick={() => handleStepClick(addon)}>Chi tiết</a>}
                style={{
                  marginBottom: 16,
                  borderLeft: `5px solid ${addon.status === 'COMPLETED' ? '#52c41a' : '#1890ff'}`,
                }}
              >
                Trạng thái:{' '}
                <Tag color={addon.status === 'COMPLETED' ? 'green' : addon.status === 'IN_PROGRESS' ? 'blue' : 'default'}>
                  {addon.status}
                </Tag>
                <Space style={{ marginTop: 8 }}>
                  {addon.status === 'IN_PROGRESS' && (
                    <Button size="small" type="primary" onClick={() => handleStepClick(addon)}>
                      Hoàn thành
                    </Button>
                  )}
                </Space>
              </Card>
            ))}
          </Card>

          <Card title="Tài liệu (Documents)" bordered={false} style={{ marginTop: 16 }}>
            <Space style={{ marginBottom: 8 }}>
              <Upload
                customRequest={async ({ file, onSuccess, onError }) => {
                  try {
                    await handleUploadDocument(file);
                    onSuccess(null, file);
                  } catch (e) {
                    onError(e);
                  }
                }}
                showUploadList={false}
              >
                <Button icon={<UploadOutlined />}>Tải tài liệu</Button>
              </Upload>
            </Space>

            {docsLoading ? (
              <Spin />
            ) : documents.length === 0 ? (
              <div>Không có tài liệu.</div>
            ) : (
              documents.map((d) => (
                <Card type="inner" key={d.id} style={{ marginBottom: 8 }}>
                  <Row justify="space-between" align="middle">
                    <Col>
                      <div><b>{d.name}</b> {d.type && <Tag>{d.type}</Tag>}</div>
                      <div style={{ color: '#666', fontSize: 12 }}>{d.uploadedAt}</div>
                    </Col>
                    <Col>
                      <Space>
                        <Button size="small" onClick={() => handleToggleDocVisibility(d)}>
                          {d.isPublic ? 'Công khai' : 'Riêng tư'}
                        </Button>
                      </Space>
                    </Col>
                  </Row>
                </Card>
              ))
            )}
          </Card>
        </Col>
      </Row>

<Modal
  title={`Xử lý bước: ${selectedStep?.milestoneName || selectedStep?.milestoneCode || ''}`}
  open={modalVisible}
  onCancel={() => setModalVisible(false)}
  footer={[
    <Button key="close" onClick={() => setModalVisible(false)}>
      Đóng
    </Button>,
    <Button
      key="submit"
      type="primary"
      onClick={handleCompleteStep}
      disabled={selectedStep?.status === 'COMPLETED'}
    >
      Hoàn thành Bước
    </Button>,
  ]}
>
  <p>
    <b>Trạng thái hiện tại:</b> {selectedStep?.status}
  </p>

  <Divider orientation="left">Ghi chú công việc</Divider>
  <Input.TextArea
    rows={4}
    value={note}
    onChange={(e) => setNote(e.target.value)}
    placeholder="Nhập ghi chú"
  />

  {selectedStep?.milestoneType !== 'ADDON' && (
    <>
      <Divider orientation="left">File bằng chứng</Divider>

      {/* Upload + Preview + Download */}
      <Space align="center">
        {/* Nút upload */}
        <Upload
          customRequest={async ({ file, onSuccess, onError }) => {
            try {
              const res = await uploadProof(file, {
                leadId: id,
                milestoneCode: selectedStep?.milestoneCode,
                isPublic: proofIsPublic,
              });
              setProofDocId(res.id);
              setProofFileName(res.fileName || file.name || '');
              setProofPreviewUrl(res.fileLink || `/api/admin/proofs/${res.id}`);

              onSuccess(res, file);
              message.success(`Đã tải lên: ${file.name}`);
            } catch (e) {
              onError(e);
              message.error('Upload proof thất bại');
            }
          }}
          showUploadList={false}
        >
          <Button icon={<UploadOutlined />}>Tải file proof</Button>
        </Upload>

        <Checkbox checked={proofIsPublic} onChange={(e) => setProofIsPublic(e.target.checked)}>
          Công khai tài liệu cho khách hàng
        </Checkbox>

        {/* PREVIEW */}
        <Button
          type="link"
          disabled={!proofPreviewUrl}
          onClick={() => window.open(proofPreviewUrl, '_blank')}
        >
          Preview
        </Button>

        {/* DOWNLOAD */}
        <Button
          type="link"
          disabled={!proofPreviewUrl}
          onClick={async () => {
            if (!proofPreviewUrl) return;

            try {
              const response = await fetch(proofPreviewUrl, {
                method: 'GET',
                headers: { Authorization: `Bearer ${token}` },
              });

              if (!response.ok) throw new Error();

              const blob = await response.blob();
              const url = window.URL.createObjectURL(blob);
              const a = document.createElement('a');

              a.href = url;
              a.download = proofFileName || 'proof_file';
              a.click();

              window.URL.revokeObjectURL(url);
              message.success('Tải file thành công');
            } catch (err) {
              message.error('Không thể tải file.');
            }
          }}
        >
          Download
        </Button>
      </Space>

      {/* Tên file */}
      {proofFileName && (
        <div style={{ marginTop: 6, color: '#555' }}>
          File đã upload: <b>{proofFileName}</b>
        </div>
      )}

      {/* Cảnh báo nếu bước yêu cầu proof */}
      {selectedStep?.requiredProof && (
        <Alert
          style={{ marginTop: 8 }}
          type="warning"
          message="Bước này yêu cầu Proof. Vui lòng upload file."
        />
      )}
    </>
  )}
</Modal>


      <Modal
        title="Chọn gói dịch vụ / Add-on"
        open={packageModalOpen}
        onCancel={() => setPackageModalOpen(false)}
        onOk={handleConfirmPackage}
        okText="Lưu"
        width={560}
      >
        <Form layout="vertical" form={packageForm} colon={false}>
          <Form.Item label="Gói dịch vụ" name="packageCode">
            <Select
              allowClear
              placeholder="Chưa chọn gói"
              options={[
                { label: 'GÓI 1', value: 'GOI_1' },
                { label: 'GÓI 2', value: 'GOI_2' },
              ]}
            />
          </Form.Item>
          <Form.Item label="Add-ons" name="addons">
            <Checkbox.Group style={{ width: '100%' }}>
              <Row gutter={[8, 8]}>
                {addonOptions.map((addon) => (
                  <Col span={12} key={addon.value}>
                    <Checkbox value={addon.value}>{addon.label}</Checkbox>
                  </Col>
                ))}
              </Row>
            </Checkbox.Group>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default TrackingDetail;
