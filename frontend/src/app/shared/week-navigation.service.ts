import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WeekNavigationService {
  private currentDate = new BehaviorSubject<Date>(new Date());
  currentDate$ = this.currentDate.asObservable();

  goToPreviousWeek() {
    const newDate = new Date(this.currentDate.value);
    newDate.setDate(newDate.getDate() - 7);
    this.currentDate.next(newDate);
  }

  goToNextWeek() {
    const newDate = new Date(this.currentDate.value);
    newDate.setDate(newDate.getDate() + 7);
    this.currentDate.next(newDate);
  }

  resetToCurrentWeek() {
    this.currentDate.next(new Date());
  }

  getWeekStart(date: Date): Date {
    const newDate = new Date(date);
    const day = newDate.getDay();
    const diff = newDate.getDate() - day + (day === 0 ? -6 : 1); // Assume semana começa na segunda-feira
    return new Date(newDate.setDate(diff));
  }

  getWeekEnd(date: Date): Date {
    const start = this.getWeekStart(date);
    const end = new Date(start);
    end.setDate(start.getDate() + 6);
    return end;
  }
}
