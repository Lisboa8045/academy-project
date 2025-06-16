import { Component } from '@angular/core';

@Component({
  selector: 'app-notification-sidebar',
  imports: [],
  templateUrl: './notification-sidebar.component.html',
  styleUrl: './notification-sidebar.component.css'
})
export class NotificationSidebarComponent {

  getNotifications() {
    return ["1", "2", "3", "4", "5", "6", "7", "8", "9"];
  }

}
