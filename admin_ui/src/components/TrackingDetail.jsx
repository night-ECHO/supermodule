import React, { useEffect, useMemo, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Button,
  Row,
  Col,
  Tag,
  Descriptions,
  Modal,
  Alert,
  message,
  Divider,
  Spin,
  Input,
  Form,
  Select,
  Checkbox,
  Space,
  Upload,
  Progress,
  Steps,
} from 'antd';
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  DollarOutlined,
  ArrowLeftOutlined,
  UploadOutlined,
  QrcodeOutlined,
  LinkOutlined,
  CopyOutlined,
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
  getCustomerPortalLink,
  fetchOrderPaymentInfo,
  confirmOrderContract,
  uploadContract,
  updateLeadInfo,
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
  const [proofList, setProofList] = useState([]);
  const [packageModalOpen, setPackageModalOpen] = useState(false);
  const [packageForm] = Form.useForm();

  // Documents and portal state
  const [documents, setDocuments] = useState([]);
  const [docsLoading, setDocsLoading] = useState(false);
  const [portalModalVisible, setPortalModalVisible] = useState(false);
  const [portalData, setPortalData] = useState(null);
  const [portalLink, setPortalLink] = useState('');
  const [contractUploading, setContractUploading] = useState(false);
  const [contractPreviewUrl, setContractPreviewUrl] = useState('');
  const [contractFileName, setContractFileName] = useState('');
  const [contractConfirmed, setContractConfirmed] = useState(false);
  const [editLeadModalOpen, setEditLeadModalOpen] = useState(false);
  const [editLeadSaving, setEditLeadSaving] = useState(false);
  const [editLeadForm] = Form.useForm();

  // Payment QR modal
  const [paymentModalVisible, setPaymentModalVisible] = useState(false);
  const [paymentInfo, setPaymentInfo] = useState(null);
  const [paymentLoading, setPaymentLoading] = useState(false);
  const paymentPaid = lead?.paymentStatus === 'PAID';
  const packageLocked = paymentPaid && !!lead?.packageCode;

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

      // Contract detection
      const contractDoc = (docsRes || []).find((d) => (d.type || '').toUpperCase() === 'CONTRACT');
      if (contractDoc) {
        setContractPreviewUrl(`/api/admin/documents/${contractDoc.id}/download`);
        setContractFileName(contractDoc.name || 'contract.pdf');
      } else {
        setContractPreviewUrl('');
        setContractFileName('');
      }
      const isContractDone =
        (leadRes?.contractStatus && leadRes.contractStatus === 'SIGNED_HARD_COPY') ||
        !!contractDoc;
      setContractConfirmed(isContractDone);

      // Try fetch portal link (safe GET that DOES NOT reset access code)
      if (leadRes?.trackingToken) {
        try {
          const linkRes = await getCustomerPortalLink(id);
          setPortalLink(linkRes?.link || '');
        } catch (e) {
          // ignore — optional link
          setPortalLink('');
        }
      } else {
        setPortalLink('');
      }
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

  const statusMeta = {
    COMPLETED: { color: 'green', label: 'Hoàn thành' },
    IN_PROGRESS: { color: 'blue', label: 'Đang làm' },
    WAITING_PAYMENT: { color: 'red', label: 'Chờ thanh toán' },
    LOCKED: { color: 'default', label: 'Chờ mở khóa' },
    default: { color: 'default', label: 'Chưa bắt đầu' },
  };

  const completedCount = coreFlow.filter((s) => s.status === 'COMPLETED').length;
  const totalSteps = coreFlow.length || 1;
  const completionPercent = Math.round((completedCount / totalSteps) * 100);

  const currentStepIndex =
    coreFlow.findIndex((s) => s.status !== 'COMPLETED') === -1
      ? Math.max(coreFlow.length - 1, 0)
      : coreFlow.findIndex((s) => s.status !== 'COMPLETED');

  const nextStep =
    coreFlow.find((s) => s.status === 'IN_PROGRESS' || s.status === 'WAITING_PAYMENT') ||
    coreFlow.find((s) => s.status !== 'COMPLETED');

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
    const proofs = Array.isArray(step.proofs) ? step.proofs : [];
    setProofList(proofs);
    const previewProof =
      proofs.find((p) => p.id === step.proofDocId) || proofs[0] || null;
    setProofDocId(previewProof?.id || step.proofDocId || '');
    setProofPreviewUrl(
      previewProof?.fileLink ||
        step.fileLink ||
        (step.proofDocId ? `/api/admin/proofs/${step.proofDocId}` : ''),
    );
    setProofFileName(previewProof?.name || '');
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

  const handleOpenPaymentModal = async () => {
    if (!lead?.orderId) {
      message.error('Không tìm thấy đơn hàng để tạo thông tin thanh toán');
      return;
    }
    setPaymentLoading(true);
    try {
      const info = await fetchOrderPaymentInfo(lead.orderId);
      setPaymentInfo(info);
      setPaymentModalVisible(true);
    } catch (err) {
      message.error('Không lấy được thông tin thanh toán');
    } finally {
      setPaymentLoading(false);
    }
  };

  const handleCopyPaymentLink = async () => {
    if (!paymentInfo?.paymentLink) return;
    try {
      await navigator.clipboard.writeText(paymentInfo.paymentLink);
      message.success('Đã sao chép link thanh toán');
    } catch (err) {
      message.error('Không sao chép được link');
    }
  };

  const handleRefreshPaymentInfo = async () => {
    try {
      await loadData();
      if (lead?.orderId) {
        const info = await fetchOrderPaymentInfo(lead.orderId);
        setPaymentInfo(info);
      }
    } catch (err) {
      // ignore
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
    packageForm.setFieldsValue({
      packageCode: lead?.packageCode || undefined,
      addons: Array.isArray(lead?.addons) ? lead.addons : undefined,
    });
    if (packageLocked) {
      message.info('Gói dịch vụ đã khóa sau khi thanh toán. Bạn chỉ có thể quản lý Add-on.');
    }
    setPackageModalOpen(true);
  };

  const [packageSaving, setPackageSaving] = useState(false);

  const handleConfirmPackage = async () => {
    try {
      setPackageSaving(true);
      const values = await packageForm.validateFields();
      const addons = (values.addons || []).map((a) => (a || '').toString().toUpperCase());
      if (packageLocked && values.packageCode && values.packageCode !== lead.packageCode) {
        message.warning('Gói đã khóa sau thanh toán, không thể thay đổi.');
        return;
      }
      const packageCode = packageLocked ? lead.packageCode : values.packageCode || lead?.packageCode || null;

      const payload = {
        package_code: packageCode,
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
    const portalBase = import.meta.env.VITE_PORTAL_BASE || window.location.origin || 'http://localhost:5173';
    try {
      const res = await ensureCustomerPortal(id, portalBase);
      setPortalData(res);
      setPortalLink(res?.link || '');
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
      const msg = e?.response?.data?.message || e?.message || 'Không tạo được portal';
      message.error(msg);
    }
  };

  const handleResetPasscode = async () => {
    const portalBase = import.meta.env.VITE_PORTAL_BASE || window.location.origin || 'http://localhost:5173';
    try {
      const res = await resetCustomerPortalPasscode(id, portalBase);
      setPortalLink(res?.link || '');
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
      const msg = e?.response?.data?.message || e?.message || 'Không reset passcode';
      message.error(msg);
    }
  };

  // Contract actions
  const handleConfirmOrderContract = async () => {
    if (!lead?.orderId) return message.error('Không tìm thấy đơn hàng');
    if (!contractPreviewUrl) {
      return message.warning('Vui lòng tải hợp đồng (PDF) trước khi xác nhận');
    }
    try {
      const res = await confirmOrderContract(lead.orderId);
      message.success('Đã xác nhận hợp đồng');
      setContractConfirmed(true);
      if (res?.contractStatus) {
        setLead((prev) => ({ ...prev, contractStatus: res.contractStatus }));
      }
      loadData();
      // Đưa người dùng lên đầu trang để xem trạng thái sau khi xác nhận
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (e) {
      const msg = e?.response?.data?.message || e?.message || 'Không thể xác nhận hợp đồng';
      message.error(msg);
    }
  };

  const openEditLeadModal = () => {
    editLeadForm.setFieldsValue({
      fullName: lead?.fullName,
      email: lead?.email,
      mbRefId: lead?.mbRefId,
      businessAddress: lead?.businessAddress,
      businessNameOptions: lead?.businessNameOptions?.join(', ') || '',
      charterCapital: lead?.charterCapital,
      industryNeeds: lead?.industryNeeds,
    });
    setEditLeadModalOpen(true);
  };

  const handleSaveLeadInfo = async () => {
    try {
      const values = await editLeadForm.validateFields();
      setEditLeadSaving(true);
      const payload = {
        fullName: values.fullName,
        email: values.email || null,
        mbRefId: values.mbRefId || null,
        businessAddress: values.businessAddress || null,
        businessNameOptions: values.businessNameOptions
          ? values.businessNameOptions.split(',').map((s) => s.trim()).filter(Boolean)
          : [],
        charterCapital:
          values.charterCapital === undefined || values.charterCapital === null || values.charterCapital === ''
            ? null
            : Number(values.charterCapital),
        industryNeeds: values.industryNeeds || null,
      };
      const updated = await updateLeadInfo(id, payload);
      setLead(updated);
      message.success('Đã cập nhật thông tin hồ sơ');
      setEditLeadModalOpen(false);
    } catch (err) {
      if (err?.errorFields) return; // form validation
      const msg = err?.response?.data?.message || err?.message || 'Không cập nhật được thông tin';
      message.error(msg);
    } finally {
      setEditLeadSaving(false);
    }
  };

  const handleUploadContract = async (file) => {
    if (!lead?.orderId) return message.error('Không tìm thấy đơn hàng');
    try {
      setContractUploading(true);
      const res = await uploadContract(lead.orderId, file);
      setContractPreviewUrl(res?.fileLink || '');
      setContractFileName(file.name || 'contract.pdf');
      setContractConfirmed(false);
      message.success(res?.message || 'Tải hợp đồng lên thành công');
      return res;
    } catch (e) {
      const msg = e?.response?.data?.message || e?.message || 'Upload hợp đồng thất bại';
      message.error(msg);
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

  return (
    <div>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/')} style={{ marginBottom: 16 }}>
        Quay lại
      </Button>

      <Card
        title="Thông tin Hồ Sơ"
        bordered={false}
        style={{ marginBottom: 24 }}
        extra={
          <Button size="small" type="link" onClick={openEditLeadModal}>
            Sửa thông tin
          </Button>
        }
      >
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
            {/* Nút tạo QR thanh toán - hiển thị nếu chưa thanh toán và có orderId */}
            {!paymentPaid && lead?.orderId && (
              <Button
                size="small"
                onClick={handleOpenPaymentModal}
                icon={<QrcodeOutlined />}
                style={{ marginLeft: 12 }}
              >
                Thanh toán
              </Button>
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
          <Descriptions.Item label="Địa chỉ doanh nghiệp">{lead.businessAddress || '—'}</Descriptions.Item>
          <Descriptions.Item label="Tên doanh nghiệp">
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
                {packageLocked && <Tag color="red">Gói đã khóa</Tag>}
              </Space>

              {contractConfirmed ? (
                <Space>
                  <Tag color="green">Hợp đồng đã xác nhận</Tag>
                  {/* Contract preview button */}
                  <Button
                    size="small"
                    type="link"
                    disabled={!contractPreviewUrl}
                    onClick={async () => {
                      if (!contractPreviewUrl) return;
                      try {
                        const isAbsolute = /^(https?:)?\/\//i.test(contractPreviewUrl);
                        if (isAbsolute && !contractPreviewUrl.startsWith(window.location.origin)) {
                          window.open(contractPreviewUrl, '_blank');
                          return;
                        }
                        const response = await fetch(contractPreviewUrl, { method: 'GET' });
                        if (!response.ok) throw new Error('Không thể tải hợp đồng');
                        const blob = await response.blob();
                        const url = window.URL.createObjectURL(blob);
                        window.open(url, '_blank');
                        setTimeout(() => window.URL.revokeObjectURL(url), 60 * 1000);
                      } catch (err) {
                        const msg = err?.response?.data?.message || err?.message || 'Không thể preview hợp đồng';
                        message.error(msg);
                      }
                    }}
                  >
                    Preview hợp đồng
                  </Button>
                  {contractFileName && (
                    <span style={{ marginLeft: 8 }}>
                      {contractFileName}
                      <Tag color="green" style={{ marginLeft: 6 }}>
                        Đã lưu
                      </Tag>
                    </span>
                  )}
                </Space>
              ) : (
                <Space>
                  <Button size="small" onClick={handleConfirmOrderContract} disabled={!lead.orderId}>
                    Xác nhận hợp đồng
                  </Button>

                  <Upload
                    accept="application/pdf"
                    customRequest={async ({ file, onSuccess, onError }) => {
                      try {
                        const res = await handleUploadContract(file);
                        if (res) onSuccess(res, file);
                        else onSuccess(null, file);
                      } catch (e) {
                        onError(e);
                      }
                    }}
                    showUploadList={false}
                    disabled={!lead.orderId}
                  >
                    <Button size="small" icon={<UploadOutlined />}>Tải hợp đồng (PDF)</Button>
                  </Upload>

                  {/* Contract preview button */}
                  <Button
                    size="small"
                    type="link"
                    disabled={!contractPreviewUrl}
                    onClick={async () => {
                      if (!contractPreviewUrl) return;
                      try {
                        const isAbsolute = /^(https?:)?\/\//i.test(contractPreviewUrl);
                        if (isAbsolute && !contractPreviewUrl.startsWith(window.location.origin)) {
                          window.open(contractPreviewUrl, '_blank');
                          return;
                        }
                        const response = await fetch(contractPreviewUrl, { method: 'GET' });
                        if (!response.ok) throw new Error('Không thể tải hợp đồng');
                        const blob = await response.blob();
                        const url = window.URL.createObjectURL(blob);
                        window.open(url, '_blank');
                        setTimeout(() => window.URL.revokeObjectURL(url), 60 * 1000);
                      } catch (err) {
                        const msg = err?.response?.data?.message || err?.message || 'Không thể preview hợp đồng';
                        message.error(msg);
                      }
                    }}
                  >
                    Preview hợp đồng
                  </Button>

                  {contractFileName && (
                    <span style={{ marginLeft: 8 }}>
                      {contractFileName}
                      <Tag color="orange" style={{ marginLeft: 6 }}>
                        Chưa lưu
                      </Tag>
                    </span>
                  )}
                </Space>
              )}
            </Space>
          </Descriptions.Item>

          <Descriptions.Item label="Cổng khách hàng">
            <Space>
              <Button size="small" onClick={handleEnsurePortal}>Sinh link / Gửi</Button>
              <Button size="small" onClick={handleResetPasscode}>Reset passcode</Button>
              {portalLink ? (
                <a href={portalLink} target="_blank" rel="noreferrer">Mở link</a>
              ) : lead.trackingToken ? (
                <a href={`${import.meta.env.VITE_PORTAL_BASE || 'https://portal.adflex.vn/track'}/${lead.trackingToken}`} target="_blank" rel="noreferrer">Mở link</a>
              ) : null}
            </Space>
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Modal
        open={paymentModalVisible}
        title="Thanh toán"
        onCancel={() => setPaymentModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setPaymentModalVisible(false)}>
            Đóng
          </Button>,
          <Button key="copy" icon={<CopyOutlined />} onClick={handleCopyPaymentLink}>
            Sao chép link
          </Button>,
          <Button key="open" type="primary" icon={<LinkOutlined />} onClick={() => paymentInfo?.paymentLink && window.open(paymentInfo.paymentLink, '_blank')}>
            Mở link
          </Button>,
          <Button key="refresh" onClick={handleRefreshPaymentInfo}>Làm mới</Button>,
        ]}
      >
        {paymentLoading ? (
          <div style={{ textAlign: 'center' }}><Spin /></div>
        ) : paymentInfo ? (
          <div style={{ textAlign: 'center' }}>
            <div style={{ marginBottom: 12, fontSize: 16, fontWeight: 'bold' }}>
              {paymentInfo.totalAmount ? Intl.NumberFormat('vi-VN').format(Number(paymentInfo.totalAmount)) + ' VNĐ' : ''}
            </div>
            <img src={paymentInfo.qrCodeUrl} alt="QR" style={{ maxWidth: '100%' }} />
            <div style={{ marginTop: 8 }}>
              <a href={paymentInfo.paymentLink} target="_blank" rel="noreferrer">{paymentInfo.paymentLink}</a>
            </div>
          </div>
        ) : (
          <div>Không có thông tin thanh toán</div>
        )}
      </Modal>

      <Row gutter={24}>
        <Col span={16}>
          {lead.packageCode ? (
          <Card
            title="Quy trình xử lý (Core Flow)"
            bordered={false}
            extra={
              <Space size={16}>
                <div style={{ minWidth: 170 }}>
                  <div style={{ fontSize: 12, color: '#888' }}>Hoàn thành</div>
                  <Progress percent={completionPercent} size="small" showInfo />
                </div>
                {nextStep && (
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: 12, color: '#888' }}>Bước hiện tại</div>
                    <div style={{ fontWeight: 600, maxWidth: 220 }}>{nextStep.milestoneName || nextStep.milestoneCode}</div>
                    <Tag color={statusMeta[nextStep.status]?.color || statusMeta.default.color} style={{ marginTop: 4 }}>
                      {statusMeta[nextStep.status]?.label || statusMeta.default.label}
                    </Tag>
                  </div>
                )}
              </Space>
            }
          >
            {coreFlow.find((s) => s.status === 'WAITING_PAYMENT') && (
              <Alert
                message="Đang chờ thanh toán"
                description="Bước tiếp theo đang bị tạm dừng cho đến khi khách hàng hoàn tất thanh toán."
                type="error"
                showIcon
                style={{ marginBottom: 20 }}
              />
            )}

            <div style={{ padding: '8px 12px', border: '1px dashed #d9d9d9', borderRadius: 10, marginBottom: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                <div style={{ fontWeight: 600 }}>Lộ trình</div>
                <Tag color="blue">Hiện tại: {currentStepIndex + 1}/{coreFlow.length || 1}</Tag>
              </div>
              <Steps
                size="small"
                current={currentStepIndex}
                type="dot"
                items={coreFlow.map((step) => ({
                  title: step.milestoneName || step.milestoneCode,
                  description: statusMeta[step.status]?.label || statusMeta.default.label,
                  status: getStepStatus(step.status),
                }))}
              />
            </div>

            <Row gutter={[16, 16]}>
              {coreFlow.map((step, idx) => (
                <Col span={12} key={step.milestoneCode}>
                  <Card
                    type="inner"
                    title={
                      <Space align="start">
                        <div
                          style={{
                            width: 30,
                            height: 30,
                            borderRadius: '50%',
                            background: '#f0f5ff',
                            color: '#2f54eb',
                            display: 'inline-flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontWeight: 600,
                          }}
                        >
                          {idx + 1}
                        </div>
                        <div>
                          <div style={{ fontWeight: 600 }}>{step.milestoneName || step.milestoneCode}</div>
                          <div style={{ fontSize: 12, color: '#888' }}>Mã: {step.milestoneCode}</div>
                        </div>
                      </Space>
                    }
                    extra={
                      <a onClick={() => handleStepClick(step)} style={{ fontWeight: 600 }}>
                        Chi tiết
                      </a>
                    }
                    style={{
                      borderLeft: `5px solid ${
                        step.status === 'COMPLETED' ? '#52c41a' : step.status === 'IN_PROGRESS' ? '#1890ff' : '#d9d9d9'
                      }`,
                      background:
                        step.status === 'IN_PROGRESS'
                          ? 'linear-gradient(90deg, #f0f5ff 0%, #ffffff 60%)'
                          : undefined,
                    }}
                  >
                    <div style={{ marginBottom: 8 }}>
                      Trạng thái:{' '}
                      <Tag color={statusMeta[step.status]?.color || statusMeta.default.color}>
                        {step.status}
                      </Tag>
                    </div>
                    <Space>
                      {step.status === 'WAITING_PAYMENT' && (
                        <Tag color="red" bordered={false}>
                          Cần thanh toán
                        </Tag>
                      )}
                      {step.status === 'LOCKED' && (
                        <Tag color="default" bordered={false}>
                          Chưa mở khóa
                        </Tag>
                      )}
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
          ) : (
            <Card title="Quy trình xử lý (Core Flow)" bordered={false}>
              <Alert
                message="Chưa chọn gói dịch vụ"
                description="Chọn gói để khởi tạo quy trình."
                type="info"
                showIcon
              />
            </Card>
          )}
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
                <Tag color={statusMeta[addon.status]?.color || statusMeta.default.color}>
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
      disabled={
        selectedStep?.status === 'COMPLETED' ||
        (selectedStep?.requiredProof && !proofDocId)
      }
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
              setProofList((prev) => [
                {
                  id: res.id,
                  name: res.fileName || file.name || '',
                  milestoneCode: selectedStep?.milestoneCode,
                  fileLink: res.fileLink || `/api/admin/proofs/${res.id}`,
                  isPublic: res.isPublic ?? proofIsPublic,
                  size: res.size,
                  uploadedAt: res.uploadedAt,
                },
                ...prev,
              ]);

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
          onClick={async () => {
            if (!proofPreviewUrl) return;
            try {
              const isAbsolute = /^(https?:)?\/\//i.test(proofPreviewUrl);
              // For external absolute URLs on other origins, just open directly
              if (isAbsolute && !proofPreviewUrl.startsWith(window.location.origin)) {
                window.open(proofPreviewUrl, '_blank');
                return;
              }

              // Otherwise fetch with Authorization header and open blob URL
              const response = await fetch(proofPreviewUrl, {
                method: 'GET',
                headers: { Authorization: `Bearer ${token}` },
              });

              if (!response.ok) throw new Error('Không thể tải file preview');
              const blob = await response.blob();
              const url = window.URL.createObjectURL(blob);
              window.open(url, '_blank');
              // Revoke after a short while
              setTimeout(() => window.URL.revokeObjectURL(url), 60 * 1000);
            } catch (err) {
              const msg = err?.response?.data?.message || err?.message || 'Không thể preview file';
              message.error(msg);
            }
          }}
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
      {proofList?.length > 0 && (
        <div style={{ marginTop: 6, color: '#555' }}>
          File đã upload:
          <div style={{ marginTop: 4, lineHeight: 1.6 }}>
            {proofList.map((p) => (
              <div key={p.id}>
                <b>{p.name}</b>
              </div>
            ))}
          </div>
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
        <Form.Item
          label={packageLocked ? 'Gói dịch vụ (đã khóa sau thanh toán)' : 'Gói dịch vụ'}
          name="packageCode"
          tooltip={packageLocked ? 'Bạn không thể đổi gói sau khi đã thanh toán' : undefined}
        >
          <Select
            allowClear
            placeholder="Chưa chọn gói"
            disabled={packageLocked}
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
      <Modal
        title="Sửa thông tin hồ sơ"
        open={editLeadModalOpen}
        onCancel={() => setEditLeadModalOpen(false)}
        onOk={handleSaveLeadInfo}
        confirmLoading={editLeadSaving}
        okText="Lưu"
      >
        <Form layout="vertical" form={editLeadForm} colon={false}>
          <Form.Item label="Họ tên" name="fullName" rules={[{ required: true, message: 'Nhập họ tên' }]}>
            <Input placeholder="Nhập họ tên" />
          </Form.Item>
          <Form.Item label="Email" name="email" rules={[{ type: 'email', message: 'Email không hợp lệ' }]}>
            <Input placeholder="example@domain.com" />
          </Form.Item>
          <Form.Item label="MB Ref ID" name="mbRefId">
            <Input placeholder="MB Ref ID" />
          </Form.Item>
          <Form.Item label="Địa chỉ doanh nghiệp" name="businessAddress">
            <Input placeholder="Địa chỉ" />
          </Form.Item>
          <Form.Item label="Tên doanh nghiệp (comma-separated)" name="businessNameOptions">
            <Input placeholder="Ví dụ: Tên 1, Tên 2" />
          </Form.Item>
          <Form.Item label="Vốn điều lệ" name="charterCapital">
            <Input type="number" placeholder="Số tiền" />
          </Form.Item>
          <Form.Item label="Nhu cầu / Ngành nghề" name="industryNeeds">
            <Input.TextArea rows={3} placeholder="Mô tả nhu cầu" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default TrackingDetail;
