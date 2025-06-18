import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { AvailabilityService } from './availability-management.service';
import { AvailabilityModel } from '../../models/availability.model';
import { AuthStore } from '../../auth/auth.store';
import { FormsModule } from '@angular/forms';

// Novo tipo para criação (sem id)
interface AvailabilityCreateModel {
  startDateTime: string;
  endDateTime: string;
  dayOfWeek: string;
}

@Component({
  selector: 'app-availability-selection',
  templateUrl: 'availability-management.component.html',
  styleUrls: ['availability-management.component.css'],
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule],
})
export class AvailabilityManagementComponent implements OnInit {

  availabilities: AvailabilityModel[] = [];
  currentWeekStart!: Date;
  currentWeekEnd!: Date;

  // MODAL
  showAvailabilityModal = false;
  modalDay!: Date;
  modalStartTime: string = '09:00';
  modalEndTime: string = '17:00';
  modalMode: 'add' | 'edit' = 'add';
  editingAvailability: AvailabilityModel | null = null;
  applyType: 'all-year' | 'some-months' | 'one-day' = 'one-day';
  selectedMonths: number[] = [];

  monthsList = [
    { value: 0, label: 'Jan' }, { value: 1, label: 'Fev' }, { value: 2, label: 'Mar' }, { value: 3, label: 'Abr' },
    { value: 4, label: 'Mai' }, { value: 5, label: 'Jun' }, { value: 6, label: 'Jul' }, { value: 7, label: 'Ago' },
    { value: 8, label: 'Set' }, { value: 9, label: 'Out' }, { value: 10, label: 'Nov' }, { value: 11, label: 'Dez' },
  ];

  constructor(
    private availabilityService: AvailabilityService,
    private authStore: AuthStore
  ) {}

  ngOnInit() {
    this.initCurrentWeek();

    const memberId = this.authStore.id();
    if (memberId && memberId > 0) {
      this.loadAvailabilitiesForMember(memberId);
    } else {
      alert("Usuário não autenticado.");
    }
  }

  private initCurrentWeek() {
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

  private loadAvailabilitiesForMember(memberId: number) {
    this.availabilityService.getAvailabilitiesByWorker(memberId)
      .subscribe(avail => this.availabilities = avail);
  }

  getWeekDays(): Date[] {
    const days = [];
    for (let i = 0; i < 7; i++) {
      const day = new Date(this.currentWeekStart.getTime());
      day.setDate(this.currentWeekStart.getDate() + i);
      days.push(day);
    }
    return days;
  }

  getAvailabilityForDay(day: Date): AvailabilityModel[] {
    return this.availabilities.filter(a => this.dateMatchesDay(a, day));
  }

  private dateMatchesDay(avail: AvailabilityModel, day: Date): boolean {
    const availDate = new Date(avail.startDateTime).toISOString().split('T')[0];
    const dayKey = day.toISOString().split('T')[0];
    return availDate === dayKey;
  }

  goToPreviousWeek() {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() - 7);
    this.currentWeekEnd.setDate(this.currentWeekEnd.getDate() - 7);
  }

  goToNextWeek() {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + 7);
    this.currentWeekEnd.setDate(this.currentWeekEnd.getDate() + 7);
  }

  addAvailability(day: Date) {
    this.showAvailabilityModal = true;
    this.modalDay = day;
    this.modalStartTime = '09:00';
    this.modalEndTime = '17:00';
    this.modalMode = 'add';
    this.applyType = 'one-day';
    this.selectedMonths = [];
    this.editingAvailability = null;
  }

  editAvailability(avail: AvailabilityModel) {
    this.showAvailabilityModal = true;
    this.modalMode = 'edit';
    this.editingAvailability = avail;
    this.modalDay = new Date(avail.startDateTime);
    this.modalStartTime = this.modalDay.toTimeString().slice(0, 5);
    this.modalEndTime = new Date(avail.endDateTime).toTimeString().slice(0, 5);
    this.applyType = 'one-day';
    this.selectedMonths = [];
  }

  closeAvailabilityModal() {
    this.showAvailabilityModal = false;
    this.editingAvailability = null;
  }

  onApplyTypeChange() {
    if (this.applyType !== 'some-months') {
      this.selectedMonths = [];
    }
  }

  generateAvailabilities(): AvailabilityCreateModel[] {
    const [startHour, startMinute] = this.modalStartTime.split(':').map(Number);
    const [endHour, endMinute] = this.modalEndTime.split(':').map(Number);
    let availabilitiesToCreate: AvailabilityCreateModel[] = [];

    const getDayOfWeekString = (date: Date): string => {
      const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
      return days[date.getDay()];
    };

    if (this.applyType === 'one-day') {
      const date = new Date(this.modalDay);
      let start = new Date(date); start.setHours(startHour, startMinute, 0, 0);
      let end = new Date(date); end.setHours(endHour, endMinute, 0, 0);
      availabilitiesToCreate.push({
        dayOfWeek: getDayOfWeekString(date),
        startDateTime: start.toISOString(),
        endDateTime: end.toISOString(),
      });
    } else if (this.applyType === 'all-year') {
      for (let month = 0; month < 12; month++) {
        const date = new Date(this.modalDay);
        date.setMonth(month);
        let start = new Date(date); start.setHours(startHour, startMinute, 0, 0);
        let end = new Date(date); end.setHours(endHour, endMinute, 0, 0);
        availabilitiesToCreate.push({
          dayOfWeek: getDayOfWeekString(date),
          startDateTime: start.toISOString(),
          endDateTime: end.toISOString(),
        });
      }
    } else if (this.applyType === 'some-months') {
      for (const m of this.selectedMonths) {
        const date = new Date(this.modalDay);
        date.setMonth(m);
        let start = new Date(date); start.setHours(startHour, startMinute, 0, 0);
        let end = new Date(date); end.setHours(endHour, endMinute, 0, 0);
        availabilitiesToCreate.push({
          dayOfWeek: getDayOfWeekString(date),
          startDateTime: start.toISOString(),
          endDateTime: end.toISOString(),
        });
      }
    }

    return availabilitiesToCreate;
  }


  saveAvailability() {
    let availabilitiesToCreate = this.generateAvailabilities();
    const memberId = this.authStore.id();
    if (memberId) {
      this.availabilityService.createAvailabilities(memberId, availabilitiesToCreate).subscribe(() => {
        this.closeAvailabilityModal();
        this.loadAvailabilitiesForMember(memberId);
      });
    }
  }

  removeAvailability(avail: AvailabilityModel) {
    this.availabilities = this.availabilities.filter(a => a.id !== avail.id);
  }

}
