import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, map, forkJoin} from 'rxjs';
import {AppointmentModel} from '../models/appointment.model';
import {ServiceModel} from '../service/service.model';
import {ServiceProviderModel} from '../models/service-provider.model';


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

  getTopRatedServices(): Observable<RatedServiceModel[]> {
    return forkJoin({
      appointments: this.getAppointments(),
      services: this.getServices(),
      serviceProviders: this.http.get<ServiceProviderModel[]>(`${this.BASE_URL}/service-providers`),
    }).pipe(
      map(({ appointments, services, serviceProviders }) => {

        const providerToService = new Map<number, number>();
        for (const sp of serviceProviders) {
          providerToService.set(sp.id, sp.serviceId);
        }

        const servicesMap = new Map(services.map((s: ServiceModel) => [s.id, s]));

        const ratings = new Map<number, { total: number; count: number }>();

        for (const appointment of appointments) {
          const serviceProviderId = appointment.serviceProviderId;
          const serviceId = providerToService.get(serviceProviderId);
          const rating = appointment.rating;
          if (
            serviceId != null &&
            rating != null &&
            servicesMap.has(serviceId)
          ) {
            const entry = ratings.get(serviceId) ?? { total: 0, count: 0 };
            entry.total += rating;
            entry.count += 1;
            ratings.set(serviceId, entry);
          }
        }

        const ratedServices: RatedServiceModel[] = Array.from(ratings.entries())
          .map(([id, { total, count }]) => {
            const avg = total / count;
            return {
              ...servicesMap.get(id)!,
              averageRating: avg,
            };
          });

        return ratedServices
          .sort((a, b) => b.averageRating - a.averageRating)
          .slice(0, 10);
      })
    );
  }
}
