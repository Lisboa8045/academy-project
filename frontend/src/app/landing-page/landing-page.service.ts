import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, map, forkJoin} from 'rxjs';
import {AppointmentModel} from '../models/appointment.model';
import {ServiceModel} from '../service/service.model';
import {ServiceProviderModel} from '../models/service-provider.model';

@Injectable({
  providedIn: 'root'
})

export class LandingPageService {

  private BASE_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getServiceTypes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.BASE_URL}/service-types`);
  }

  getServices(): Observable<any[]> {
    return this.http.get<ServiceModel[]>(`${this.BASE_URL}/services`);
  }

  getAppointments(): Observable<any[]> {
    return this.http.get<AppointmentModel[]>(`${this.BASE_URL}/appointments`);
  }

  getTopRatedServices(): Observable<ServiceModel[]> {
    return this.getServices().pipe(
      map((services) =>
        services
          .sort((a, b) => (b.rating ?? 0) - (a.rating ?? 0))
          .slice(0, 10)
      )
    );
  }
}
