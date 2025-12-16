import { useAuth } from '../context/AuthContext';

export function useCurrentUser() {
  const { token } = useAuth();
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return {
      username: payload.sub,
      role: payload.role || 'ASSOCIATE',
    };
  } catch {
    return null;
  }
}