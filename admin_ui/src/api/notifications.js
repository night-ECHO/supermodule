import api from './api';

export const fetchNotifications = async (page = 0, size = 50) => {
  const res = await api.get('/api/admin/notifications', { params: { page, size } });
  return res.data;
};

export const retryNotification = async (id, note) => {
  const res = await api.post(`/api/admin/notifications/${id}/retry`, { note });
  return res.data;
};
