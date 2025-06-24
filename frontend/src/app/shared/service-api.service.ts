import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ServiceModel} from '../service/service.model';
import {ServiceQuery} from './models/service-query.model';
import {ServiceTypeResponseDTO} from "./models/service-type.model";

export interface PagedResponse {
  content: ServiceModel[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root',
})
export class ServiceApiService {
  private BASE_URL = 'http://localhost:8080/services';
  private SERVICE_TYPE_URL = 'http://localhost:8080/service-types';

  constructor(private http: HttpClient) {}

  searchServices(name: string, options: ServiceQuery): Observable<PagedResponse> {
    let params = new HttpParams()
      .set('name', name)
      .set('page', options.page.toString())
      .set('size', options.pageSize.toString())
      .set('sort', options.sortOrder);

    if (options.minPrice != null) params = params.set('minPrice', options.minPrice.toString());
    if (options.maxPrice != null) params = params.set('maxPrice', options.maxPrice.toString());
    if (options.minDuration != null) params = params.set('minDuration', options.minDuration.toString());
    if (options.maxDuration != null) params = params.set('maxDuration', options.maxDuration.toString());
    if (options.negotiable != null) params = params.set('negotiable', options.negotiable.toString());
    if (options.serviceTypeName != null) params = params.set('serviceTypeName', options.serviceTypeName.toString());

    console.log('Search params:', params.toString());

    return this.http.get<PagedResponse>(this.BASE_URL + '/search', {params});
  }

  getServiceTypes(): Observable<ServiceTypeResponseDTO[]> {
    return this.http.get<ServiceTypeResponseDTO[]>(this.SERVICE_TYPE_URL);
  }
}
