import { Component, effect, inject, Injector, OnInit, runInInjectionContext } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AvailabilityService } from './availability-management.service';
import { AvailabilityModel } from '../../models/availability.model';
import { AuthStore } from '../../auth/auth.store';
import { WeekNavigationService } from '../../shared/week-navigation.service';
import { addDays, startOfWeek, endOfWeek, isSameDay, parseISO, format } from 'date-fns';
import { AvailabilityHeaderComponent } from './availability-header/availability-header.component';
import { AvailabilityDayColumnComponent } from './availability-day-column/availability-day-column.component';
import { AvailabilityFormModalComponent } from './availability-form-modal/availability-form-modal.component';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-availability-management',
  templateUrl: './availability-management.component.html',
  styleUrls: ['./availability-management.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    AvailabilityHeaderComponent,
    AvailabilityDayColumnComponent,
    AvailabilityFormModalComponent,
  ]
})
export class AvailabilityManagementComponent implements OnInit {
  availabilities: AvailabilityModel[] = [];
  currentWeekStart: Date = new Date();
  currentWeekEnd: Date = new Date();
  today = new Date();
  isSettingDefaults = true;
  hasDefaults = false;

  // State for modals
  showFormModal = false;
  formMode: 'add' | 'edit' = 'add';
  selectedDay: Date | null = null;

  // Local changes
  localChanges: AvailabilityModel[] = [];
  deletedIds: number[] = [];

  private readonly injector = inject(Injector);

  constructor(
    private readonly availabilityService: AvailabilityService,
    private readonly authStore: AuthStore,
    private readonly weekNavigationService: WeekNavigationService
  ) {}

  ngOnInit(): void {
    runInInjectionContext(this.injector, () => {
      effect(() => {
        const userId = this.authStore.id();
        if (userId > 0) {
          this.availabilityService.hasDefaultAvailability(userId).subscribe({
            next: (hasDefaults) => {
              this.hasDefaults = hasDefaults;
              this.isSettingDefaults = !hasDefaults;

              // Set initial week and load availabilities for today
              this.setWeekFromDate(this.today);
              this.loadAvailabilities();

              // Subscribe to week navigation changes
              this.weekNavigationService.currentDate$.subscribe(date => {
                this.setWeekFromDate(date);
                this.loadAvailabilities();
              });
            },
            error: (err) => {
              console.error('Error checking default availability:', err);
            }
          });
        }
      });
    });
  }

  // Public methods used by template and child components
  getWeekDays(): Date[] {
    return Array.from({ length: 7 }, (_, i) => addDays(this.currentWeekStart, i));
  }

    getAvailabilityForDay(day: Date): AvailabilityModel[] {
    // If setting up defaults, only show intervals from localChanges
    if (this.isSettingDefaults) {
      return this.localChanges.filter(a =>
        isSameDay(parseISO(a.startDateTime), day) && !a.isException
      ).sort((a, b) =>
        parseISO(a.startDateTime).getTime() - parseISO(b.startDateTime).getTime()
      );
    }

    // Gather local exceptions for the day
    const localExceptions = this.localChanges.filter(a =>
      isSameDay(parseISO(a.startDateTime), day) && a.isException
    );

    // Gather backend exceptions for the day, excluding those that overlap with local exceptions
    const backendExceptions = this.availabilities.filter(a =>
      isSameDay(parseISO(a.startDateTime), day) &&
      a.isException &&
      !this.deletedIds.includes(a.id as number) &&
      !localExceptions.some(exc =>
        exc.startDateTime === a.startDateTime && exc.endDateTime === a.endDateTime
      )
    );

    // Get default intervals for the day
    const defaults = this.hasDefaults ? this.getDefaultIntervalsForDay(day) : [];

    // If there are any exceptions for the day, show only exceptions (local + backend, excluding deleted)
    const allExceptions = [
      ...localExceptions,
      ...backendExceptions
    ];
    if (allExceptions.length > 0) {
      return allExceptions.sort((a, b) =>
        parseISO(a.startDateTime).getTime() - parseISO(b.startDateTime).getTime()
      );
    }

    // Otherwise, show defaults
    return defaults.sort((a, b) =>
      parseISO(a.startDateTime).getTime() - parseISO(b.startDateTime).getTime()
    );
    }

  isToday(day: Date): boolean {
    return isSameDay(day, this.today);
  }

  // Child component event handlers
  onAddSlot(day: Date): void {
    this.selectedDay = day;
    this.formMode = 'add';
    this.showFormModal = true;
  }

  onEditSlot(avail: AvailabilityModel): void {
    this.selectedDay = parseISO(avail.startDateTime);
    this.formMode = 'edit';
    this.showFormModal = true;
  }

  onDeleteSlot(avail: AvailabilityModel): void {
    this.removeAvailability(avail);
  }

  onSaveChanges(): void {
    this.saveAllChanges();
  }

  onFormSubmit(availabilities: AvailabilityModel[]): void {
    if (this.selectedDay) {
      // Remove any local changes for the selected day
      this.localChanges = [
        ...this.localChanges.filter(a => {
          const availDate = parseISO(a.startDateTime);
          return !isSameDay(availDate, this.selectedDay!);
        }),
        ...availabilities.map(avail => ({
          ...avail,
          id: avail.id ?? this.generateTemporaryId(),
          memberId: this.authStore.id(),
          isException: !this.isSettingDefaults
        }))
      ];

      // If editing a default, immediately update the view so only exceptions show for that day
      this.updateView();
    }
    this.showFormModal = false;
  }

  // Private methods
  private checkExistingDefaults(): void {
    const memberId = this.authStore.id();
    if (!memberId || memberId <= 0) {
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
      }
    });
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

  private getDefaultIntervalsForDay(day: Date): AvailabilityModel[] {
    const dayName = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][day.getDay()];

    const defaultIntervals: AvailabilityModel[] = [];

    // Morning interval (09:00-12:00)
    defaultIntervals.push({
      startDateTime: this.combineDateAndTime(day, '09:00'),
      endDateTime: this.combineDateAndTime(day, '12:00'),
      dayOfWeek: dayName,
      isException: false,
      id: undefined,
      memberId: this.authStore.id()
    });

    // Afternoon interval (13:00-17:00)
    defaultIntervals.push({
      startDateTime: this.combineDateAndTime(day, '13:00'),
      endDateTime: this.combineDateAndTime(day, '17:00'),
      dayOfWeek: dayName,
      isException: false,
      id: undefined,
      memberId: this.authStore.id()
    });

    return defaultIntervals;
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

  private removeAvailability(avail: AvailabilityModel): void {
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

  private saveAllChanges(): void {
    const memberId = this.authStore.id();
    if (!memberId || memberId <= 0) {
      console.error('Invalid member ID');
      return;
    }

    if (this.isSettingDefaults) {
      // Save default template
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
        },
        error: (err) => {
          console.error('Error saving changes:', err);
        }
      });
    }
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

  private generateTemporaryId(): number {
    return Math.floor(Math.random() * -1000000);
  }

  private resetLocalState(): void {
    this.localChanges = [];
    this.deletedIds = [];
  }
}
