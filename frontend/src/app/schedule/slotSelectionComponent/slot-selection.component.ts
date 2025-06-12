import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SlotModel } from '../models/slot.model';
import {CommonModule, DatePipe} from '@angular/common';

@Component({
  selector: 'app-slot-selection',
  templateUrl: 'slot-selection.component.html',
  styleUrls: ['../schedule.component.css'],
  imports: [
    DatePipe, CommonModule,
  ],
})
export class SlotSelectionComponent {
  @Input() slots: SlotModel[] = [];
  @Input() providers: string[] = [];
  @Input() selectedProvider: string | null = null;
  @Input() currentWeekStart!: Date;
  @Input() currentWeekEnd!: Date;
  @Input() weeklySlots: { [key: string]: SlotModel[] } = {};

  @Output() providerFilter = new EventEmitter<string | null>();
  @Output() previousWeek = new EventEmitter<void>();
  @Output() nextWeek = new EventEmitter<void>();
  @Output() slotSelected = new EventEmitter<SlotModel>();
  @Output() backToServices = new EventEmitter<void>();

  getWeekDays(): Date[] {
    const days = [];
    for (let i = 0; i < 7; i++) {
      days.push(new Date(this.currentWeekStart.getTime() + i * 24 * 60 * 60 * 1000));
    }
    return days;
  }

  getSlotsForDay(day: Date): SlotModel[] {
    const key = day.toISOString().split('T')[0];
    return this.weeklySlots[key] || [];
  }

  filterByProvider(provider: string | null) {
    this.providerFilter.emit(provider);
  }

  goToPreviousWeek() {
    this.previousWeek.emit();
  }

  goToNextWeek() {
    this.nextWeek.emit();
  }

  selectSlot(slot: SlotModel) {
    this.slotSelected.emit(slot);
  }
}
