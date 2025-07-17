// src/app/services/appointment-api.service.ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SlotModel } from '../models/slot.model';
import { AppointmentModel } from '../models/appointment.model';
import { environment } from '../../enviroments/environments';

@Injectable({
  providedIn: 'root',
})
export class ScheduleApiService {

  private readonly BASE_URL = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  getFreeSlots(serviceId: number): Observable<SlotModel[]> {
    return this.http.get<SlotModel[]>(`${this.BASE_URL}/appointments/services/${serviceId}/free-slots`);
  }

  getServiceProvider(serviceId: number, providerId: number): Observable<any> {
    return this.http.get(`${this.BASE_URL}/service-providers/services/${serviceId}/providers/${providerId}`);
  }

  confirmAppointment(appointment: AppointmentModel): Observable<any> {
    return this.http.post(`${this.BASE_URL}/appointments`, appointment);
  }
}
