import {Component, input, output} from '@angular/core';
import {NotificationModel} from './notification.model';
import {NotificationSidebarItemComponent} from './item/notification-sidebar-item.component';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-notification-sidebar',
  imports: [
    NotificationSidebarItemComponent,
    NgIf
  ],
  templateUrl: './notification-sidebar.component.html',
  styleUrl: './notification-sidebar.component.css'
})
export class NotificationSidebarComponent {
  notifications = input.required<NotificationModel[]>();
  removeNotification = output<number>();
  closeSidebar = output();

  removeFromList(id: number) {
    this.removeNotification.emit(id);
  }

  goToNotificationHub() {
    console.log('Go to NotificationHub::To be implemented 😔');
    this.closeSidebar.emit();
  }
}
