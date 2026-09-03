import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { toast } from 'sonner';
import { NotificationResponse } from '@/types';
import { useNotificationStore } from '@/stores/notificationStore';

class WebSocketService {
  private client: Client | null = null;
  private isConnecting: boolean = false;

  public connect(token: string) {
    if (this.client?.active || this.isConnecting) {
      return;
    }

    this.isConnecting = true;

    try {
      const isSecure = typeof window !== 'undefined' && window.location.protocol === 'https:';
      const wsProtocol = isSecure ? 'wss:' : 'ws:';
      const httpProtocol = isSecure ? 'https:' : 'http:';
      const host = typeof window !== 'undefined' ? window.location.host : 'localhost:8080';
      
      const brokerURL = `${wsProtocol}//${host}/ws?token=${token}`;
      const fallbackUrl = `${httpProtocol}//${host}/ws?token=${token}`;

      this.client = new Client({
        brokerURL: brokerURL,
        webSocketFactory: () => new SockJS(fallbackUrl),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
          token: token,
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        onConnect: () => {
          this.isConnecting = false;
          console.log('[WebSocket] STOMP connected successfully');

          // Subscribe to personal user notifications queue
          this.client?.subscribe('/user/queue/notifications', (message) => {
            try {
              const notification: NotificationResponse = JSON.parse(message.body);
              useNotificationStore.getState().addNotification(notification);

              // Toast popup for realtime notification
              toast.info(notification.title, {
                description: notification.message,
                duration: 5000,
              });
            } catch (err) {
              console.error('[WebSocket] Error parsing notification body', err);
            }
          });
        },
        onDisconnect: () => {
          this.isConnecting = false;
          console.log('[WebSocket] STOMP disconnected');
        },
        onStompError: (frame) => {
          this.isConnecting = false;
          console.warn('[WebSocket] STOMP error frame:', frame.headers['message']);
        },
      });

      this.client.activate();
    } catch (error) {
      this.isConnecting = false;
      console.error('[WebSocket] Failed to initialize connection:', error);
    }
  }

  public disconnect() {
    if (this.client) {
      try {
        this.client.deactivate();
      } catch (err) {
        console.warn('[WebSocket] Error during deactivation', err);
      }
      this.client = null;
      this.isConnecting = false;
    }
  }
}

export const wsService = new WebSocketService();
