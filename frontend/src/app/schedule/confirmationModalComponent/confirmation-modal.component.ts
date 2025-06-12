// confirmation-modal.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ServiceModel } from '../../service/service.model';
import { SlotModel } from '../models/slot.model';

@Component({
  selector: 'app-confirmation-modal',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['../schedule.component.css']
})
export class ConfirmationModalComponent {
  @Input() selectedService?: ServiceModel;
  @Input() selectedSlot?: SlotModel;
  @Output() cancel = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();
}
