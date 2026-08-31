import axiosClient from './axiosClient';
import type { TravelContextRequest, TripResponse } from './types';

export const tripApi = {
  createTrip: (data: TravelContextRequest) => axiosClient.post<{data: TripResponse}>('/trips', data),
  getMyTrips: () => axiosClient.get<{data: TripResponse[]}>('/trips'),
  getTrip: (id: string) => axiosClient.get<{data: TripResponse}>(`/trips/${id}`),
  deleteTrip: (id: string) => axiosClient.delete(`/trips/${id}`),
  exportPdf: (id: string) => axiosClient.get(`/trips/${id}/export/pdf`, { responseType: 'blob' }),
  optimizeRoute: (payload: any) => axiosClient.post('/routes/optimize', payload),
};
