import axiosClient, { getAuthTokenQueryParam } from './axiosClient';
import { userApi } from './userApi';
import { tripApi } from './tripApi';
import { recommendationApi } from './recommendationApi';
import { messagingApi } from './messagingApi';

// Re-export all types
export * from './types';

// For backward compatibility, combine tripApi and recommendationApi into planningApi
export const planningApi = {
  ...tripApi,
  ...recommendationApi,
};

export {
  axiosClient as default,
  getAuthTokenQueryParam,
  userApi,
  tripApi,
  recommendationApi,
  messagingApi,
};
