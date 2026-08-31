import axiosClient from './axiosClient';
import type { TravelContextRequest, TripPlanResponse } from './types';

export const recommendationApi = {
  recommend: (data: TravelContextRequest) => axiosClient.post<{data: TripPlanResponse[]}>('/planning/recommend', data),
  getCities: () => axiosClient.get<{data: string[]}>('/destinations/cities'),
};
