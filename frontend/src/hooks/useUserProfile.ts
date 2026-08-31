import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi } from '../api';

// --- QUERIES ---

export function useProfile() {
  return useQuery({
    queryKey: ['profile'],
    queryFn: async () => {
      const { data } = await userApi.getProfile();
      return data.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

export function useFriends() {
  return useQuery({
    queryKey: ['friends'],
    queryFn: async () => {
      const { data } = await userApi.getFriends();
      return data.data;
    },
    staleTime: 5 * 60 * 1000,
  });
}

export function useIncomingRequests() {
  return useQuery({
    queryKey: ['friend-requests', 'incoming'],
    queryFn: async () => {
      const { data } = await userApi.getIncomingRequests();
      return data.data;
    },
    staleTime: 30 * 1000, // 30 seconds
  });
}

export function useOutgoingRequests() {
  return useQuery({
    queryKey: ['friend-requests', 'outgoing'],
    queryFn: async () => {
      const { data } = await userApi.getOutgoingRequests();
      return data.data;
    },
    staleTime: 30 * 1000,
  });
}

export function useSearchUsers(query: string) {
  return useQuery({
    queryKey: ['search-users', query],
    queryFn: async () => {
      if (!query) return [];
      const { data } = await userApi.searchUsers(query);
      return data.data;
    },
    enabled: !!query,
    staleTime: 30 * 1000,
  });
}

// --- MUTATIONS ---

export function useUpdateProfile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (profileData: any) => {
      const { data } = await userApi.updateProfile(profileData);
      return data.data;
    },
    onSuccess: (updatedProfile) => {
      queryClient.setQueryData(['profile'], updatedProfile);
    },
  });
}

export function useSendFriendRequest() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (email: string) => {
      await userApi.sendFriendRequest(email);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['friend-requests', 'outgoing'] });
      queryClient.invalidateQueries({ queryKey: ['search-users'] });
    },
  });
}

export function useRespondFriendRequest() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ requestId, accept }: { requestId: string; accept: boolean }) => {
      await userApi.respondFriendRequest(requestId, accept);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['friend-requests', 'incoming'] });
      queryClient.invalidateQueries({ queryKey: ['friends'] });
      queryClient.invalidateQueries({ queryKey: ['search-users'] });
    },
  });
}

export function useRemoveFriend() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (email: string) => {
      await userApi.removeFriend(email);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['friends'] });
    },
  });
}
