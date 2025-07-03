import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NotificationModel} from '../header/notification-button/notification-sidebar/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/notifications';

  getNotificationsByMemberId(memberId: number) {
    return this.http.get<NotificationModel[]>(
      `${this.apiUrl}/${memberId}`
    );
  }

  markNotificationAsRead(id: number) {
    return this.http.patch<NotificationModel[]>(
      `${this.apiUrl}/${id}`,
      {
        seen: true,
      }
    );
  }
}
