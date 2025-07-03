import {inject, Injectable} from '@angular/core';
import {environment} from '../../../enviroments/environments';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ServiceProviderModel} from '../../models/service-provider.model';

@Injectable({
  providedIn: 'root',
})
export class EditServiceService {
  private SERVICE_PROVIDER_URL = `${environment.apiBaseUrl}/service-providers`;
  httpClient = inject(HttpClient);

  getServiceProvidersByServiceId(serviceId:number): Observable<ServiceProviderModel[]>{
    return this.httpClient.get<ServiceProviderModel[]>(`${this.SERVICE_PROVIDER_URL}/services/${serviceId}`);
  }
}
