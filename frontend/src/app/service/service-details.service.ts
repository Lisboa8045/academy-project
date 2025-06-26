import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ServiceModel} from './service.model';
import {ServiceAppointmentReviewModel} from './service-details/service-appointment-review-model';


@Injectable({
  providedIn:'root'
})
export class ServiceDetailsService {
  private baseUrl = 'http://localhost:8080/services';
  private urlReviews = 'http://localhost:8080/services/service_with_review';


  constructor(private http: HttpClient) {
  }

  getServiceById(id:number): Observable<ServiceModel>{
    return this.http.get<ServiceModel>(`${this.baseUrl}/${id}`);
  }

  getReviewsByServiceId(id: number): Observable<ServiceAppointmentReviewModel[]> {
    return this.http.get<ServiceAppointmentReviewModel[]>(`${this.urlReviews}/${id}`);
  }
}
