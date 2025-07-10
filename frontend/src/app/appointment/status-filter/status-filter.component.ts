import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {AppointmentStatusEnumModel} from '../appointment-status.model';
import {NgForOf} from '@angular/common';

@Component({
  selector: 'app-status-filter',
  templateUrl: './status-filter.component.html',
  standalone: true,
  imports: [FormsModule, NgForOf]
})
export class StatusFilterComponent {
  @Input() status: AppointmentStatusEnumModel = AppointmentStatusEnumModel.ALL;
  @Output() statusChange = new EventEmitter<AppointmentStatusEnumModel>();

  statusOptions = Object.values(AppointmentStatusEnumModel);

  onStatusChange(event: Event) {
    const newStatus = (event.target as HTMLSelectElement).value as AppointmentStatusEnumModel;
    this.statusChange.emit(newStatus);
  }
}
