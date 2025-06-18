import {Component, inject, OnInit, signal} from '@angular/core';
import {NotificationModel} from './notification.model';
import {NotificationSidebarItemComponent} from './item/notification-sidebar-item.component';
import {PagedResponse} from '../../../shared/service-api.service';
import {NotificationService} from '../../../shared/notification.service';
import {AuthStore} from '../../../auth/auth.store';

@Component({
  selector: 'app-notification-sidebar',
  imports: [
    NotificationSidebarItemComponent
  ],
  templateUrl: './notification-sidebar.component.html',
  styleUrl: './notification-sidebar.component.css'
})
export class NotificationSidebarComponent implements OnInit {
  notificationService = inject(NotificationService);
  authStore = inject(AuthStore);
  notifications = signal<NotificationModel[]>([]);

  ngOnInit(): void {
    this.fetchNotifications();
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

}
