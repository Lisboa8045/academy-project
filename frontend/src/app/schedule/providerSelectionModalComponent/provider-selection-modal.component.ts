// provider-selection-modal.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { SlotModel } from '../../models/slot.model';

@Component({
  selector: 'app-provider-selection-modal',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './provider-selection-modal.component.html',
  styleUrls: ['../schedule.component.css']
})
export class ProviderSelectionModalComponent {
  @Input() providerOptions: SlotModel[] = [];
  @Output() providerSelected = new EventEmitter<SlotModel>();
  @Output() backToSlots = new EventEmitter<void>();
}
