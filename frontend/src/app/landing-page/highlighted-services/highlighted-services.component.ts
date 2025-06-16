import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-highlighted-services',
  imports: [],
  templateUrl: './highlighted-services.component.html',
  styleUrl: './highlighted-services.component.css'
})
export class HighlightedServicesComponent implements OnInit {
  services: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any[]>('/api/appointments').subscribe(appointments => {
      const serviceRatingsMap = new Map<number, { total: number, count: number, service: any }>();

      for (const appointment of appointments) {
        const rating = appointment.rating;
        const service = appointment.serviceProvider?.service;

        if (rating && service?.id) {
          const entry = serviceRatingsMap.get(service.id) || { total: 0, count: 0, service };
          entry.total += rating;
          entry.count += 1;
          serviceRatingsMap.set(service.id, entry);
        }
      }

      const avgRatings = Array.from(serviceRatingsMap.values())
        .map(entry => ({
          ...entry.service,
          averageRating: entry.total / entry.count
        }))
        .sort((a, b) => b.averageRating - a.averageRating)
        .slice(0, 10);

      this.services = avgRatings;
    });
  }
}
