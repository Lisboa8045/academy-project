import {Component, input, output} from '@angular/core';
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
  notifications = input.required<NotificationModel[]>();
  removeNotification = output<number>();

  removeFromList(id: number) {
    this.removeNotification.emit(id);
  }
}
