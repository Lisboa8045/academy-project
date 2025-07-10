import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ServiceModel} from './service.model';


@Injectable({
  providedIn:'root'
})
export class ServiceDetailsService {
  private baseUrl = 'http://localhost:8080/services';

  constructor(private http: HttpClient) {
  }

  getServiceById(id:number): Observable<ServiceModel>{
    return this.http.get<ServiceModel>(`${this.baseUrl}/${id}`);
  }
}
