import {inject, Injectable} from '@angular/core';
import {environment} from '../../../enviroments/environments';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ServiceProviderModel, ServiceProviderRequestDTO} from '../../models/service-provider.model';
import {UpdatePermissionsRequest} from './manage-workers/manage-workers.model';

@Injectable({
  providedIn: 'root',
})
export class EditServiceService {
  private SERVICE_PROVIDER_URL = `${environment.apiBaseUrl}/service-providers`;
  private SERVICE_URL = `${environment.apiBaseUrl}/services`;
  httpClient = inject(HttpClient);

  getServiceProvidersByServiceId(serviceId:number): Observable<ServiceProviderModel[]>{
    return this.httpClient.get<ServiceProviderModel[]>(`${this.SERVICE_PROVIDER_URL}/services/${serviceId}`);
  }

  deleteServiceProvider(providerId: number) {
    return this.httpClient.delete(`${this.SERVICE_PROVIDER_URL}/${providerId}`);
  }

  setServiceProviderPermissions(serviceId: number, updatePermissionsRequest: UpdatePermissionsRequest) {
    return this.httpClient.patch(`${this.SERVICE_URL}/${serviceId}`, updatePermissionsRequest);
  }

  createServiceProvider(request: ServiceProviderRequestDTO) {
    return this.httpClient.post<ServiceProviderModel>(`${this.SERVICE_PROVIDER_URL}`, request);
  }
}
