import {Component, signal} from '@angular/core';
import {NotificationSidebarComponent} from './notification-sidebar/notification-sidebar.component';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-notification-button',
  imports: [
    NotificationSidebarComponent,
    NgIf
  ],
  templateUrl: './notification-button.component.html',
  styleUrl: './notification-button.component.css'
})
export class NotificationButtonComponent {
  showList = signal(false);

  toggleList() {
    this.showList.set(!this.showList());
  }

  closeList() {
    this.showList.set(false);
  }
}
