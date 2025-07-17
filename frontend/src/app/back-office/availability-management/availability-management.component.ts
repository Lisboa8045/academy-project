  import {Component, effect, inject, Injector, OnInit, runInInjectionContext} from '@angular/core';
  import { CommonModule, DatePipe } from '@angular/common';
  import { AvailabilityService } from './availability-management.service';
  import { AvailabilityModel } from '../../models/availability.model';
  import { AuthStore } from '../../auth/auth.store';
  import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
  import { WeekNavigationComponent } from '../../shared/week-navigation/week-navigation.component';
  import { WeekNavigationService } from '../../shared/week-navigation.service';
  import { addDays, startOfWeek, endOfWeek, isSameDay, format, parseISO, setHours, setMinutes } from 'date-fns';
  import {forkJoin} from 'rxjs';

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
    isSettingDefaults = true; // Track if we're setting defaults
    hasDefaults = false; // Track if defaults exist

    // Form state
    availabilityForm: FormGroup;
    showFormModal = false;
    formMode: 'add' | 'edit' | 'bulk' = 'add';
    availabilityToDelete: AvailabilityModel | null = null;
    showDeleteConfirmation = false;
    localChanges: AvailabilityModel[] = [];
    deletedIds: number[] = [];
    selectedDay: Date | null = null;

    private readonly injector = inject(Injector);

    constructor(
      private readonly availabilityService: AvailabilityService,
      private readonly authStore: AuthStore,
      private readonly weekNavigationService: WeekNavigationService,
      private readonly fb: FormBuilder
    ) {
      this.availabilityForm = this.fb.group({
        startTime1: ['09:00', Validators.required],
        endTime1: ['12:00', Validators.required],
        startTime2: ['13:00', Validators.required],
        endTime2: ['17:00', Validators.required],
      });
    }

    ngOnInit(): void {
      runInInjectionContext(this.injector, () => {
        effect(() => {
          const userId = this.authStore.id();

          if (userId > 0) {
            this.checkExistingDefaults();

            this.weekNavigationService.currentDate$.subscribe(date => {
              this.setWeekFromDate(date);
              this.loadAvailabilities();
            });
          }
        });
      });
    }

    // Update the checkExistingDefaults method
    private checkExistingDefaults(): void {
      const memberId = this.authStore.id();
      if (!memberId || memberId <= 0) {  // Add validation for positive ID
        console.error('Invalid member ID:', memberId);
        return;
      }

      this.availabilityService.hasDefaultAvailability(memberId).subscribe({
        next: (hasDefaults) => {
          this.hasDefaults = hasDefaults;
          this.isSettingDefaults = !hasDefaults;
        },
        error: (err) => {
          console.error('Error checking default availability:', err);
          // Handle error appropriately
        }
      });
    }

    // View methods
    getWeekDays(): Date[] {
      return Array.from({ length: 7 }, (_, i) => addDays(this.currentWeekStart, i));
    }

    getAvailabilityForDay(day: Date): AvailabilityModel[] {
      // 1. Get all exceptions (DB + local changes)
      const exceptions = [
        ...this.availabilities.filter(a =>
          isSameDay(parseISO(a.startDateTime), day) &&
          a.isException &&
          !this.deletedIds.includes(a.id as number)
        ),
        ...this.localChanges.filter(a =>
          isSameDay(parseISO(a.startDateTime), day)
        )
      ];

      // 2. If exceptions exist, return ONLY those
      if (exceptions.length > 0) {
        return exceptions.sort((a, b) =>
          parseISO(a.startDateTime).getTime() - parseISO(b.startDateTime).getTime()
        );
      }

      // 3. Fall back to defaults if no exceptions
      if (this.hasDefaults && !this.isSettingDefaults) {
        return this.getDefaultIntervalsForDay(day);
      }

      return [];
    }

    private getDefaultIntervalsForDay(day: Date): AvailabilityModel[] {
      const dayName = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][day.getDay()];

      const defaultIntervals: AvailabilityModel[] = [];

      // Morning interval (09:00-12:00)
      defaultIntervals.push({
        startDateTime: this.combineDateAndTime(day, '09:00'),
        endDateTime: this.combineDateAndTime(day, '12:00'),
        dayOfWeek: dayName,
        isException: false,  // This is a default
        id: undefined,
        memberId: this.authStore.id()
      });

      // Afternoon interval (13:00-17:00)
      defaultIntervals.push({
        startDateTime: this.combineDateAndTime(day, '13:00'),
        endDateTime: this.combineDateAndTime(day, '17:00'),
        dayOfWeek: dayName,
        isException: false,  // This is a default
        id: undefined,
        memberId: this.authStore.id()
      });

      return defaultIntervals;
    }

    trackByAvailability(index: number, avail: AvailabilityModel): number {
      return avail.id ?? index;
    }

    isToday(day: Date): boolean {
      return isSameDay(day, this.today);
    }

    // Form operations
    openAddForm(day: Date): void {
      this.selectedDay = day;
      this.availabilityForm.reset({
        startTime1: '09:00',
        endTime1: '12:00',
        startTime2: '13:00',
        endTime2: '17:00'
      });
      this.formMode = 'add';
      this.showFormModal = true;
    }

    openEditForm(avail: AvailabilityModel): void {
      const day = parseISO(avail.startDateTime);
      const dayAvailabilities = this.getAvailabilityForDay(day);

      this.selectedDay = day;

      if (dayAvailabilities.length > 0) {
        const firstInterval = dayAvailabilities[0];
        this.availabilityForm.patchValue({
          startTime1: format(parseISO(firstInterval.startDateTime), 'HH:mm'),
          endTime1: format(parseISO(firstInterval.endDateTime), 'HH:mm')
        });

        if (dayAvailabilities.length > 1) {
          const secondInterval = dayAvailabilities[1];
          this.availabilityForm.patchValue({
            startTime2: format(parseISO(secondInterval.startDateTime), 'HH:mm'),
            endTime2: format(parseISO(secondInterval.endDateTime), 'HH:mm')
          });
        }
      }

      this.formMode = 'edit';
      this.showFormModal = true;
    }

    saveLocalChanges(): void {
      if (this.availabilityForm.invalid) {
        console.error('Form is invalid');
        return;
      }

      const formValue = this.availabilityForm.value;
      const newAvailabilities = this.generateAvailabilitiesFromForm(formValue);

      if (this.selectedDay) {
        const selectedDay = this.selectedDay;

        // Remove any existing changes for this day
        this.localChanges = this.localChanges.filter(a => {
          const availDate = parseISO(a.startDateTime);
          return !isSameDay(availDate, selectedDay);
        });

        // Remove any existing availabilities for this day
        this.availabilities = this.availabilities.filter(a => {
          const availDate = parseISO(a.startDateTime);
          return !isSameDay(availDate, selectedDay) || this.deletedIds.includes(a.id as number);
        });
      }

      // Add new availabilities with proper flags
      this.localChanges = [
        ...this.localChanges,
        ...newAvailabilities.map(avail => ({
          ...avail,
          id: avail.id ?? this.generateTemporaryId(),
          memberId: this.authStore.id(),
          isException: !this.isSettingDefaults
        }))
      ];

      this.closeFormModal();
    }

    closeFormModal(): void {
      this.showFormModal = false;
      this.selectedDay = null;
    }

    getModalTitle(): string {
      switch (this.formMode) {
        case 'add': return this.isSettingDefaults ? 'Add Default Availability' : 'Add Exception';
        case 'edit': return this.isSettingDefaults ? 'Edit Default Availability' : 'Edit Exception';
        default: return 'Availability';
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
      // Remove from local changes if present
      this.localChanges = this.localChanges.filter(a => a.id !== avail.id);

      if (avail.id && avail.id > 0) {
        // Track for backend deletion
        if (!this.deletedIds.includes(avail.id)) {
          this.deletedIds.push(avail.id);
        }
      }

      // Update view immediately
      this.updateView();
    }

    saveAllChanges(): void {
      const memberId = this.authStore.id();
      if (!memberId || memberId <= 0) {
        console.error('Invalid member ID');
        return;
      }

      if (this.isSettingDefaults) {
        // Save default template (unchanged)
        const days = [...new Set(this.localChanges.map(avail => avail.dayOfWeek))];
        const firstMorning = this.localChanges.find(a =>
          format(parseISO(a.startDateTime), 'HH:mm') === '09:00'
        );
        const firstAfternoon = this.localChanges.find(a =>
          format(parseISO(a.startDateTime), 'HH:mm') === '13:00'
        );

        this.availabilityService.saveDefaultAvailability(
          memberId,
          days,
          firstMorning ? format(parseISO(firstMorning.startDateTime), 'HH:mm') : '09:00',
          firstMorning ? format(parseISO(firstMorning.endDateTime), 'HH:mm') : '12:00',
          firstAfternoon ? format(parseISO(firstAfternoon.startDateTime), 'HH:mm') : '13:00',
          firstAfternoon ? format(parseISO(firstAfternoon.endDateTime), 'HH:mm') : '17:00'
        ).subscribe({
          next: () => {
            this.isSettingDefaults = false;
            this.hasDefaults = true;
            this.resetLocalState();
            this.loadAvailabilities();
          },
          error: (err) => {
            console.error('Error saving default availability:', err);
          }
        });
      } else {
        // 1. First handle deletions
        const deleteObservables = this.deletedIds.map(id =>
          this.availabilityService.deleteAvailability(id)
        );

        // 2. Then handle new exceptions (items with temporary IDs or no IDs)
        const newExceptions = this.localChanges.filter(change =>
          change.id === undefined || change.id < 0
        );

        const createObservables = newExceptions.map(change => {
          const cleanException = {
            dayOfWeek: change.dayOfWeek,
            startDateTime: change.startDateTime,
            endDateTime: change.endDateTime,
            isException: true,
            memberId: memberId
          };
          return this.availabilityService.createException(memberId, cleanException);
        });

        // 3. Combine both operations
        const allOperations = [...deleteObservables, ...createObservables];

        if (allOperations.length === 0) {
          this.resetLocalState();
          return;
        }

        forkJoin(allOperations).subscribe({
          next: () => {
            this.resetLocalState();
            this.loadAvailabilities();
            // Show success message to user
          },
          error: (err) => {
            console.error('Error saving changes:', err);
            // Show error to user
          }
        });
      }
    }

    isPending(avail: AvailabilityModel): boolean {
      return this.localChanges.some(a =>
        a.id === avail.id ||
        (a.startDateTime === avail.startDateTime && a.endDateTime === avail.endDateTime)
      );
    }

    private updateView(): void {
      this.availabilities = [
        ...this.availabilities.filter(a => !this.deletedIds.includes(a.id as number)),
        ...this.localChanges
      ].filter((avail, index, self) =>
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

      if (this.isSettingDefaults) {
        this.availabilities = [];
      } else {
        this.availabilityService.getAvailabilitiesByWorker(memberId)
          .subscribe(avail => {
            // Only keep exceptions (filter out defaults)
            this.availabilities = avail.filter(a =>
              a.isException && !this.deletedIds.includes(a.id as number)
            );

            // Merge with local changes
            const newLocalChanges = this.localChanges.filter(lc =>
              !this.availabilities.some(a => a.id === lc.id)
            );
            this.availabilities = [...this.availabilities, ...newLocalChanges];
          });
      }
    }

    private generateAvailabilitiesFromForm(formValue: any): AvailabilityModel[] {
      const newAvailabilities: AvailabilityModel[] = [];

      if (!this.selectedDay) {
        console.error('No day selected');
        return newAvailabilities;
      }

      const dayOfWeek = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][this.selectedDay.getDay()];
      const memberId = this.authStore.id();

      // First interval
      if (formValue.startTime1 && formValue.endTime1) {
        const startDateTime = this.combineDateAndTime(this.selectedDay, formValue.startTime1);
        const endDateTime = this.combineDateAndTime(this.selectedDay, formValue.endTime1);

        newAvailabilities.push({
          startDateTime: startDateTime,
          endDateTime: endDateTime,
          dayOfWeek: dayOfWeek,
          isException: !this.isSettingDefaults,
          memberId: memberId,
          id: undefined // Explicitly set to undefined
        });
      }

      // Second interval if provided
      if (formValue.startTime2 && formValue.endTime2) {
        const startDateTime = this.combineDateAndTime(this.selectedDay, formValue.startTime2);
        const endDateTime = this.combineDateAndTime(this.selectedDay, formValue.endTime2);

        newAvailabilities.push({
          startDateTime: startDateTime,
          endDateTime: endDateTime,
          dayOfWeek: dayOfWeek,
          isException: !this.isSettingDefaults,
          memberId: memberId,
          id: undefined // Explicitly set to undefined
        });
      }

      return newAvailabilities;
    }

    private combineDateAndTime(date: Date, time: string): string {
      const [hours, minutes] = time.split(':').map(Number);
      // Create a new date in local timezone without timezone conversion
      const newDate = new Date(
        date.getFullYear(),
        date.getMonth(),
        date.getDate(),
        hours,
        minutes
      );

      // Format as ISO string without timezone adjustment
      const pad = (n: number) => n < 10 ? '0' + n : n;
      return `${newDate.getFullYear()}-${pad(newDate.getMonth() + 1)}-${pad(newDate.getDate())}T${pad(hours)}:${pad(minutes)}:00`;
    }

    private generateTemporaryId(): number {
      return Math.floor(Math.random() * -1000000);
    }

    private resetLocalState(): void {
      this.localChanges = [];
      this.deletedIds = [];
    }
  }
