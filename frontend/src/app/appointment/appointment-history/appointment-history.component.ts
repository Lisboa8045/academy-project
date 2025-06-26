import { Component, OnInit } from '@angular/core';
import {AppointmentResponseDetailedDTO, AppointmentResponseDTO} from '../appointment-response-dto.model';
import {AppointmentService} from '../appointment.service';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import { Router } from '@angular/router';
import {Page} from '../page.model';
import {FormsModule} from '@angular/forms';
import {AppointmentModalComponent} from '../appointment-modal/appointment-modal.component';
import {SortingOrderComponent} from '../../shared/sorting-order/sorting-order.component';
import {CancelConfirmationModalComponent} from '../../shared/confirmation-component/confirmation-modal.component';

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
    CancelConfirmationModalComponent
  ]
})
export class AppointmentHistoryComponent implements OnInit {
  appointments: AppointmentResponseDTO[] = [];
  selectedAppointment?: AppointmentResponseDetailedDTO | null;
  viewAppointmentModal?: boolean = false;
  cancelAppointmentModal?: boolean = false;
  totalItems = 0;
  pageSize = 10;
  currentPage = 0;
  dateOrder: 'asc' | 'desc' = 'desc';
  totalPages = 0;
  page: Page<AppointmentResponseDTO> | null = null;

  constructor(private appointmentHistoryService: AppointmentService,
              private router: Router,
              private datePipe: DatePipe,) {

  }
    ngOnInit(): void {
      this.loadAppointments();
    }

    loadAppointments(){
      this.appointmentHistoryService.getUserAppointments({
          page: this.currentPage,
          pageSize: this.pageSize,
          dateOrder: this.dateOrder
      }
      ).subscribe({
        next:(page: Page<AppointmentResponseDTO>) => {
          this.appointments = page.content;
          this.totalItems = this.appointments.length;
          this.currentPage = page.number;
          this.totalPages = page.totalPages;
          this.page = page;
        },
        error:(error) => {
          alert(error.error.message);
        }
      })
      console.log("APPOINTMENTS");
      console.log(this.appointments);
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
    this.loadAppointments();
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
    this.cancelAppointmentModal = false;
    this.selectedAppointment = null;
    alert("canceled appointment");
  }

  cancelCancelAppointment() {
    this.cancelAppointmentModal = false;
    this.selectedAppointment = null;
  }
}
