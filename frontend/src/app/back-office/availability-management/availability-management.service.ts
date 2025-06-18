import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AvailabilityModel} from '../../models/availability.model';

@Injectable({ providedIn: 'root' })
export class AvailabilityService {

  private BASE_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getAvailabilitiesByWorker(memberId: number): Observable<AvailabilityModel[]> {
    return this.http.get<AvailabilityModel[]>(`${this.BASE_URL}/availabilities/members/${memberId}`);
  }

  // Adicione aqui métodos para adicionar, editar, remover se necessário
  createAvailabilities(memberId: number, availabilities: AvailabilityModel[]) {
    return this.http.post(`/availabilities/member/${memberId}`, availabilities);
  }
}
