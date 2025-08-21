import { Component, Input, Output, EventEmitter } from '@angular/core';
import {CurrencyPipe, DatePipe, NgForOf, NgIf} from '@angular/common';
import {AppointmentResponseDetailedDTO} from '../appointment-response-dto.model';
import {ConfirmationModalComponent} from '../../shared/confirmation-component/confirmation-modal.component';
import {AppointmentService} from '../appointment.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarSuccess} from '../../shared/snackbar/snackbar-success';
import {snackBarError} from '../../shared/snackbar/snackbar-error';

@Component({
  selector: 'app-appointment-modal',
  templateUrl: './appointment-modal.component.html',
  imports: [
    DatePipe,
    NgForOf,
    CurrencyPipe,
    ConfirmationModalComponent,
    NgIf,
  ],
  providers: [DatePipe]
})
export class AppointmentModalComponent {
  @Input({required:true}) appointment!: AppointmentResponseDetailedDTO;
  @Input() isCalendarContext = false;
  @Output() close = new EventEmitter<void>();
  cancelAppointmentModal = false;
  finishAppointmentModal = false;

  constructor(private datePipe: DatePipe, private appointmentHistoryService:AppointmentService, private snackBar: MatSnackBar) {}

  getCancelText(): string {
    if (!this.appointment) return '';
    const formattedDate = this.datePipe.transform(
      this.appointment.startDateTime,
      'MMM d, y, h:mm a'
    );

    return `cancel the appointment at ${formattedDate} for ${this.appointment.serviceName}`;
  }

  getFinishText(): string {
    if (!this.appointment) return '';
    const formattedDate = this.datePipe.transform(
      this.appointment.startDateTime,
      'MMM d, y, h:mm a'
    );

    return `finish the appointment at ${formattedDate} for ${this.appointment.serviceName}`;
  }

  hasHappened(appt: { startDateTime: string | Date; endDateTime?: string | Date }): boolean {
    const endsAt = appt.endDateTime ?? appt.startDateTime; // consider it "happened" when it has ended
    return new Date(endsAt).getTime() < Date.now();
  }

  confirmCancelAppointment() {
    this.appointmentHistoryService.cancelAppointment(this.appointment!.id).subscribe({
      next: data => {
        this.cancelAppointmentModal = false;
        snackBarSuccess(this.snackBar, 'Appointment cancelled successfully');
        this.close.emit();
      },
      error: err => {
        this.cancelAppointmentModal = false;
        snackBarError(this.snackBar, err.error);
      }
    });
  }

  confirmFinishAppointment() {
    this.appointmentHistoryService.finishAppointment(this.appointment!.id).subscribe({
      next: data => {
        this.finishAppointmentModal = false;
        snackBarSuccess(this.snackBar, 'Appointment finished successfully');
        this.close.emit();
      },
      error: err => {
        this.finishAppointmentModal = false;
        snackBarError(this.snackBar, err.error);
      }
    });
  }
}
