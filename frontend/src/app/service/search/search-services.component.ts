import {Component, OnInit, signal, WritableSignal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {ControlsBarComponent} from "./controls-bar/controls-bar.component";
import {SidebarFiltersComponent} from "./sidebar-filters/sidebar-filters.component";
import {ServiceModel} from '../service.model';
import {LoadingComponent} from "../../loading/loading.component";
import {ServiceListComponent} from "../service-list/service-list.component";
import {PagedResponse, ServiceApiService} from "../../shared/service-api.service";
import {ServiceQuery} from "../../shared/models/service-query.model";
import {PaginationBarComponent} from "./pagination-bar/pagination-bar.component";

@Component({
  selector: 'app-search-services',
  templateUrl: './search-services.component.html',
  styleUrls: ['./search-services.component.css'],
  imports: [LoadingComponent, FormsModule, ControlsBarComponent, SidebarFiltersComponent, ServiceListComponent, PaginationBarComponent]
})
export class SearchServicesComponent implements OnInit {
  services = signal<ServiceModel[]>([]);
  searchTerm = signal('');
  loading = signal(false);
  currentPage = signal(0);
  totalPages = signal(0);
  pageSize = signal(10);
  sortOrder = signal("price,asc");

  filters: WritableSignal<{
    minPrice: number | null;
    maxPrice: number | null;
    minDuration: number | null;
    maxDuration: number | null;
    negotiable: boolean;
    serviceType: string;
  }> = signal({
    minPrice: null,
    maxPrice: null,
    minDuration: null,
    maxDuration: null,
    negotiable: false,
    serviceType: '',
  });

  appliedFilters = signal({
    minPrice: null as number | null,
    maxPrice: null as number | null,
    minDuration: null as number | null,
    maxDuration: null as number | null,
    negotiable: false,
    serviceType: ''
  });

  serviceTypes: string[] = [];

  constructor(private serviceApi: ServiceApiService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.serviceApi.getServiceTypes().subscribe(types => {
      this.serviceTypes = types.map(type => type.name);
    });
    this.route.queryParams.subscribe(params => {
      const q = (params['q'] || '').trim();
      this.searchTerm.set(q);
      this.fetchServices(this.buildQuery({ page: 0 }));
    });
  }

  fetchServices(query: ServiceQuery): void {
    const search = this.searchTerm().trim();
    this.loading.set(true);

    this.serviceApi.searchServices(search, query).subscribe({
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
      sortOrder: overrides.sortOrder ?? this.sortOrder(),
      minPrice: this.filters().minPrice ?? undefined,
      maxPrice: this.filters().maxPrice ?? undefined,
      minDuration: this.filters().minDuration ?? undefined,
      maxDuration: this.filters().maxDuration ?? undefined,
      negotiable: this.filters().negotiable ?? undefined,
      serviceTypeName: this.filters().serviceType?.trim() || undefined
    };
  }

  onFilterChange() {
    this.currentPage.set(0);
    this.fetchServices(this.buildQuery({ page: 0 }));
    this.appliedFilters.set({...this.filters()});
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
}
