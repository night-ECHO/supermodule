import axios from 'axios';
import api from './api';

const profileApi = axios.create({
  baseURL: import.meta.env.VITE_PROFILE_API_BASE || 'http://localhost:8081',
});

const PROFILE_API_KEY = import.meta.env.VITE_PROFILE_API_KEY || 'super_secret_webhook_key_123';

export const fetchLeads = async (keyword) => {
  const res = await api.get('/api/leads', {
    params: keyword ? { q: keyword } : {},
  });
  return res.data;
};

export const createLead = async (payload) => {
  const res = await profileApi.post(
    '/api/webhooks/google-form',
    { data: payload },
    {
      headers: {
        'X-Api-Key': PROFILE_API_KEY,
      },
    },
  );
  return res.data;
};

export const fetchLeadDetail = async (leadId) => {
  const res = await api.get(`/api/leads/${leadId}`);
  return res.data;
};

export const fetchProgress = async (leadId) => {
  const res = await api.get(`/api/leads/${leadId}/progress`);
  return res.data;
};

export const updateLeadInfo = async (leadId, payload) => {
  const res = await api.patch(`/api/leads/${leadId}`, payload);
  return res.data;
};

export const updateProgress = async (leadId, milestoneCode, data) => {
  const res = await api.post(`/api/leads/${leadId}/progress/${milestoneCode}`, data);
  return res.data;
};

export const confirmPayment = async (orderId) => {
  const res = await api.post(`/api/admin/payment/pay/${orderId}`);
  return res.data;
};

export const confirmPackage = async (leadId, payload) => {
  try {
    const res = await api.post(`/api/leads/${leadId}/confirm-package`, payload);
    return res.data;
  } catch (err) {
    // Debug: log payload and server response for easier triage
    console.error('[leads.confirmPackage] error', {
      leadId,
      payload,
      status: err?.response?.status,
      responseData: err?.response?.data,
      responseHeaders: err?.response?.headers,
      request: err?.request,
      message: err?.message,
    });
    throw err;
  }
};

export const updateDocumentVisibility = async (docId, isPublic) => {
  const res = await api.patch(`/api/admin/documents/${docId}`, {
    isPublic,
  });
  return res.data;
};

export const uploadProof = async (file, { leadId, milestoneCode, isPublic } = {}) => {
  const form = new FormData();
  form.append('file', file);
  const res = await api.post('/api/admin/proofs', form, {
    params: {
      lead_id: leadId,
      milestone_code: milestoneCode,
      is_public: !!isPublic,
    },
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
};

export const initProgress = async (leadId) => {
  const res = await api.post(`/api/leads/${leadId}/init-progress`);
  return res.data;
};

export const uploadDocument = async (file, { leadId, type, isPublic } = {}) => {
  const form = new FormData();
  form.append('file', file);
  const res = await api.post(`/api/admin/leads/${leadId}/documents`, form, {
    params: {
      type,
      is_public: !!isPublic,
    },
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
};

export const fetchDocuments = async (leadId) => {
  const res = await api.get(`/api/admin/leads/${leadId}/documents`);
  return res.data;
};

export const updateDocument = async (docId, payload) => {
  const res = await api.patch(`/api/admin/documents/${docId}`, payload);
  return res.data;
};

export const ensureCustomerPortal = async (leadId, portalBaseUrl) => {
  const res = await api.post(`/api/admin/leads/${leadId}/customer-portal/ensure`, null, {
    params: portalBaseUrl ? { portal_base_url: portalBaseUrl } : {},
  });
  return res.data;
};

export const resetCustomerPortalPasscode = async (leadId, portalBaseUrl) => {
  const res = await api.post(`/api/admin/leads/${leadId}/customer-portal/reset-passcode`, null, {
    params: portalBaseUrl ? { portal_base_url: portalBaseUrl } : {},
  });
  return res.data;
};

export const confirmOrderPayment = async (orderId) => {
  const res = await api.post(`/api/admin/orders/${orderId}/confirm-payment`);
  return res.data;
};

export const fetchOrderPaymentInfo = async (orderId) => {
  const res = await api.get(`/api/admin/orders/${orderId}/public-payment`);
  return res.data;
};

export const confirmOrderContract = async (orderId) => {
  const res = await api.post(`/api/admin/orders/${orderId}/confirm-contract`);
  return res.data;
};

export const uploadContract = async (orderId, file) => {
  const form = new FormData();
  form.append('file', file);
  const res = await api.post(`/api/admin/orders/${orderId}/upload-contract`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
};

export const getProofUrl = (docId) => `/api/admin/proofs/${docId}`;

export const getCustomerPortalLink = async (leadId, portalBaseUrl) => {
  const res = await api.get(`/api/admin/leads/${leadId}/customer-portal/link`, {
    params: portalBaseUrl ? { portal_base_url: portalBaseUrl } : {},
  });
  return res.data;
};
