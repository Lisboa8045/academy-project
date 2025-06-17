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
        // Mapa para relacionar providerId -> serviceId
        const providerToService = new Map(serviceProviders.map(sp => [sp.id, sp.serviceId]));

        // Mapa para serviços por id para lookup rápido
        const servicesMap = new Map(services.map(s => [s.id, s]));

        // Acumula total de ratings e contagem por serviceId
        const ratings = appointments.reduce((acc, appointment) => {
          const serviceId = providerToService.get(appointment.serviceProviderId);
          if (serviceId != null && appointment.rating != null && servicesMap.has(serviceId)) {
            const current = acc.get(serviceId) ?? { total: 0, count: 0 };
            current.total += appointment.rating;
            current.count += 1;
            acc.set(serviceId, current);
          }
          return acc;
        }, new Map<number, { total: number; count: number }>());

        // Calcula média e cria array de serviços com rating
        const ratedServices: RatedServiceModel[] = Array.from(ratings.entries() as Iterable<[number, { total: number; count: number }]>).map(
          ([id, { total, count }]) => ({
            ...servicesMap.get(id)!,
            averageRating: total / count,
          })
        );

        // Ordena por rating decrescente e pega os top 10
        return ratedServices.sort((a, b) => b.averageRating - a.averageRating).slice(0, 10);
      })
    );
  }

}
