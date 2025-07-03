import {Component, OnInit} from '@angular/core';
import {AppointmentResponseDetailedDTO, AppointmentResponseDTO} from '../appointment-response-dto.model';
import {AppointmentService} from '../appointment.service';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {Router} from '@angular/router';
import {Page} from '../page.model';
import {FormsModule} from '@angular/forms';
import {AppointmentModalComponent} from '../appointment-modal/appointment-modal.component';
import {SortingOrderComponent} from '../../shared/sorting-order/sorting-order.component';
import {ConfirmationModalComponent} from '../../shared/confirmation-component/confirmation-modal.component';
import {StatusFilterComponent} from '../status-filter/status-filter.component';
import {AppointmentStatusEnumModel} from '../appointment-status.model';
import {snackBarSuccess} from '../../shared/snackbar/snackbar-success';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarError} from '../../shared/snackbar/snackbar-error';

@Component({
  selector: 'app-appointment-history',
  templateUrl: './appointment-history.component.html',
  styleUrls: ['./appointment-history.component.css'],
  standalone: true,
  providers: [DatePipe],
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    FormsModule,
    NgClass,
    AppointmentModalComponent,
    SortingOrderComponent,
    ConfirmationModalComponent,
    StatusFilterComponent
  ]
})
export class AppointmentHistoryComponent implements OnInit {
  appointments: AppointmentResponseDTO[] = [];
  filteredAppointments: AppointmentResponseDTO[] = [];
  selectedAppointment: AppointmentResponseDetailedDTO | null | undefined;
  viewAppointmentModal?: boolean = false;
  cancelAppointmentModal?: boolean = false;
  totalItems = 0;
  pageSize = 10;
  currentPage = 0;
  dateOrder: 'asc' | 'desc' = 'desc';
  totalPages = 0;
  page: Page<AppointmentResponseDTO> | null = null;
  status: AppointmentStatusEnumModel = AppointmentStatusEnumModel.ALL;

  constructor(private appointmentHistoryService: AppointmentService,
              private router: Router,
              private datePipe: DatePipe,
              private snackBar: MatSnackBar,) {

  }
    ngOnInit(): void {
      this.loadAppointments();
    }

  loadAppointments() {
    this.appointmentHistoryService.getUserAppointments(this.dateOrder).subscribe({
      next: (allAppointments: AppointmentResponseDTO[]) => {
        this.appointments = allAppointments;
        this.applyFiltersAndPagination();
      },
      error: (error) => {
        alert(error.error.message);
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
      error: err => alert('Error loading appointment.')
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

  onCancelAppointmentClick(id: number) {
    this.selectAppointment(id, () => this.cancelAppointmentModal = true);
  }

  onViewAppointment(id: number) {
    this.selectAppointment(id, () => this.viewAppointmentModal= true);
  }

  getCancelText(): string {
    if (!this.selectedAppointment) return '';

    const formattedDate = this.datePipe.transform(
      this.selectedAppointment.startDateTime,
      'MMM d, y, h:mm a'
    );

    return `cancel the appointment at ${formattedDate} for ${this.selectedAppointment.serviceName}`;
  }

  confirmCancelAppointment() {
    this.appointmentHistoryService.cancelAppointment(this.selectedAppointment!.id).subscribe({
      next: data => {
        this.selectedAppointment = null;
        this.cancelAppointmentModal = false;
        snackBarSuccess(this.snackBar, 'Appointment cancelled successfully');
        this.loadAppointments();
      },
      error: err => {
        this.selectedAppointment = null;
        this.cancelAppointmentModal = false;
        snackBarError(this.snackBar, err.error);
      }
    });
  }

  cancelCancelAppointment() {
    this.cancelAppointmentModal = false;
    this.selectedAppointment = null;
  }

  statusChange(newStatus: AppointmentStatusEnumModel) {
    this.status = newStatus;
    this.currentPage = 0;
    this.applyFiltersAndPagination();
  }
}
