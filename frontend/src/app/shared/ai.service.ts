import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

export interface ServiceModelBasicInfo {
  name: string;
  description: string;
  tags: string[];
}

@Injectable({ providedIn: 'root' })
export class AiService {

  constructor(private http: HttpClient) {
  }

  generateServiceImage(service: ServiceModelBasicInfo) {
    const webhookUrl = 'http://localhost:5678/webhook/generate-service-image';
    return this.http.post(webhookUrl, service, { responseType: 'blob' });
  }

  generateServiceTags(service: ServiceModelBasicInfo) {
    const webhookUrl = 'http://localhost:5678/webhook/generate-service-tags';
    return this.http.post<string[]>(webhookUrl, service);
  }
}
