import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { GlobalConfiguration } from './global-configuration.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class GlobalConfigurationService {
  readonly http = inject(HttpClient);
  readonly apiUrl = 'http://localhost:8080/admin/global_configurations';

  getAll(): Observable<GlobalConfiguration[]> {
    return this.http.get<GlobalConfiguration[]>(this.apiUrl);
  }

  editConfigs(configs: GlobalConfiguration[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/edit`, configs);
  }
}
