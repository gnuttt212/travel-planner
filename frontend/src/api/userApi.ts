import axiosClient from './axiosClient';
import type { ProfileResponse, UserSearchResult, FriendRequestDto } from './types';

export const userApi = {
  // Auth
  register: (data: any) => axiosClient.post('/auth/register', data),
  login: (data: any) => axiosClient.post('/auth/login', data),

  // Profile
  getProfile: () => axiosClient.get<{ data: ProfileResponse }>('/profile'),
  updateProfile: (data: any) => axiosClient.put<{ data: ProfileResponse }>('/profile', data),
  
  // Friends
  getFriends: () => axiosClient.get<{ data: UserSearchResult[] }>('/profile/friends'),
  removeFriend: (email: string) => axiosClient.delete(`/profile/friends/${encodeURIComponent(email)}`),
  
  // Friend Requests
  getIncomingRequests: () => axiosClient.get<{ data: FriendRequestDto[] }>('/profile/friend-requests/incoming'),
  getOutgoingRequests: () => axiosClient.get<{ data: FriendRequestDto[] }>('/profile/friend-requests/outgoing'),
  sendFriendRequest: (email: string) => axiosClient.post('/profile/friend-requests', { email }),
  respondFriendRequest: (requestId: string, accept: boolean) => 
    axiosClient.patch(`/profile/friend-requests/${requestId}/${accept ? 'accept' : 'reject'}`),
  
  // Search
  searchUsers: (query: string) => axiosClient.get<{ data: UserSearchResult[] }>('/profile/search', { params: { query } }),

  // Onboarding
  submitOnboarding: (data: any) => axiosClient.post('/onboarding', data),
};
