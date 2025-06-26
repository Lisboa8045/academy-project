import {Component, effect, OnInit, signal} from '@angular/core';
import {ServiceModel} from './service.model';
import {ServiceApiService, PagedServicesResponse} from '../shared/service-api.service';
import {LoadingComponent} from '../loading/loading.component';
import {DatePipe, NgForOf} from '@angular/common';

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

  constructor(private serviceApi: ServiceApiService) {
    effect(() => {
      this.fetchServices();

    });
  }

  ngOnInit(): void {
    this.fetchServices();
  }

  fetchServices(): void {
    const name = this.searchTerm();
    this.loading.set(true);

    this.serviceApi.searchServices(name).subscribe({
      next: (res: PagedServicesResponse) => {
        this.services.set(res.content);
        this.loading.set(false);
      },
      error: (e) => {
        console.error(e);
        this.loading.set(false);
      },
    });
  }
}
