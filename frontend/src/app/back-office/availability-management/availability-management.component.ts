import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { AvailabilityService } from './availability-management.service';
import { AvailabilityModel } from '../../models/availability.model';
import { AuthStore } from '../../auth/auth.store';
import { FormsModule } from '@angular/forms';

interface AvailabilityCreateModel {
  startDateTime: string;
  endDateTime: string;
  dayOfWeek: string;
}

interface RecurrenceOption {
  value: 'one-day' | 'all-year' | 'some-months' | 'weekly';
  label: string;
}

interface DayOfWeekOption {
  value: number;
  label: string;
}

@Component({
  selector: 'app-availability-management',
  templateUrl: './availability-management.component.html',
  styleUrls: ['./availability-management.component.css'],
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule]
})
export class AvailabilityManagementComponent implements OnInit {
  availabilities: AvailabilityModel[] = [];
  currentWeekStart: Date = new Date();
  currentWeekEnd: Date = new Date();
  modalDay: Date = new Date();
  today = new Date();

  // Modal state
  showAvailabilityModal = false;
  modalStartTime = '09:00';
  modalEndTime = '17:00';
  modalMode: 'add' | 'edit' | 'bulk' = 'add';
  editingAvailability: AvailabilityModel | null = null;
  availabilityToDelete: AvailabilityModel | null = null;
  applyType: 'one-day' | 'all-year' | 'some-months' | 'weekly' = 'one-day';
  selectedMonths: number[] = [];
  selectedDays: number[] = [];
  showDeleteConfirmation = false;

  recurrenceOptions: RecurrenceOption[] = [
    { value: 'one-day', label: 'Apenas este dia' },
    { value: 'weekly', label: 'Toda semana' },
    { value: 'some-months', label: 'Meses específicos' },
    { value: 'all-year', label: 'Todo o ano' }
  ];

  monthsList = [
    { value: 0, label: 'Jan' }, { value: 1, label: 'Fev' }, { value: 2, label: 'Mar' },
    { value: 3, label: 'Abr' }, { value: 4, label: 'Mai' }, { value: 5, label: 'Jun' },
    { value: 6, label: 'Jul' }, { value: 7, label: 'Ago' }, { value: 8, label: 'Set' },
    { value: 9, label: 'Out' }, { value: 10, label: 'Nov' }, { value: 11, label: 'Dez' }
  ];

  daysOfWeek: DayOfWeekOption[] = [
    { value: 0, label: 'Dom' },
    { value: 1, label: 'Seg' },
    { value: 2, label: 'Ter' },
    { value: 3, label: 'Qua' },
    { value: 4, label: 'Qui' },
    { value: 5, label: 'Sex' },
    { value: 6, label: 'Sáb' }
  ];

  constructor(
    private availabilityService: AvailabilityService,
    private authStore: AuthStore
  ) {}

  ngOnInit(): void {
    this.initCurrentWeek();
    this.loadAvailabilities();
  }

  private initCurrentWeek(): void {
    const now = new Date();
    const day = now.getDay();
    const diffToMonday = (day === 0 ? -6 : 1) - day;
    this.currentWeekStart = new Date(now);
    this.currentWeekStart.setDate(now.getDate() + diffToMonday);
    this.currentWeekStart.setHours(0, 0, 0, 0);
    this.currentWeekEnd = new Date(this.currentWeekStart);
    this.currentWeekEnd.setDate(this.currentWeekStart.getDate() + 6);
    this.currentWeekEnd.setHours(23, 59, 59, 999);
  }

  private loadAvailabilities(): void {
    const memberId = this.authStore.id();
    if (memberId) {
      this.availabilityService.getAvailabilitiesByWorker(memberId)
        .subscribe(avail => this.availabilities = avail);
    }
  }

  getWeekDays(): Date[] {
    return Array.from({ length: 7 }, (_, i) => {
      const date = new Date(this.currentWeekStart);
      date.setDate(date.getDate() + i);
      return date;
    });
  }

  getAvailabilityForDay(day: Date): AvailabilityModel[] {
    const dayKey = day.toISOString().split('T')[0];
    return this.availabilities
      .filter(a => new Date(a.startDateTime).toISOString().split('T')[0] === dayKey)
      .sort((a, b) => new Date(a.startDateTime).getTime() - new Date(b.startDateTime).getTime());
  }

  isToday(day: Date): boolean {
    return day.toDateString() === this.today.toDateString();
  }

  isSlotBooked(avail: AvailabilityModel): boolean {
    // Implement actual booking check logic here
    return false;
  }

  // Week navigation
  goToPreviousWeek(): void {
    this.adjustWeek(-7);
  }

  goToNextWeek(): void {
    this.adjustWeek(7);
  }

  goToCurrentWeek(): void {
    this.initCurrentWeek();
    this.loadAvailabilities();
  }

  private adjustWeek(days: number): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + days);
    this.currentWeekEnd.setDate(this.currentWeekEnd.getDate() + days);
    this.loadAvailabilities();
  }

  // Availability CRUD
  addAvailability(day: Date): void {
    this.modalDay = new Date(day);
    this.modalMode = 'add';
    this.showAvailabilityModal = true;
  }

  editAvailability(avail: AvailabilityModel): void {
    this.editingAvailability = avail;
    this.modalDay = new Date(avail.startDateTime);
    this.modalStartTime = this.formatTime(new Date(avail.startDateTime));
    this.modalEndTime = this.formatTime(new Date(avail.endDateTime));
    this.modalMode = 'edit';
    this.showAvailabilityModal = true;
  }

  private formatTime(date: Date): string {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }

  removeAvailability(avail: AvailabilityModel): void {
    this.availabilityToDelete = avail;
    this.showDeleteConfirmation = true;
  }

  confirmDelete(): void {
    if (this.availabilityToDelete?.id) {
      this.availabilityService.deleteAvailability(this.availabilityToDelete.id)
        .subscribe({
          next: () => this.handleDeleteSuccess(),
          error: (err) => this.handleDeleteError(err)
        });
    }
  }

  private handleDeleteSuccess(): void {
    this.loadAvailabilities();
    this.showDeleteConfirmation = false;
    this.availabilityToDelete = null;
  }

  private handleDeleteError(error: any): void {
    console.error('Delete failed:', error);
    this.showDeleteConfirmation = false;
  }

  cancelDelete(): void {
    this.showDeleteConfirmation = false;
    this.availabilityToDelete = null;
  }

  // Modal helpers
  closeAvailabilityModal(): void {
    this.showAvailabilityModal = false;
    this.editingAvailability = null;
  }

  getModalTitle(): string {
    switch (this.modalMode) {
      case 'add': return 'Nova Disponibilidade';
      case 'edit': return 'Editar Disponibilidade';
      case 'bulk': return 'Adicionar em Massa';
      default: return 'Disponibilidade';
    }
  }

  onApplyTypeChange(): void {
    if (this.applyType !== 'some-months') {
      this.selectedMonths = [];
    }
  }

  toggleMonthSelection(month: number): void {
    const index = this.selectedMonths.indexOf(month);
    if (index === -1) {
      this.selectedMonths.push(month);
    } else {
      this.selectedMonths.splice(index, 1);
    }
  }

  toggleDaySelection(day: number): void {
    const index = this.selectedDays.indexOf(day);
    if (index === -1) {
      this.selectedDays.push(day);
    } else {
      this.selectedDays.splice(index, 1);
    }
  }

  saveAvailability(): void {
    const availabilities = this.generateAvailabilities();
    const memberId = this.authStore.id();

    if (!memberId) {
      console.error('User not authenticated');
      return;
    }

    if (this.modalMode === 'edit' && this.editingAvailability) {
      this.updateExistingAvailability();
    } else {
      this.createNewAvailabilities(memberId, availabilities);
    }
  }

  private updateExistingAvailability(): void {
    if (!this.editingAvailability) return;

    const updated: AvailabilityModel = {
      ...this.editingAvailability,
      startDateTime: this.combineDateAndTime(this.modalDay, this.modalStartTime),
      endDateTime: this.combineDateAndTime(this.modalDay, this.modalEndTime)
    };

    this.availabilityService.updateAvailability(updated)
      .subscribe(() => {
        this.closeAvailabilityModal();
        this.loadAvailabilities();
      });
  }

  private createNewAvailabilities(memberId: number, availabilities: AvailabilityCreateModel[]): void {
    this.availabilityService.createAvailabilities(memberId, availabilities)
      .subscribe(() => {
        this.closeAvailabilityModal();
        this.loadAvailabilities();
      });
  }

  private combineDateAndTime(date: Date, time: string): string {
    const [hours, minutes] = time.split(':').map(Number);
    const newDate = new Date(date);
    newDate.setHours(hours, minutes);
    return newDate.toISOString();
  }

  private generateAvailabilities(): AvailabilityCreateModel[] {
    const availabilities: AvailabilityCreateModel[] = [];

    if (this.modalMode === 'bulk') {
      this.generateBulkAvailabilities(availabilities);
    } else {
      this.generateStandardAvailabilities(availabilities);
    }

    return availabilities;
  }

  private generateBulkAvailabilities(availabilities: AvailabilityCreateModel[]): void {
    for (let week = 0; week < 12; week++) {
      this.selectedDays.forEach(dayValue => {
        const date = new Date(this.currentWeekStart);
        date.setDate(date.getDate() + dayValue + (week * 7));
        availabilities.push(this.createAvailability(date));
      });
    }
  }

  private generateStandardAvailabilities(availabilities: AvailabilityCreateModel[]): void {
    switch (this.applyType) {
      case 'one-day':
        availabilities.push(this.createAvailability(this.modalDay));
        break;
      case 'weekly':
        for (let week = 0; week < 12; week++) {
          const date = new Date(this.modalDay);
          date.setDate(date.getDate() + (week * 7));
          availabilities.push(this.createAvailability(date));
        }
        break;
      case 'all-year':
        for (let month = 0; month < 12; month++) {
          const date = new Date(this.modalDay);
          date.setMonth(month);
          availabilities.push(this.createAvailability(date));
        }
        break;
      case 'some-months':
        this.selectedMonths.forEach(month => {
          const date = new Date(this.modalDay);
          date.setMonth(month);
          availabilities.push(this.createAvailability(date));
        });
        break;
    }
  }

  openBulkAddModal(): void {
    this.showAvailabilityModal = true;
    this.modalDay = new Date(this.currentWeekStart);
    this.modalMode = 'bulk';
    this.applyType = 'weekly';
    this.selectedMonths = [];
    this.selectedDays = [1, 2, 3, 4, 5]; // Default: Monday to Friday
    this.editingAvailability = null;
  }

  private createAvailability(date: Date): AvailabilityCreateModel {
    return {
      dayOfWeek: ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][date.getDay()],
      startDateTime: this.combineDateAndTime(date, this.modalStartTime),
      endDateTime: this.combineDateAndTime(date, this.modalEndTime)
    };
  }
}
