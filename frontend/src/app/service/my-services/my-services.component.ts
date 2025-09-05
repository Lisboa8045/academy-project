import {Component, effect, Input, signal} from '@angular/core';
import {PagedResponse, ServiceApiService} from '../../shared/service-api.service';
import {ServiceModel} from '../service.model';
import {ControlsBarComponent} from '../search/controls-bar/controls-bar.component';
import {LoadingComponent} from '../../loading/loading.component';
import {PaginationBarComponent} from '../search/pagination-bar/pagination-bar.component';
import {ServiceListComponent} from '../service-list/service-list.component';
import {ServiceQuery} from '../../shared/models/service-query.model';
import {AuthStore} from '../../auth/auth.store';
import {NgTemplateOutlet} from '@angular/common';
import {RouterLink} from '@angular/router';
import {MemberResponseDTO} from '../../auth/member-response-dto.model';

@Component({
  selector: 'app-my-services',
  templateUrl: './my-services.component.html',
  styleUrls: ['./my-services.component.css'],
  imports: [
    ControlsBarComponent,
    LoadingComponent,
    PaginationBarComponent,
    ServiceListComponent,
    RouterLink,
    NgTemplateOutlet
  ]
})
export class MyServicesComponent{
  @Input() member : MemberResponseDTO | undefined;
  memberId! : number;
  services = signal<ServiceModel[]>([]);
  loading = signal(false);
  currentPage = signal(0);
  totalPages = signal(0);
  pageSize = signal(10);
  sortOrder = signal("price,asc");
  isMyServices = false;

  constructor(private serviceApiService: ServiceApiService, private authStore: AuthStore) {
    this.loading.set(true);
    effect(() => {
      if (this.member ===  undefined) {
        this.memberId = this.authStore.id();
        this.isMyServices = true;
      } else {
        this.memberId = this.member.id;
      }

      if (this.memberId === -1) {
        this.services.set([]);
        this.totalPages.set(0);
        this.currentPage.set(0);
        this.loading.set(false);
        return;
      }

      if (this.memberId !== undefined && this.memberId !== null && this.memberId > 0) {
        this.fetchServices(this.buildQuery({ page: 0 }));
      }
    });
  }

    fetchServices(query: ServiceQuery){
      this.serviceApiService.getServicesOfMember(query, this.memberId).subscribe({
        next: (res: PagedResponse) => {
          let services = res.content;
          if (this.memberId) {
            services = services.filter(service => service.status == 'APPROVED');
          }
          this.services.set(services);
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

  get clickPath() {
    return this.isMyServices ? 'backoffice/services' : 'services';
  }
}
