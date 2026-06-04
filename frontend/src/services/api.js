import axios from 'axios';

const api = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor to dynamically fetch latest local storage tokens
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to catch unauthorized requests
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      logOutUserSession();
    }
    return Promise.reject(error);
  }
);

const logOutUserSession = () => {
  localStorage.removeItem('auth_token');
  localStorage.removeItem('auth_refresh');
  localStorage.removeItem('auth_user');
  window.location.href = '/login';
};

export default api;
