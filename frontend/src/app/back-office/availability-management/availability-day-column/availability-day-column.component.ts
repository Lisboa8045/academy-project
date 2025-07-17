import { Component, EventEmitter, Input, Output } from '@angular/core';
import {DatePipe, NgForOf, NgIf} from '@angular/common';
import { AvailabilityModel } from '../../../models/availability.model';

@Component({
  selector: 'app-availability-day-column',
  templateUrl: './availability-day-column.component.html',
  styleUrls: ['./availability-day-column.component.css'],
  standalone: true,
  imports: [DatePipe, NgForOf, NgIf]
})
export class AvailabilityDayColumnComponent {
  @Input() day!: Date;
  @Input() isToday = false;
  @Input() isSettingDefaults = false;
  @Input() availabilities: AvailabilityModel[] = [];
  @Input() hasDefaults = false;

  @Output() addSlot = new EventEmitter<Date>();
  @Output() editSlot = new EventEmitter<AvailabilityModel>();
  @Output() deleteSlot = new EventEmitter<AvailabilityModel>();
}
