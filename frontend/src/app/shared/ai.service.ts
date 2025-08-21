import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ServiceModel} from '../service/service.model';

@Injectable({ providedIn: 'root' })
export class AiService {

  constructor(private http: HttpClient) {
  }

  generateServiceImage(service: ServiceModel) {
    const webhookUrl = 'http://localhost:5678/webhook-test/generate-service-image';
    return this.http.post(webhookUrl, service, { responseType: 'blob' });
  }
}
