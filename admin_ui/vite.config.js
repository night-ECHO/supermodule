import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Basic Vite config for React + JSX
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Forward all /api calls in dev to backend on 8081 to avoid CORS
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
});
