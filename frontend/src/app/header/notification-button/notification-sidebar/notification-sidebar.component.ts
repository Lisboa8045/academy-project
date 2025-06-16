import { Component } from '@angular/core';
import {NotificationModel} from './notification.model';
import {NotificationSidebarItemComponent} from './item/notification-sidebar-item.component';

@Component({
  selector: 'app-notification-sidebar',
  imports: [
    NotificationSidebarItemComponent
  ],
  templateUrl: './notification-sidebar.component.html',
  styleUrl: './notification-sidebar.component.css'
})
export class NotificationSidebarComponent {

  getNotifications(): NotificationModel[] {
    return [
      {
        id: 0,
        title: 'Service Created',
        body: 'Service has been successfully created.',
        url: 'http://www.google.com',
        seen: false,
        notificationType: "MESSAGE"

      },
      {
        id: 1,
        title: 'Member Created',
        body: 'Member has been successfully created.',
        url: 'http://www.google.com',
        seen: false,
        notificationType: "MESSAGE"

      },
    ];
  }

}
