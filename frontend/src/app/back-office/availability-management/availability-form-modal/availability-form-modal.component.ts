import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AvailabilityModel } from '../../../models/availability.model';

@Component({
  selector: 'app-availability-form-modal',
  templateUrl: './availability-form-modal.component.html',
  styleUrls: ['./availability-form-modal.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule]
})
export class AvailabilityFormModalComponent {
  @Input() day: Date | null = null;
  @Input() mode: 'add' | 'edit' = 'add';
  @Input() isSettingDefaults = false;
  @Output() close = new EventEmitter<void>();
  @Output() submit = new EventEmitter<AvailabilityModel[]>();

  availabilityForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.availabilityForm = this.fb.group({
      startTime1: ['09:00', Validators.required],
      endTime1: ['12:00', Validators.required],
      startTime2: ['13:00', Validators.required],
      endTime2: ['17:00', Validators.required],
    });
  }

  onSave(): void {
    if (this.availabilityForm.invalid || !this.day) return;

    const formValue = this.availabilityForm.value;
    const newAvailabilities: AvailabilityModel[] = [];
    const dayOfWeek = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][this.day.getDay()];

    // First interval
    if (formValue.startTime1 && formValue.endTime1) {
      newAvailabilities.push({
        startDateTime: this.combineDateAndTime(this.day, formValue.startTime1),
        endDateTime: this.combineDateAndTime(this.day, formValue.endTime1),
        dayOfWeek: dayOfWeek,
        isException: !this.isSettingDefaults
      });
    }

    // Second interval
    if (formValue.startTime2 && formValue.endTime2) {
      newAvailabilities.push({
        startDateTime: this.combineDateAndTime(this.day, formValue.startTime2),
        endDateTime: this.combineDateAndTime(this.day, formValue.endTime2),
        dayOfWeek: dayOfWeek,
        isException: !this.isSettingDefaults
      });
    }

    this.submit.emit(newAvailabilities);
  }

  private combineDateAndTime(date: Date, time: string): string {
    const [hours, minutes] = time.split(':').map(Number);
    const newDate = new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      hours,
      minutes
    );
    const pad = (n: number) => n < 10 ? '0' + n : n;
    return `${newDate.getFullYear()}-${pad(newDate.getMonth() + 1)}-${pad(newDate.getDate())}T${pad(hours)}:${pad(minutes)}:00`;
  }

  getModalTitle(): string {
    switch (this.mode) {
      case 'add': return this.isSettingDefaults ? 'Add Default Availability' : 'Add Exception';
      case 'edit': return this.isSettingDefaults ? 'Edit Default Availability' : 'Edit Exception';
      default: return 'Availability';
    }
  }
}
