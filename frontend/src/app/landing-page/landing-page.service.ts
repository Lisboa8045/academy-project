import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, map, forkJoin} from 'rxjs';
import {AppointmentModel} from '../models/appointment.model';
import {ServiceModel} from '../service/service.model';


interface RatedServiceModel extends ServiceModel {
  averageRating: number;
}

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

  getServiceProviderById(id: number): Observable<any> {
    return this.http.get(`${this.BASE_URL}/service-providers/${id}`);
  }

  getTopRatedServices(): Observable<RatedServiceModel[]> {
    return forkJoin({
      appointments: this.getAppointments(),
      services: this.getServices()
    }).pipe(
      map(({ appointments, services }) => {
        const servicesMap = new Map(services.map((s: ServiceModel) => [s.id, s]));

        const ratings = new Map<number, { total: number; count: number }>();

        for (const appointment of appointments) {
          const rating = appointment.rating;
          const service = servicesMap.get(appointment.serviceProviderId);

          if (rating != null && service?.id != null && servicesMap.has(service.id)) {
            const entry = ratings.get(service.id) ?? { total: 0, count: 0 };
            entry.total += rating;
            entry.count += 1;
            ratings.set(service.id, entry);
          }
        }

        const ratedServices: RatedServiceModel[] = Array.from(ratings.entries())
          .map(([id, { total, count }]) => {
            const avg = total / count;
            return {
              ...servicesMap.get(id)!,
              averageRating: avg
            };
          });

        return ratedServices
          .sort((a, b) => b.averageRating - a.averageRating)
          .slice(0, 10);
      })
    );
  }



}
