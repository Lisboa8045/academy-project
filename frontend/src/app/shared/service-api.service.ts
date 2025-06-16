// src/app/services/service-api.service.ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {ServiceModel} from '../service/service.model';

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
  private BASE_URL = 'http://localhost:8080/services/search';

  constructor(private http: HttpClient) {}

  searchServices(
    name: string,
    page: number,
    size: number,
    sort: string,
    priceMin?: number,
    priceMax?: number
  ): Observable<PagedResponse> {
    let params = new HttpParams()
      .set('name', name)
      .set('page', page)
      .set('size', size)
      .set('sort', sort);

    if (priceMin !== undefined) params = params.set('priceMin', priceMin);
    if (priceMax !== undefined) params = params.set('priceMax', priceMax);

    return this.http.get<PagedResponse>(this.BASE_URL, { params });
  }
}
