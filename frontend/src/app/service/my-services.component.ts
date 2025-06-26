import {Component, OnInit, signal} from '@angular/core';
import {PagedResponse, ServiceApiService} from '../shared/service-api.service';
import {ServiceModel} from './service.model';
import {ControlsBarComponent} from './search/controls-bar/controls-bar.component';
import {LoadingComponent} from '../loading/loading.component';
import {PaginationBarComponent} from './search/pagination-bar/pagination-bar.component';
import {SearchSidebarFiltersComponent} from './search/search-sidebar-filters/search-sidebar-filters.component';
import {ServiceListComponent} from './service-list/service-list.component';
import {ServiceQuery} from '../shared/models/service-query.model';

@Component({
  selector: 'app-my-services',
  templateUrl: './my-services.component.html',
  styleUrls: ['./my-services.component.css'],
  imports: [
    ControlsBarComponent,
    LoadingComponent,
    PaginationBarComponent,
    ServiceListComponent
  ]
})
export class MyServicesComponent implements OnInit{
  services = signal<ServiceModel[]>([]);
  loading = signal(false);
  currentPage = signal(0);
  totalPages = signal(0);
  pageSize = signal(10);
  sortOrder = signal("price,asc");

  constructor(private serviceApiService: ServiceApiService) {}

    ngOnInit(): void {
      this.fetchServices(this.buildQuery({ page: 0 }))
    }

    fetchServices(query: ServiceQuery){
      this.loading.set(true);
      this.serviceApiService.getServicesOfCurrentlyLoggedMember(query).subscribe({
        next: (res: PagedResponse) => {
          this.services.set(res.content);
          this.totalPages.set(res.totalPages);
          this.currentPage.set(res.number);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Failed to fetch services', err);
          this.services.set([]);
          this.loading.set(false);
        },
      });
    }



  buildQuery(overrides: Partial<ServiceQuery> = {}): ServiceQuery {
    return {
      page: overrides.page ?? this.currentPage(),
      pageSize: overrides.pageSize ?? this.pageSize(),
      sortOrder: overrides.sortOrder ?? this.sortOrder()
    };
  }

  goToPreviousPage() {
    if (this.currentPage() > 0) {
      const newPage = this.currentPage() - 1;
      this.currentPage.set(newPage);
      this.fetchServices(this.buildQuery({ page: newPage }));
    }
  }


  goToNextPage() {
    if (this.currentPage() + 1 < this.totalPages()) {
      const newPage = this.currentPage() + 1;
      this.currentPage.set(newPage);
      this.fetchServices(this.buildQuery({ page: newPage }));
    }
  }

  goToPage(page: number | string): void {
    const pageNumber = Number(page);
    if (pageNumber >= 0 && pageNumber < this.totalPages()) {
      this.currentPage.set(pageNumber);
      this.fetchServices(this.buildQuery({ page: pageNumber }));
    }
  }

  buildDummyData(): void {
    this.loading.set(true)
    this.serviceApiService.createService().subscribe({
      next: (res) => {
        console.log(res);
      },
      error: (err) => {
        console.error('Failed to create service', err);
        this.loading.set(false);
      },
    })
  }

}
