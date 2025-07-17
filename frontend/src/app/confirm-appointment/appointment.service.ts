import {Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private readonly apiUrl = 'http://localhost:8080/appointments';

  constructor(private readonly http: HttpClient) {}

  confirmAppointment(id: number) {
    return this.http.post<{ response: string }>(
      `${this.apiUrl}/confirm-appointment/${id}`,
      {}
    );
  }
}


