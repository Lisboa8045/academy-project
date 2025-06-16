import {Component, input, Input} from '@angular/core';
import {NotificationModel} from '../notification.model';

@Component({
  selector: 'app-notification-sidebar-item',
  imports: [],
  templateUrl: './notification-sidebar-item.component.html',
  styleUrl: './notification-sidebar-item.component.css'
})
export class NotificationSidebarItemComponent {
  item = input.required<NotificationModel>();

}
