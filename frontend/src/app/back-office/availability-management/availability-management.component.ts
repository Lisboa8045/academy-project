import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { AvailabilityService } from './availability-management.service';
import { AvailabilityModel } from '../../models/availability.model';
import { AuthStore } from '../../auth/auth.store';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { WeekNavigationComponent } from '../../shared/week-navigation/week-navigation.component';
import { WeekNavigationService } from '../../shared/week-navigation.service';
import { addDays, startOfWeek, endOfWeek, isSameDay, format, parseISO, getDay, setHours, setMinutes } from 'date-fns';
import { ptBR } from 'date-fns/locale';

interface RecurrenceOption {
  value: 'one-day' | 'all-year' | 'some-months' | 'weekly';
  label: string;
}

interface DayOfWeekOption {
  value: number;
  label: string;
}

interface MonthOption {
  value: number;
  label: string;
}

@Component({
  selector: 'app-availability-management',
  templateUrl: './availability-management.component.html',
  styleUrls: ['./availability-management.component.css'],
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, WeekNavigationComponent, ReactiveFormsModule]
})
export class AvailabilityManagementComponent implements OnInit {
  availabilities: AvailabilityModel[] = [];
  currentWeekStart: Date = new Date();
  currentWeekEnd: Date = new Date();
  today = new Date();

  // Form state
  availabilityForm: FormGroup;
  showFormModal = false;
  formMode: 'add' | 'edit' | 'bulk' = 'add';
  availabilityToDelete: AvailabilityModel | null = null;
  showDeleteConfirmation = false;
  localChanges: AvailabilityModel[] = [];
  deletedIds: number[] = [];
  selectedDay: Date | null = null;

  recurrenceOptions: RecurrenceOption[] = [
    { value: 'one-day', label: 'Only this day' },
    { value: 'weekly', label: 'Every week' },
    { value: 'some-months', label: 'Specific months' },
    { value: 'all-year', label: 'Entire year' }
  ];

  monthsList: MonthOption[] = [
    { value: 0, label: 'Jan' }, { value: 1, label: 'Feb' }, { value: 2, label: 'Mar' },
    { value: 3, label: 'Apr' }, { value: 4, label: 'May' }, { value: 5, label: 'Jun' },
    { value: 6, label: 'Jul' }, { value: 7, label: 'Aug' }, { value: 8, label: 'Sep' },
    { value: 9, label: 'Oct' }, { value: 10, label: 'Nov' }, { value: 11, label: 'Dec' }
  ];

  daysOfWeek: DayOfWeekOption[] = [
    { value: 0, label: 'Sun' },
    { value: 1, label: 'Mon' },
    { value: 2, label: 'Tue' },
    { value: 3, label: 'Wed' },
    { value: 4, label: 'Thu' },
    { value: 5, label: 'Fri' },
    { value: 6, label: 'Sat' }
  ];

  constructor(
    private availabilityService: AvailabilityService,
    private authStore: AuthStore,
    private weekNavigationService: WeekNavigationService,
    private fb: FormBuilder
  ) {
    this.availabilityForm = this.fb.group({
      startTime: ['09:00', Validators.required],
      endTime: ['17:00', Validators.required],
      applyType: ['one-day', Validators.required],
      selectedMonths: this.fb.array([]),
      selectedDays: this.fb.array(
        this.daysOfWeek
          .filter(day => [1, 2, 3, 4, 5].includes(day.value))
          .map(day => this.fb.control(day.value))
      )
    });
  }

  ngOnInit(): void {
    this.weekNavigationService.currentDate$.subscribe(date => {
      this.setWeekFromDate(date);
      this.loadAvailabilities();
    });
  }

  // View methods
  getWeekDays(): Date[] {
    return Array.from({ length: 7 }, (_, i) => addDays(this.currentWeekStart, i));
  }

  getAvailabilityForDay(day: Date): AvailabilityModel[] {
    return this.availabilities
      .filter(a => isSameDay(parseISO(a.startDateTime as string), day))
      .sort((a, b) => parseISO(a.startDateTime as string).getTime() - parseISO(b.startDateTime as string).getTime());
  }

  isToday(day: Date): boolean {
    return isSameDay(day, this.today);
  }

  isSlotBooked(avail: AvailabilityModel): boolean {
    return false; // Implement actual booking check logic here
  }

  isDaySelected(dayValue: number): boolean {
    return this.selectedDaysFormArray.value.includes(dayValue);
  }

  isMonthSelected(monthValue: number): boolean {
    return this.selectedMonthsFormArray.value.includes(monthValue);
  }

  // Form operations
  openAddForm(day: Date): void {
    this.selectedDay = day;
    this.availabilityForm.reset({
      startTime: '09:00',
      endTime: '17:00',
      applyType: 'one-day',
      selectedMonths: [],
      selectedDays: this.daysOfWeek
        .filter(day => [1, 2, 3, 4, 5].includes(day.value))
        .map(day => day.value)
    });
    this.formMode = 'add';
    this.showFormModal = true;
  }

  openEditForm(avail: AvailabilityModel): void {
    const startDate = parseISO(avail.startDateTime as string);
    const endDate = parseISO(avail.endDateTime as string);

    this.availabilityForm.patchValue({
      startTime: format(startDate, 'HH:mm'),
      endTime: format(endDate, 'HH:mm'),
      applyType: 'one-day'
    });

    this.formMode = 'edit';
    this.showFormModal = true;
  }

  openBulkAddForm(): void {
    this.availabilityForm.reset({
      startTime: '09:00',
      endTime: '17:00',
      applyType: 'weekly',
      selectedMonths: [],
      selectedDays: [1, 2, 3, 4, 5] // Default: Monday to Friday
    });
    this.formMode = 'bulk';
    this.showFormModal = true;
  }

  saveLocalChanges(): void {
    if (this.availabilityForm.invalid) return;

    const formValue = this.availabilityForm.value;
    const newAvailabilities = this.generateAvailabilitiesFromForm(formValue);

    this.localChanges = [
      ...this.localChanges,
      ...newAvailabilities.map(avail => ({
        ...avail,
        id: avail.id || this.generateTemporaryId(),
        memberId: this.authStore.id()
      }))
    ];

    this.updateView();
    this.closeFormModal();
  }

  closeFormModal(): void {
    this.showFormModal = false;
    this.selectedDay = null;
  }

  getModalTitle(): string {
    switch (this.formMode) {
      case 'add': return 'New availability';
      case 'edit': return 'Edit availability';
      case 'bulk': return 'Add in bulk';
      default: return 'Availability';
    }
  }

  get selectedDaysFormArray(): FormArray {
    return this.availabilityForm.get('selectedDays') as FormArray;
  }

  get selectedMonthsFormArray(): FormArray {
    return this.availabilityForm.get('selectedMonths') as FormArray;
  }

  toggleDaySelection(dayValue: number): void {
    const index = this.selectedDaysFormArray.value.indexOf(dayValue);
    if (index === -1) {
      this.selectedDaysFormArray.push(this.fb.control(dayValue));
    } else {
      this.selectedDaysFormArray.removeAt(index);
    }
  }

  toggleMonthSelection(monthValue: number): void {
    const index = this.selectedMonthsFormArray.value.indexOf(monthValue);
    if (index === -1) {
      this.selectedMonthsFormArray.push(this.fb.control(monthValue));
    } else {
      this.selectedMonthsFormArray.removeAt(index);
    }
  }

  // Delete operations
  confirmDelete(): void {
    if (this.availabilityToDelete) {
      if (this.availabilityToDelete.id && this.availabilityToDelete.id > 0) {
        this.deletedIds.push(this.availabilityToDelete.id);
      }

      this.localChanges = this.localChanges.filter(a => a.id !== this.availabilityToDelete?.id);
      this.updateView();
      this.showDeleteConfirmation = false;
      this.availabilityToDelete = null;
    }
  }

  cancelDelete(): void {
    this.showDeleteConfirmation = false;
    this.availabilityToDelete = null;
  }

  removeAvailability(avail: AvailabilityModel): void {
    this.availabilityToDelete = avail;
    this.showDeleteConfirmation = true;
  }

  // Save operations
  saveAllChanges(): void {
    const memberId = this.authStore.id();
    if (!memberId) return;

    const deleteObservables = this.deletedIds
      .filter(id => id !== undefined)
      .map(id => this.availabilityService.deleteAvailability(id));

    const createObservables = this.localChanges.map(change => {
      if (change.id && change.id > 0) {
        return this.availabilityService.updateAvailability(change);
      } else {
        return this.availabilityService.createAvailability(memberId, change);
      }
    });

    Promise.all([...deleteObservables, ...createObservables])
      .then(() => {
        this.resetLocalState();
        this.loadAvailabilities();
      });
  }

  isPending(avail: AvailabilityModel): boolean {
    return this.localChanges.some(a =>
      a.id === avail.id ||
      (a.startDateTime === avail.startDateTime && a.endDateTime === avail.endDateTime)
    );
  }

  // Private helper methods
  private updateView(): void {
    const combined = [
      ...this.availabilities.filter(a => !this.deletedIds.includes(a.id as number)),
      ...this.localChanges
    ];

    this.availabilities = combined.filter((avail, index, self) =>
        index === self.findIndex(a =>
          a.id === avail.id ||
          (a.startDateTime === avail.startDateTime && a.endDateTime === avail.endDateTime)
        )
    );
  }

  private setWeekFromDate(date: Date): void {
    this.currentWeekStart = startOfWeek(date, { weekStartsOn: 1 });
    this.currentWeekEnd = endOfWeek(date, { weekStartsOn: 1 });
  }

  private loadAvailabilities(): void {
    const memberId = this.authStore.id();
    if (!memberId) return;

    this.availabilityService.getAvailabilitiesByWorker(memberId)
      .subscribe(avail => {
        this.availabilities = avail
          .filter(a => !this.deletedIds.includes(a.id as number))
          .map(a => {
            const localChange = this.localChanges.find(lc => lc.id === a.id);
            return localChange || a;
          });

        // Add new local changes that don't exist in the server data
        const newLocalChanges = this.localChanges.filter(lc =>
          !this.availabilities.some(a => a.id === lc.id)
        );
        this.availabilities = [...this.availabilities, ...newLocalChanges];
      });
  }

  private generateAvailabilitiesFromForm(formValue: any): AvailabilityModel[] {
    const newAvailabilities: AvailabilityModel[] = [];
    const daysToProcess = this.getDaysToProcess(formValue);

    daysToProcess.forEach(day => {
      const startDateTime = this.combineDateAndTime(day, formValue.startTime);
      const endDateTime = this.combineDateAndTime(day, formValue.endTime);

      newAvailabilities.push({
        startDateTime,
        endDateTime,
        dayOfWeek: ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][day.getDay()]
      });
    });

    return newAvailabilities;
  }

  private getDaysToProcess(formValue: any): Date[] {
    const days: Date[] = [];
    const weekDays = this.getWeekDays();

    switch(formValue.applyType) {
      case 'one-day':
        if (this.selectedDay) {
          days.push(this.selectedDay);
        }
        break;

      case 'weekly':
        weekDays.forEach(day => {
          if (formValue.selectedDays.includes(day.getDay())) {
            days.push(day);
          }
        });
        break;

      case 'some-months':
        weekDays.forEach(day => {
          if (formValue.selectedDays.includes(day.getDay()) &&
            formValue.selectedMonths.includes(day.getMonth())) {
            days.push(day);
          }
        });
        break;

      case 'all-year':
        weekDays.forEach(day => {
          if (formValue.selectedDays.includes(day.getDay())) {
            days.push(day);
          }
        });
        break;
    }

    return days;
  }

  private combineDateAndTime(date: Date, time: string): string {
    const [hours, minutes] = time.split(':').map(Number);
    const newDate = setMinutes(setHours(date, hours), minutes);
    return newDate.toISOString();
  }

  private generateTemporaryId(): number {
    return Math.floor(Math.random() * -1000000);
  }

  private resetLocalState(): void {
    this.localChanges = [];
    this.deletedIds = [];
  }
}
