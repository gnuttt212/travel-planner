import axiosClient from './axiosClient';

export const messagingApi = {
  sendMessage: (to: string, content: string) => axiosClient.post<{data: any}>('/messages', { to, content }),
  getConversation: (otherEmail: string) => axiosClient.get<{data: any}>(`/messages/with/${encodeURIComponent(otherEmail)}`),
  addComment: (tripId: string, content: string) => axiosClient.post<{data: any}>('/comments', { tripId, content }),
  listComments: (tripId: string) => axiosClient.get<{data: any}>(`/comments/trip/${encodeURIComponent(tripId)}`),
  react: (targetType: string, targetId: string, type: string) => axiosClient.post<{data: any}>('/reactions', { targetType, targetId, type }),
  removeReaction: (targetType: string, targetId: string) => axiosClient.delete(`/reactions?targetType=${encodeURIComponent(targetType)}&targetId=${encodeURIComponent(targetId)}`),
  listReactions: (targetType: string, targetId: string) => axiosClient.get<{data: any}>(`/reactions?targetType=${encodeURIComponent(targetType)}&targetId=${encodeURIComponent(targetId)}`),
};
