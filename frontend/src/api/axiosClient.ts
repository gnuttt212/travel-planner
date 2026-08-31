import axios from 'axios';

export function getAuthTokenQueryParam() {
  const token = localStorage.getItem('token');
  return token ? `?token=${encodeURIComponent(token)}` : '';
}

const axiosClient = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
});

// Add a request interceptor to attach the JWT token
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 Unauthorized errors
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
