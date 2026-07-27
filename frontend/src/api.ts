import axios from 'axios';

export interface TravelContextRequest {
  purpose: string;
  tripDate: string;
  duration: string;
  groupType: string;
  groupSize: number;
  budgetPerPerson: number;
  styles: string[];
  startLat: number;
  startLon: number;
  transportation: string;
  city: string;
}

export interface TripActivity {
  id: string;
  destinationId: string;
  orderIndex: number;
  plannedStartTime: string;
  plannedEndTime: string;
  estimatedDurationMinutes: number;
  estimatedCost: number;
  destinationName: string;
  destinationCategory: string;
  destinationLat: number;
  destinationLon: number;
  destinationRating: number;
  destinationImageUrl: string;
  travelDistanceKm: number;
  travelTimeMinutes: number;
  status: string;
}

export interface TripPlanResponse {
  variantName: string;
  variantDescription: string;
  aiNarrative?: string;
  activities: TripActivity[];
  totalCost: number;
  totalDistanceKm: number;
}

export interface TripResponse {
  id: string;
  title: string;
  status: string;
  tripDate: string;
  purpose: string;
  groupSize: number;
  activities: TripActivity[];
  createdAt: string;
}

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
});

// Add a request interceptor to attach the JWT token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 Unauthorized errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const planningApi = {
  recommend: (data: TravelContextRequest) => api.post<{data: TripPlanResponse[]}>('/planning/recommend', data),
  createTrip: (data: TravelContextRequest) => api.post<{data: TripResponse}>('/trips', data),
  getMyTrips: () => api.get<{data: TripResponse[]}>('/trips'),
  getTrip: (id: string) => api.get<{data: TripResponse}>(`/trips/${id}`),
  deleteTrip: (id: string) => api.delete(`/trips/${id}`),
  exportPdf: (id: string) => api.get(`/trips/${id}/export/pdf`, { responseType: 'blob' }),
};

export default api;
