import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE || 'http://localhost:8081';

export const customerAuth = async (trackingToken, accessCode) => {
  const res = await axios.post(`${baseURL}/api/customer/auth/${trackingToken}`, {
    access_code: accessCode,
  });
  return res.data;
};

export const fetchCustomerTracking = async (customerToken) => {
  const res = await axios.get(`${baseURL}/api/customer/tracking`, {
    headers: { Authorization: `Bearer ${customerToken}` },
  });
  return res.data;
};

export const downloadCustomerDocument = async (docId, customerToken) => {
  const response = await fetch(`${baseURL}/api/customer/documents/${docId}/download`, {
    method: 'GET',
    headers: { Authorization: `Bearer ${customerToken}` },
  });
  if (!response.ok) throw new Error('Download failed');
  return response.blob();
};

