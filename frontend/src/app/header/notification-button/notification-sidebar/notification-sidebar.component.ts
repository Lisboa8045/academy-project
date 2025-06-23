import {Component, inject, input, OnInit, signal} from '@angular/core';
import {NotificationModel} from './notification.model';
import {NotificationSidebarItemComponent} from './item/notification-sidebar-item.component';
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
export class NotificationSidebarComponent {
  notificationService = inject(NotificationService);
  authStore = inject(AuthStore);
  notifications = input.required<NotificationModel[]>();
}
