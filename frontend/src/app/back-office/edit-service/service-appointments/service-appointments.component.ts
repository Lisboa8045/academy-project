import {Component, input, OnInit, ViewEncapsulation} from '@angular/core';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {
  AppointmentResponseDetailedDTO,
  AppointmentResponseDTO
} from '../../../appointment/appointment-response-dto.model';
import {Page} from '../../../appointment/page.model';
import {AppointmentStatusEnumModel} from '../../../appointment/appointment-status.model';
import {AppointmentService} from '../../../appointment/appointment.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarError} from '../../../shared/snackbar/snackbar-error';
import {SortingOrderComponent} from '../../../shared/sorting-order/sorting-order.component';
import {StatusFilterComponent} from '../../../appointment/status-filter/status-filter.component';
import {AppointmentModalComponent} from '../../../appointment/appointment-modal/appointment-modal.component';

@Component({
  selector: 'app-service-appointments',
  templateUrl: './service-appointments.component.html',
  styleUrl: './service-appointments.component.css',
  encapsulation: ViewEncapsulation.None,
  standalone: true,
  providers: [DatePipe],
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    FormsModule,
    SortingOrderComponent,
    StatusFilterComponent,
    AppointmentModalComponent,
    NgClass,
  ]
})
export class ServiceAppointmentsComponent implements OnInit {
  serviceId = input.required<number>();
  appointments: AppointmentResponseDTO[] = [];
  filteredAppointments: AppointmentResponseDTO[] = [];
  selectedAppointment: AppointmentResponseDetailedDTO | null | undefined;
  viewAppointmentModal?: boolean = false;
  totalItems = 0;
  pageSize = 10;
  currentPage = 0;
  dateOrder: 'asc' | 'desc' = 'desc';
  totalPages = 0;
  page: Page<AppointmentResponseDTO> | null = null;
  status: AppointmentStatusEnumModel = AppointmentStatusEnumModel.ALL;

  constructor(private appointmentHistoryService: AppointmentService,
              private snackBar: MatSnackBar,) {

  }

  ngOnInit(): void {
    this.loadAppointments();
  }

  loadAppointments() {
    this.appointmentHistoryService.getServiceAppointments(this.serviceId(), this.dateOrder).subscribe({
      next: (allAppointments: AppointmentResponseDTO[]) => {
        this.appointments = allAppointments;
        this.applyFiltersAndPagination();
      },
      error: (error) => {
        snackBarError(this.snackBar, error.error.message);
      }
    });
  }

  applyFiltersAndPagination() {
    // Apply filter
    let filtered = this.status === AppointmentStatusEnumModel.ALL
      ? this.appointments
      : this.appointments.filter(a => a.status === this.status);

    this.totalItems = filtered.length;
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);

    // Apply pagination to filtered results
    const startIndex = this.currentPage * this.pageSize;
    this.filteredAppointments = filtered.slice(startIndex, startIndex + this.pageSize);
  }


  selectAppointment(id: number, callback?: () => void) {
    this.appointmentHistoryService.getAppointmentById(id).subscribe({
      next: data => {
        console.log("data");
        console.log(data);
        this.selectedAppointment = data;
        if(callback)
          callback();
      },
      error: () => snackBarError(this.snackBar,'Error loading appointment.')
    });
  }

  closeModal(): void {
    this.selectedAppointment = null;
    this.viewAppointmentModal = false;
  }

  changePage(page: number) {
    if (page < 0 || page >= this.totalPages || page === this.currentPage) return;
    this.currentPage = page;
    this.applyFiltersAndPagination();
  }

  onOrderChange(newOrder: 'asc' | 'desc') {
    console.log("order changed to " + newOrder);
    this.dateOrder = newOrder;
    this.currentPage = 0;
    this.loadAppointments();
  }

  onViewAppointment(id: number) {
    this.selectAppointment(id, () => this.viewAppointmentModal= true);
  }

  statusChange(newStatus: AppointmentStatusEnumModel) {
    this.status = newStatus;
    this.currentPage = 0;
    this.applyFiltersAndPagination();
  }
}
