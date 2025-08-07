import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {AppointmentCalendarDTO, AvailabilityDTO} from './availability.models';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AvailabilityService {
  private availabilityUrl = 'http://localhost:8080/availabilities';
  private appointmentsUrl = 'http://localhost:8080/appointments';

  constructor(private http: HttpClient) {}

  getAvailabilities(): Observable<AvailabilityDTO> {
    return this.http.get<AvailabilityDTO>(this.availabilityUrl + "/member");
  }

  getAppointments(): Observable<AppointmentCalendarDTO[]> {
    return this.http.get<AppointmentCalendarDTO[]>(this.appointmentsUrl + "/calendar");
  }

  saveAvailabilities(dto: AvailabilityDTO): Observable<any> {
    return this.http.post(this.availabilityUrl + "/create-availabilities", dto);
  }
}
