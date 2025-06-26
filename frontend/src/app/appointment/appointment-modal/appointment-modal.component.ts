import { Component, Input, Output, EventEmitter } from '@angular/core';
import {DatePipe, NgIf} from '@angular/common';

@Component({
  selector: 'app-appointment-modal',
  templateUrl: './appointment-modal.component.html',
  imports: [
    DatePipe,
  ]
})
export class AppointmentModalComponent {
  @Input() appointment: any;
  @Output() close = new EventEmitter<void>();
}
