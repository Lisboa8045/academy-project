  import {Component, effect, inject, Injector, OnInit, runInInjectionContext} from '@angular/core';
  import { CommonModule, DatePipe } from '@angular/common';
  import { AvailabilityService } from './availability-management.service';
  import { AvailabilityModel } from '../../models/availability.model';
  import { AuthStore } from '../../auth/auth.store';
  import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
  import { WeekNavigationComponent } from '../../shared/week-navigation/week-navigation.component';
  import { WeekNavigationService } from '../../shared/week-navigation.service';
  import { addDays, startOfWeek, endOfWeek, isSameDay, format, parseISO, getDay, setHours, setMinutes } from 'date-fns';
  import { ptBR } from 'date-fns/locale';
  import {filter, take} from 'rxjs';
  import {toObservable} from '@angular/core/rxjs-interop';

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
      const dayOfWeek = day.getDay();

      // Get all availabilities for this specific day (both exceptions and local changes)
      const dayAvailabilities = [
        ...this.availabilities.filter(a =>
          isSameDay(parseISO(a.startDateTime as string), day) &&
          !this.deletedIds.includes(a.id as number)
        ),
        ...this.localChanges.filter(a =>
          isSameDay(parseISO(a.startDateTime as string), day)
        )
      ];

      // If there are any exceptions (or local changes), return only those
      if (dayAvailabilities.length > 0) {
        return dayAvailabilities.sort((a, b) =>
          parseISO(a.startDateTime as string).getTime() - parseISO(b.startDateTime as string).getTime()
        );
      }

      // If no exceptions and we have defaults, return default intervals
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
      return avail.id || index;
    }

    isToday(day: Date): boolean {
      return isSameDay(day, this.today);
    }

    isSlotBooked(avail: AvailabilityModel): boolean {
      return false;
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
      const day = parseISO(avail.startDateTime as string);
      const dayAvailabilities = this.getAvailabilityForDay(day);

      this.selectedDay = day;

      if (dayAvailabilities.length > 0) {
        const firstInterval = dayAvailabilities[0];
        this.availabilityForm.patchValue({
          startTime1: format(parseISO(firstInterval.startDateTime as string), 'HH:mm'),
          endTime1: format(parseISO(firstInterval.endDateTime as string), 'HH:mm')
        });

        if (dayAvailabilities.length > 1) {
          const secondInterval = dayAvailabilities[1];
          this.availabilityForm.patchValue({
            startTime2: format(parseISO(secondInterval.startDateTime as string), 'HH:mm'),
            endTime2: format(parseISO(secondInterval.endDateTime as string), 'HH:mm')
          });
        }
      }

      this.formMode = 'edit';
      this.showFormModal = true;
    }

    openBulkAddForm(): void {
      this.availabilityForm.reset({
        startTime: '09:00',
        endTime: '17:00',
        applyType: 'weekly',
        selectedMonths: [],
        selectedDays: [1, 2, 3, 4, 5]
      });
      this.formMode = 'bulk';
      this.showFormModal = true;
    }

    saveLocalChanges(): void {
      if (this.availabilityForm.invalid) return;

      const formValue = this.availabilityForm.value;
      const newAvailabilities = this.generateAvailabilitiesFromForm(formValue);

      if (this.selectedDay) {
        const selectedDay = this.selectedDay;

        this.localChanges = this.localChanges.filter(a => {
          const availDate = parseISO(a.startDateTime as string);
          return !isSameDay(availDate, selectedDay);
        });

        this.availabilities = this.availabilities.filter(a => {
          const availDate = parseISO(a.startDateTime as string);
          return !isSameDay(availDate, selectedDay) || this.deletedIds.includes(a.id as number);
        });
      }

      this.localChanges = [
        ...this.localChanges,
        ...newAvailabilities.map(avail => ({
          ...avail,
          id: avail.id || this.generateTemporaryId(),
          memberId: this.authStore.id(),
          isException: !this.isSettingDefaults // Mark as exception if not setting defaults
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
      this.localChanges = this.localChanges.filter(a => a.id !== avail.id);

      if (avail.id && avail.id > 0) {
        this.deletedIds.push(avail.id);
        this.availabilities = this.availabilities.filter(a => a.id !== avail.id);
      } else {
        this.availabilities = this.availabilities.filter(a =>
          a.startDateTime !== avail.startDateTime ||
          a.endDateTime !== avail.endDateTime
        );
      }
    }

    saveAllChanges(): void {
      const memberId = this.authStore.id();
      if (!memberId) return;

      if (this.isSettingDefaults) {
        const days = [...new Set(this.localChanges.map(avail => avail.dayOfWeek))];
        const firstMorning = this.localChanges.find(a =>
          format(parseISO(a.startDateTime), 'HH:mm') === '09:00' // Adjust based on your morning start
        );
        const firstAfternoon = this.localChanges.find(a =>
          format(parseISO(a.startDateTime), 'HH:mm') === '13:00' // Adjust based on your afternoon start
        );

        this.availabilityService.saveDefaultAvailability(
          memberId,
          days,
          firstMorning ? format(parseISO(firstMorning.startDateTime), 'HH:mm') : '09:00',
          firstMorning ? format(parseISO(firstMorning.endDateTime), 'HH:mm') : '12:00',
          firstAfternoon ? format(parseISO(firstAfternoon.startDateTime), 'HH:mm') : '13:00',
          firstAfternoon ? format(parseISO(firstAfternoon.endDateTime), 'HH:mm') : '17:00'
        ).subscribe(() => {
          this.isSettingDefaults = false;
          this.hasDefaults = true;
          this.resetLocalState();
          this.loadAvailabilities();
        });
      } else {
        // Original save logic for exceptions
        const deleteObservables = this.deletedIds
          .filter(id => id !== undefined)
          .map(id => this.availabilityService.deleteAvailability(id));

        const createObservables = this.localChanges.map(change => {
          if (change.id && change.id > 0) {
            return this.availabilityService.updateAvailability(change);
          } else {
            return this.availabilityService.createAvailability(memberId, {
              ...change,
              isException: true
            });
          }
        });

        Promise.all([...deleteObservables, ...createObservables])
          .then(() => {
            this.resetLocalState();
            this.loadAvailabilities();
          });
      }
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

    private dayHasExceptions(day: Date): boolean {
      return [
        ...this.availabilities,
        ...this.localChanges
      ].some(a =>
        isSameDay(parseISO(a.startDateTime as string), day) &&
        (a.isException || !a.hasOwnProperty('isException'))
      );
    }

    private generateAvailabilitiesFromForm(formValue: any): AvailabilityModel[] {
      const newAvailabilities: AvailabilityModel[] = [];

      if (!this.selectedDay) return newAvailabilities;

      // First interval
      if (formValue.startTime1 && formValue.endTime1) {
        const startDateTime1 = this.combineDateAndTime(this.selectedDay, formValue.startTime1);
        const endDateTime1 = this.combineDateAndTime(this.selectedDay, formValue.endTime1);

        newAvailabilities.push({
          startDateTime: startDateTime1,
          endDateTime: endDateTime1,
          dayOfWeek: ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][this.selectedDay.getDay()],
          isException: false
        });
      }

      // Second interval if provided
      if (formValue.startTime2 && formValue.endTime2) {
        const startDateTime2 = this.combineDateAndTime(this.selectedDay, formValue.startTime2);
        const endDateTime2 = this.combineDateAndTime(this.selectedDay, formValue.endTime2);

        newAvailabilities.push({
          startDateTime: startDateTime2,
          endDateTime: endDateTime2,
          dayOfWeek: ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][this.selectedDay.getDay()],
          isException: false
        });
      }

      return newAvailabilities;
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
