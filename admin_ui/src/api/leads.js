import axios from 'axios';
import api from './client';

const profileApi = axios.create({
  baseURL: import.meta.env.VITE_PROFILE_API_BASE || 'http://localhost:8081',
});

const PROFILE_API_KEY = import.meta.env.VITE_PROFILE_API_KEY || 'super_secret_webhook_key_123';

export const fetchLeads = async (keyword) => {
  const res = await api.get('/api/admin/leads', {
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
  const res = await api.get(`/api/admin/leads/${leadId}`);
  return res.data;
};

export const fetchProgress = async (leadId) => {
  const res = await api.get(`/api/admin/leads/${leadId}/progress`);
  return res.data;
};

export const updateProgress = async (leadId, milestoneCode, data) => {
  const res = await api.post(`/api/admin/leads/${leadId}/progress/${milestoneCode}`, data);
  return res.data;
};

export const confirmPayment = async (orderId) => {
  const res = await api.post(`/api/payment/pay/${orderId}`);
  return res.data;
};

export const confirmPackage = async (leadId, payload) => {
  const res = await api.post(`/api/admin/leads/${leadId}/confirm-package`, payload);
  return res.data;
};

export const uploadProof = async (file) => {
  const form = new FormData();
  form.append('file', file);
  const res = await api.post('/api/admin/proofs', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
};
