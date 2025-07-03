import {Component, inject, input, output} from '@angular/core';
import {NotificationModel} from '../notification.model';
import {NotificationService} from '../../../../shared/notification.service';

@Component({
  selector: 'app-notification-sidebar-item',
  imports: [],
  templateUrl: './notification-sidebar-item.component.html',
  styleUrl: './notification-sidebar-item.component.css'
})
export class NotificationSidebarItemComponent {
  notificationService = inject(NotificationService);
  item = input.required<NotificationModel>();
  removeFromList = output();

  navigateToUrl() {
    window.open(this.item().url);
  }

  markAsRead() {
    this.notificationService.markNotificationAsRead(this.item().id).subscribe({
      next: () => {
        this.removeFromList.emit();
      },
      error: (e) => {
        console.error(e);
      },
    });
  }
}
