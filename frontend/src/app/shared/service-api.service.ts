// src/app/services/service-api.service.ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {ServiceModel} from '../service/service.model';

export interface PagedServicesResponse {
  content: ServiceModel[];
  totalElements: number;
}

@Injectable({
  providedIn: 'root',
})
export class ServiceApiService {
  private SEARCH_URL = 'http://localhost:8080/auth/services/search';
  private BASE_URL = 'http://localhost:8080/my-services';

  constructor(private http: HttpClient) {}

  searchServices(
    name = '',
    page = 0,
    size = 10,
    sort = 'price,asc',
    tags: string[] = [],
    priceMin?: number,
    priceMax?: number
  ): Observable<PagedServicesResponse> {
    let params = new HttpParams()
      .set('name', name)
      .set('page', page)
      .set('size', size)
      .set('sort', sort);

    if (tags.length) {
      tags.forEach(tag => {
        params = params.append('tags', tag);
      });
    }

    if (priceMin !== undefined) params = params.set('priceMin', priceMin);
    if (priceMax !== undefined) params = params.set('priceMax', priceMax);

    return this.http.get<PagedServicesResponse>(this.SEARCH_URL, { params });
  }

  getServicesByMemberID(id: number){
    return this.http.get<PagedServicesResponse>(`${this.BASE_URL}/${id}`);
  }
}
