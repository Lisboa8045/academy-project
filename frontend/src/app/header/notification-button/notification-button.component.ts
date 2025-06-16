import {Component, signal} from '@angular/core';
import {NotificationListComponent} from './notification-list/notification-list.component';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-notification-button',
  imports: [
    NotificationListComponent,
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
