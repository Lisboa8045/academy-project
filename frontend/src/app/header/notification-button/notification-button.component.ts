import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {NotificationSidebarComponent} from './notification-sidebar/notification-sidebar.component';
import {NgIf} from '@angular/common';
import {NotificationService} from '../../shared/notification.service';
import {AuthStore} from '../../auth/auth.store';
import {NotificationModel} from './notification-sidebar/notification.model';
import {WebSocketService} from '../../shared/websocket.service';

@Component({
  selector: 'app-notification-button',
  imports: [
    NotificationSidebarComponent,
    NgIf
  ],
  templateUrl: './notification-button.component.html',
  styleUrl: './notification-button.component.css'
})
export class NotificationButtonComponent implements OnInit {
  notificationService = inject(NotificationService);
  authStore = inject(AuthStore);
  destroyRef = inject(DestroyRef);
  webSocketService = inject(WebSocketService);
  notifications = signal<NotificationModel[]>([]);
  showList = signal(false);

  ngOnInit(): void {
    this.fetchNotifications();
    this.webSocketService.connect(this.authStore.id(), (newNotification: NotificationModel) => {
      this.notifications.update((prev) => [newNotification, ...prev]);
    });

    this.destroyRef.onDestroy(() => {
      this.webSocketService.disconnect();
    });
  }

  toggleList() {
    this.showList.set(!this.showList());
  }

  closeList() {
    this.showList.set(false);
  }

  fetchNotifications(): void {
    this.notificationService.getNotificationsByMemberId(this.authStore.id()).subscribe({
      next: (res) => {
        this.notifications.set(res);
      },
      error: (e) => {
        console.error(e);
      },
    });
  }

  removeNotification(id: number) {
    this.notifications.set(this.notifications().filter(item => item.id !== id));
  }
}
