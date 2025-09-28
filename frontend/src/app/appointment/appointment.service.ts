import {Injectable, WritableSignal} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {MemberResponseDTO} from '../auth/member-response-dto.model';
import {Observable} from 'rxjs';
import {AppointmentResponseDetailedDTO, AppointmentResponseDTO} from './appointment-response-dto.model';
import {AppointmentQuery} from './appointment-query-service.model';
import {Page} from './page.model';
import {Review} from './review/review.model';
export interface ConfirmAppointmentResponseDTO {
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private apiUrl = 'http://localhost:8080/appointments';

  constructor(private http: HttpClient) {}

  getUserAppointments(dateOrder: string){
    return this.http
      .get<AppointmentResponseDTO[]>(
      `${this.apiUrl}/member`
        ,{
          params: {
            dateOrder: dateOrder,
          }
        }
    );
  }

  getServiceAppointments(serviceId: number, dateOrder: string){
    return this.http
      .get<AppointmentResponseDTO[]>(
      `${this.apiUrl}/service/${serviceId}`
        ,{
          params: {
            dateOrder: dateOrder,
          }
        }
    );
  }

  getAppointmentById(id: number): Observable<AppointmentResponseDetailedDTO> {
    return this.http.get<AppointmentResponseDetailedDTO>(
      `${this.apiUrl}/${id}`
    );
  }

  cancelAppointment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  finishAppointment(id: number) {
    return this.http.post<ConfirmAppointmentResponseDTO>(`${this.apiUrl}/finish-appointment/${id}`, null);
  }

  sendReview(id: number, review: Review): Observable<{ message: string }> {
    return this.http.patch<{ message: string }>(`${this.apiUrl}/${id}/review`, review);
  }

}


