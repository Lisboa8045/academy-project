import { Component, Input, Output, EventEmitter } from '@angular/core';
import {DatePipe, NgForOf, NgIf} from '@angular/common';
import {AppointmentResponseDetailedDTO} from '../appointment-response-dto.model';

@Component({
  selector: 'app-appointment-modal',
  templateUrl: './appointment-modal.component.html',
  imports: [
    DatePipe,
    NgForOf,
  ]
})
export class AppointmentModalComponent {
  @Input({required:true}) appointment!: AppointmentResponseDetailedDTO;
  @Output() close = new EventEmitter<void>();
}
