import {Component, effect, OnInit, signal} from '@angular/core';
import {ServiceModel} from './service.model';
import {ServiceApiService, PagedResponse} from '../shared/service-api.service';
import {LoadingComponent} from '../loading/loading.component';
import {DatePipe, NgForOf} from '@angular/common';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-service-list',
  templateUrl: './service-list.component.html',
  styleUrls: ['./service-list.component.css'],
  imports: [LoadingComponent, NgForOf, DatePipe]
})
export class ServiceListComponent implements OnInit{
  services = signal<ServiceModel[]>([]);
  searchTerm = signal('');
  loading = signal(false);

  constructor(private serviceApi: ServiceApiService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const q = (params['q'] || '').trim();
      this.searchTerm.set(q);
      this.fetchServices();
    });
  }

  fetchServices(): void {
    const query = this.searchTerm().trim();
    this.loading.set(true);

    this.serviceApi.searchServices(query).subscribe({
      next: (res: PagedResponse) => {
        this.services.set(res.content);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to fetch services', err);
        this.services.set([]);
        this.loading.set(false);
      },
    });
  }
}
