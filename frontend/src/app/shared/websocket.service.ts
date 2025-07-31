import { Injectable } from '@angular/core';
import { Client, IMessage, Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {NotificationModel} from '../header/notification-button/notification-sidebar/notification.model';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client;

  constructor() {
    this.client = new Client({
      brokerURL: undefined, // usamos SockJS
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-notifications'),
       reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });
  }

  connect(memberId: number, onMessage: (notification: NotificationModel) => void): void {
    this.client.onConnect = () => {
      this.client.subscribe(`/topic/notifications/${memberId}`, (message: IMessage) => {
        const body = JSON.parse(message.body);
        onMessage(body);
      });
    };
    this.client.activate();
  }

  disconnect(): void {
    this.client.deactivate();
  }
}
