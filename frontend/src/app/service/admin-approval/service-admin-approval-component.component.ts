import {Component, computed, effect, Input, signal} from '@angular/core';
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
import {MatSnackBar} from "@angular/material/snack-bar";
import {snackBarSuccess} from "../../shared/snackbar/snackbar-success";
import {snackBarError} from "../../shared/snackbar/snackbar-error";

@Component({
  selector: 'app-service-admin-approval-component',
  templateUrl: './service-admin-approval-component.component.html',
  styleUrls: ['./service-admin-approval-component.component.css'],
  imports: [
    ControlsBarComponent,
    LoadingComponent,
    PaginationBarComponent,
    ServiceListComponent,
    NgTemplateOutlet
  ]
})
export class ServiceAdminApprovalComponent{
  @Input() memberIdInput : number | undefined;
  memberId = computed(() => this.memberIdInput ?? this.authStore.id());
  services = signal<ServiceModel[]>([]);
  loading = signal(false);
  currentPage = signal(0);
  totalPages = signal(0);
  pageSize = signal(10);
  sortOrder = signal("price,asc");
  enabled = false;

  constructor(private serviceApiService: ServiceApiService,
              private authStore: AuthStore,
              private snackBar: MatSnackBar) {
    this.loading.set(true);
    effect(() => {
      const id = this.memberId();
      if (id !== undefined && id !== null && id > 0) {
        this.fetchServices(this.buildQuery({ page: 0, enabled:false, status:'PENDING_APPROVAL' }));
      }
    });
  }

  fetchServices(query: ServiceQuery){
    this.serviceApiService.searchServices('', query).subscribe({
      next: (res: PagedResponse) => {
        this.services.set(res.content);
        this.totalPages.set(res.totalPages);
        this.currentPage.set(res.number);
        this.loading.set(false);
      },
      error: (err) => {
        snackBarError(this.snackBar, 'Failed to fetch services')
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
      enabled: this.enabled,
      status: overrides.status ?? '',
    };
  }

  goToPreviousPage() {
    if (this.currentPage() > 0) {
      const newPage = this.currentPage() - 1;
      this.currentPage.set(newPage);
      this.fetchServices(this.buildQuery({ page: newPage, enabled:false, status:'PENDING_APPROVAL' }));
    }
  }

  goToNextPage() {
    if (this.currentPage() + 1 < this.totalPages()) {
      const newPage = this.currentPage() + 1;
      this.currentPage.set(newPage);
      this.fetchServices(this.buildQuery({ page: newPage, enabled:false, status:'PENDING_APPROVAL' }));
    }
  }

  goToPage(page: number | string): void {
    const pageNumber = Number(page);
    if (pageNumber >= 0 && pageNumber < this.totalPages()) {
      this.currentPage.set(pageNumber);
      this.fetchServices(this.buildQuery({ page: pageNumber, enabled:false, status:'PENDING_APPROVAL' }));
    }
  }

  rejectService(service: ServiceModel) {
    this.serviceApiService.rejectService(service.id).subscribe({
      next: () => {
        snackBarSuccess(this.snackBar, "Service rejected successfully")
        this.fetchServices(this.buildQuery({ page: this.currentPage(), enabled:false, status:'PENDING_APPROVAL' }));
      },
      error: err => snackBarError(this.snackBar, err)
    });
  }

  approveService(service: ServiceModel) {
    this.serviceApiService.approveService(service.id).subscribe({
      next: () => {
        snackBarSuccess(this.snackBar, "Service approved successfully")
        this.fetchServices(this.buildQuery({ page: this.currentPage(), enabled:false, status:'PENDING_APPROVAL' }));
      },
      error: err => snackBarError(this.snackBar, err)
    });
  }
}
