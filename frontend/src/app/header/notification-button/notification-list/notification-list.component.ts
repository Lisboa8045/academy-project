import { Component } from '@angular/core';

@Component({
  selector: 'app-notification-list',
  imports: [],
  templateUrl: './notification-list.component.html',
  styleUrl: './notification-list.component.css'
})
export class NotificationListComponent {

  getNotifications() {
    return ["1", "2", "3", "4", "5", "6", "7", "8", "9"];
  }

}
