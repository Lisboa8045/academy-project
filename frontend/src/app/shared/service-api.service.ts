// src/app/services/service-api.service.ts
import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ServiceModel} from '../service/service.model';
import {ServiceQuery} from './models/service-query.model';
import {ServiceTypeResponseDTO} from "./models/service-type.model";
import {ServiceAppointmentReviewModel} from '../service/service-details/service-appointment-review-model';

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
  private UPLOADS_URL = 'http://localhost:8080/auth/uploads/service-image';
  private REVIEWS_URL = 'http://localhost:8080/services/service_with_review';

  constructor(private http: HttpClient) {
  }

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

  createService(payload: ServiceModel): Observable<ServiceModel> {
    return this.http.post<ServiceModel>(this.BASE_URL, payload);
  }

  editService(payload: ServiceModel, id: number): Observable<ServiceModel> {
    return this.http.put<ServiceModel>(`${this.BASE_URL}/${id}`, payload);
  }

  uploadServiceImages(formData: FormData, id : number) {
    return this.http.post<{imageUrl: string}>(
      `${this.UPLOADS_URL}?id=${id}`,
      formData,
    );
  }

  getServiceById(id: number): Observable<ServiceModel> {
    return this.http.get<ServiceModel>(`${this.BASE_URL}/${id}`);
  }

  getReviewsByServiceId(id: number): Observable<ServiceAppointmentReviewModel[]> {
    return this.http.get<ServiceAppointmentReviewModel[]>(`${this.REVIEWS_URL}/${id}`);
  }

  getServicesOfMember(query: ServiceQuery, id: number): Observable<PagedResponse>{
    let params = new HttpParams()
      .set('page', query.page.toString())
      .set('size', query.pageSize.toString())
      .set('sort', query.sortOrder);
    return this.http.get<PagedResponse>(this.BASE_URL + '/my-services/'+id, {params});
  }
}
