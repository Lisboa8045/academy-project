import { Component, OnInit } from '@angular/core';
import {AppointmentResponseDetailedDTO, AppointmentResponseDTO} from './appointment-response-dto.model';
import {AppointmentService} from './appointment.service';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import { Router } from '@angular/router';
import {Page} from './page.model';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-appointment-history',
  templateUrl: './appointment-history.component.html',
  styleUrls: ['./appointment-history.component.css'],
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    FormsModule,
    NgClass
  ]
})
export class AppointmentHistoryComponent implements OnInit {
  appointments: AppointmentResponseDTO[] = [];
  selectedAppointment?: AppointmentResponseDetailedDTO | null;
  totalItems = 0;
  pageSize = 10;
  currentPage = 0;
  dateOrder: 'asc' | 'desc' = 'desc';
  totalPages = 0;
  page: Page<AppointmentResponseDTO> | null = null;

  constructor(private appointmentHistoryService: AppointmentService,
              private router: Router) {

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

  openModal(id: number) {
    this.appointmentHistoryService.getAppointmentById(id).subscribe({
      next: data => this.selectedAppointment = data,
      error: err => alert('Error loading appointment.')
    });
  }


  closeModal(): void {
    this.selectedAppointment = null;
  }

  cancelAppointment(appointmentId: number): void {
    // Call your service to cancel the appointment
    //this.deleteAppointment(appointmentId);  // Or a dedicated cancel method
    this.closeModal();
  }

  onSortChange() {
    this.currentPage = 0;
    this.loadAppointments();
  }

  changePage(page: number) {
    if (page < 0 || page >= this.totalPages || page === this.currentPage) return;
    this.currentPage = page;
    this.loadAppointments();
  }
}
