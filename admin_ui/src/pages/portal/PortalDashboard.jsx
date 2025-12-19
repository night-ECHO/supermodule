import React, { useEffect, useState } from 'react';
import { Layout, Card, Steps, List, Button, Tag, Typography, Spin, Empty, Divider } from 'antd';
import { FilePdfOutlined, DownloadOutlined, LogoutOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getCustomerDashboard, getCustomerDocuments } from '../../api/portal';

const { Header, Content } = Layout;

const PortalDashboard = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [data, setData] = useState(null);
    const [docs, setDocs] = useState([]);

    const leadId = sessionStorage.getItem('customer_lead_id');

    useEffect(() => {
        if (!leadId) {
            navigate('/login'); // Hoặc trang lỗi
            return;
        }
        loadData();
    }, [leadId]);

    const loadData = async () => {
        try {
            const [dashboardRes, docsRes] = await Promise.all([
                getCustomerDashboard(leadId),
                getCustomerDocuments(leadId)
            ]);
            setData(dashboardRes);
            setDocs(docsRes);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        sessionStorage.clear();
        navigate('/login'); // Hoặc redirect về trang chủ công ty
    };

    if (loading) return <div style={{ display: 'flex', height: '100vh', justifyContent: 'center', alignItems: 'center' }}><Spin size="large" /></div>;

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Header style={{ background: '#fff', padding: '0 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
                <div style={{ fontSize: 18, fontWeight: 'bold', color: '#1890ff' }}>AdFlex Portal</div>
                <Button icon={<LogoutOutlined />} onClick={handleLogout}>Thoát</Button>
            </Header>

            <Content style={{ padding: '24px', maxWidth: 800, margin: '0 auto', width: '100%' }}>
                {/* 1. THÔNG TIN CÔNG TY */}
                <Card style={{ marginBottom: 24, borderRadius: 12 }}>
                    <Typography.Title level={4}>{data?.leadInfo?.companyName || 'Đang cập nhật tên DN'}</Typography.Title>
                    <Typography.Text>Mã số thuế: <Tag color="blue">{data?.leadInfo?.mst}</Tag></Typography.Text>
                    <br/>
                    <Typography.Text>Trạng thái hồ sơ: <b>{data?.currentStatus}</b></Typography.Text>
                </Card>

                {/* 2. TIMELINE TIẾN ĐỘ */}
                <Card title="Tiến độ thực hiện" style={{ marginBottom: 24, borderRadius: 12 }}>
                    <Steps
                        direction="vertical"
                        current={data?.timeline?.findIndex(t => t.status === 'IN_PROGRESS') + 1}
                        items={data?.timeline?.map(item => ({
                            title: item.stepName,
                            description: item.date,
                            status: item.status === 'COMPLETED' ? 'finish' : item.status === 'IN_PROGRESS' ? 'process' : 'wait'
                        }))}
                    />
                </Card>

                {/* 3. KHO TÀI LIỆU CÔNG KHAI */}
                <Card title="Tài liệu hồ sơ" style={{ borderRadius: 12 }}>
                    {docs.length === 0 ? <Empty description="Chưa có tài liệu nào" /> : (
                        <List
                            itemLayout="horizontal"
                            dataSource={docs}
                            renderItem={item => (
                                <List.Item
                                    actions={[
                                        <Button
                                            type="link"
                                            icon={<DownloadOutlined />}
                                            href={`http://localhost:8081/api/documents/download/${item.id}`} // Link download trực tiếp
                                            target="_blank"
                                        >
                                            Tải về
                                        </Button>
                                    ]}
                                >
                                    <List.Item.Meta
                                        avatar={<FilePdfOutlined style={{ fontSize: 24, color: '#ff4d4f' }} />}
                                        title={item.fileName}
                                        description={`Cập nhật: ${item.uploadedAt || 'Mới nhất'}`}
                                    />
                                </List.Item>
                            )}
                        />
                    )}
                </Card>
            </Content>
        </Layout>
    );
};

export default PortalDashboard;