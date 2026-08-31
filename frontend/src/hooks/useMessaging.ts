import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { messagingApi } from '../api';

export function useConversation(email: string | null) {
  return useQuery({
    queryKey: ['conversation', email],
    queryFn: async () => {
      if (!email) return [];
      const { data } = await messagingApi.getConversation(email);
      return data.data;
    },
    enabled: !!email,
    staleTime: 5 * 1000, // 5 seconds for messaging
  });
}

export function useSendMessage() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ to, content }: { to: string; content: string }) => {
      const { data } = await messagingApi.sendMessage(to, content);
      return data.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['conversation', variables.to] });
    },
  });
}
