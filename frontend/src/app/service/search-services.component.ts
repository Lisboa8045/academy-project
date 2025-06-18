import {Component, OnInit, signal} from '@angular/core';
import {ServiceModel} from './service.model';
import {PagedResponse, ServiceApiService} from '../shared/service-api.service';
import {ActivatedRoute} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {ServiceQuery} from '../shared/models/service-query.model';
import {ControlsBarComponent} from "./search/controls-bar/controls-bar.component";
import {SidebarFiltersComponent} from "./search/sidebar-filters/sidebar-filters.component";
import {PaginationBarComponent} from "./search/pagination-bar/pagination-bar.component";
import {ServiceListComponent} from "./service-list/service-list.component";

type ClearableFilterKeys = 'minPrice' | 'maxPrice' | 'minDuration' | 'maxDuration';

@Component({
  selector: 'app-search-services',
  templateUrl: './search-services.component.html',
  styleUrls: ['./search-services.component.css'],
  imports: [FormsModule, ControlsBarComponent, SidebarFiltersComponent, PaginationBarComponent, ServiceListComponent]
})
export class SearchServicesComponent implements OnInit {
  services = signal<ServiceModel[]>([]);
  searchTerm = signal('');
  loading = signal(false);
  currentPage = signal(0);
  totalPages = signal(0);
  pageSize = signal(10);
  sortOrder = signal("price,asc");

  filters = signal({
    minPrice: null as number | null,
    maxPrice: null as number | null,
    minDuration: null as number | null,
    maxDuration: null as number | null,
    negotiable: false,
    serviceType: ''
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
      serviceTypeName: this.filters().serviceType?.trim() || undefined,
    };
  }

  onFilterChange() {
    this.currentPage.set(0);
    this.fetchServices(this.buildQuery({ page: 0 }));
    this.appliedFilters.set({...this.filters()});
  }

  getPaginationPages(): (number | string)[] {
    const total = this.totalPages();
    const current = this.currentPage();

    const pages: (number | string)[] = [];

    if (total <= 7) {
      for (let i = 0; i < total; i++) {
        pages.push(i);
      }
    } else {
      pages.push(0);

      const windowSize = 3;

      let start = Math.max(1, current - 1);
      let end = Math.min(total - 2, current + 1);

      if (current <= 2) {
        start = 1;
        end = 1 + windowSize - 1;
      } else if (current >= total - 3) {
        start = total - windowSize - 1;
        end = total - 2;
      }

      if (start > 1) {
        pages.push('...');
      }

      for (let i = start; i <= end; i++) {
        pages.push(i);
      }

      if (end < total - 2) {
        pages.push('...');
      }

      pages.push(total - 1);
    }

    return pages;
  }
}
