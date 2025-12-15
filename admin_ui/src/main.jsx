import React from 'react';
import ReactDOM from 'react-dom/client';
import 'antd/dist/reset.css';
import './index.css'; // Import file CSS reset bên dưới
import App from './App.jsx';

// Tìm thẻ div có id="root" trong index.html và render App vào đó
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
