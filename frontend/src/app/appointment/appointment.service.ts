import {Injectable, WritableSignal} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {MemberResponseDTO} from '../auth/member-response-dto.model';
import {Observable} from 'rxjs';
import {AppointmentResponseDetailedDTO, AppointmentResponseDTO} from './appointment-response-dto.model';
import {AppointmentQuery} from './appointment-query-service.model';
import {Page} from './page.model';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private apiUrl = 'http://localhost:8080/appointments';

  constructor(private http: HttpClient) {}

  getUserAppointments(query : AppointmentQuery){
    return this.http
      .get<Page<AppointmentResponseDTO>>(
      `${this.apiUrl}/member`
      ,{
        params:{
          page: query.page,
          size:query.pageSize,
          dateOrder: query.dateOrder,
        }
      },
    );
  }

  getAppointmentById(id: number): Observable<AppointmentResponseDetailedDTO> {
    return this.http.get<AppointmentResponseDetailedDTO>(
      `${this.apiUrl}/${id}`
    );
  }

}


