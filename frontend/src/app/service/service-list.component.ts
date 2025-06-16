import {Component, OnInit, signal} from '@angular/core';
import {ServiceModel} from './service.model';
import {ServiceApiService, PagedResponse} from '../shared/service-api.service';
import {LoadingComponent} from '../loading/loading.component';
import {DatePipe, NgForOf} from '@angular/common';
import {ActivatedRoute} from '@angular/router';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-service-list',
  templateUrl: './service-list.component.html',
  styleUrls: ['./service-list.component.css'],
  imports: [LoadingComponent, NgForOf, DatePipe, FormsModule]
})
export class ServiceListComponent implements OnInit{
  services = signal<ServiceModel[]>([]);
  searchTerm = signal('');
  loading = signal(false);
  currentPage = signal(0);
  totalPages = signal(0);
  pageSize = signal(1);
  sortOrder = signal("price,asc");

  constructor(private serviceApi: ServiceApiService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const q = (params['q'] || '').trim();
      this.searchTerm.set(q);
      this.fetchServices();
    });
  }

  fetchServices(filters?: { pageSize?: number; sortOrder?: string; page?: number }): void {
    const query = this.searchTerm().trim();
    this.loading.set(true);

    const pageSize = filters?.pageSize ?? this.pageSize();
    const sortOrder = filters?.sortOrder ?? this.sortOrder();
    const page = filters?.page ?? this.currentPage();

    this.serviceApi.searchServices(query, page, pageSize, sortOrder).subscribe({
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

  onFilterChange() {
    this.fetchServices({
      pageSize: this.pageSize(),
      sortOrder: this.sortOrder(),
      page: 0
    });
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


  goToPreviousPage() {
    if (this.currentPage() > 0) {
      this.currentPage.set(this.currentPage() - 1);
      this.fetchServices({
        page: this.currentPage(),
        pageSize: this.pageSize(),
        sortOrder: this.sortOrder()
      });
    }
  }

  goToNextPage() {
    if (this.currentPage() + 1 < this.totalPages()) {
      this.currentPage.set(this.currentPage() + 1);
      this.fetchServices({
        page: this.currentPage(),
        pageSize: this.pageSize(),
        sortOrder: this.sortOrder()
      });
    }
  }

  goToPage(page: number | string): void {
    const pageNumber = Number(page);
    if (pageNumber >= 0 && pageNumber < this.totalPages()) {
      this.currentPage.set(pageNumber);
      this.fetchServices({
        page: this.currentPage(),
        pageSize: this.pageSize(),
        sortOrder: this.sortOrder()
      });
    }
  }

  get pageSizeValue() {
    return this.pageSize();
  }

  set pageSizeValue(value: number) {
    this.pageSize.set(value);
  }

  get sortOrderValue() {
    return this.sortOrder();
  }
  set sortOrderValue(value: string) {
    this.sortOrder.set(value);
  }

  isPageNumber(page: number | string): page is number {
    return typeof page === 'number';
  }

  displayPageNumber(page: number | string): number {
    if (this.isPageNumber(page)) {
      return page + 1;
    }
    return 0;
  }
}
