import React, { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'sonner';
import { router } from './router';
import { useAuthStore } from './stores/authStore';
import { wsService } from './lib/websocket';
import { authApi } from './api/auth.api';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

export const App: React.FC = () => {
  const accessToken = useAuthStore((state) => state.accessToken);
  const setUser = useAuthStore((state) => state.setUser);

  // Re-establish WebSocket connection & refresh profile on page load/refresh
  useEffect(() => {
    if (accessToken) {
      wsService.connect(accessToken);
      authApi.getCurrentUser().then(setUser).catch(console.warn);
    }
    return () => {
      wsService.disconnect();
    };
  }, [accessToken, setUser]);

  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <Toaster position="top-right" richColors closeButton />
    </QueryClientProvider>
  );
};

export default App;
