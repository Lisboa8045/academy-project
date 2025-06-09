// src/app/services/appointment-api.service.ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SlotModel } from './slot.model';
import { AppointmentModel } from './appointment.model';


@Injectable({
  providedIn: 'root',
})
export class ScheduleApiService {
  private BASE_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getFreeSlots(serviceId: number): Observable<SlotModel[]> {
    return this.http.get<SlotModel[]>(`${this.BASE_URL}/appointments/services/${serviceId}/free-slots`);
  }
}
