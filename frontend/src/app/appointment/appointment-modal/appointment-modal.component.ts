import { Component, Input, Output, EventEmitter } from '@angular/core';
import {CurrencyPipe, DatePipe, NgForOf, NgIf} from '@angular/common';
import {AppointmentResponseDetailedDTO} from '../appointment-response-dto.model';

@Component({
  selector: 'app-appointment-modal',
  templateUrl: './appointment-modal.component.html',
  imports: [
    DatePipe,
    NgForOf,
    CurrencyPipe,
  ]
})
export class AppointmentModalComponent {
  @Input({required:true}) appointment!: AppointmentResponseDetailedDTO;
  @Output() close = new EventEmitter<void>();
}
