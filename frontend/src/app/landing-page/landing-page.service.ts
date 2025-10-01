import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {AppointmentModel} from '../models/appointment.model';
import {ServiceModel} from '../service/service.model';

@Injectable({
  providedIn: 'root'
})

export class LandingPageService {

  private BASE_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getServiceTypes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.BASE_URL}/service-types`);
  }

  getTopRatedServices(): Observable<ServiceModel[]> {
    return this.http.get<ServiceModel[]>(`${this.BASE_URL}/services/top-rated`);
  }

  getDiscountedServices(): Observable<ServiceModel[]> {
    return this.http.get<ServiceModel[]>(`${this.BASE_URL}/services/discounted`);
  }

  getTrendingServices(): Observable<ServiceModel[]> {
    return this.http.get<ServiceModel[]>(`${this.BASE_URL}/services/trending`);
  }
}
