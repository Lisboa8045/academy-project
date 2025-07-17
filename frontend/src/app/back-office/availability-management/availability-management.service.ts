import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {catchError, Observable, of, throwError} from 'rxjs';
import { AvailabilityModel } from '../../models/availability.model';

@Injectable({ providedIn: 'root' })
export class AvailabilityService {
  private BASE_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  // Get all availabilities for a worker (both defaults and exceptions)
  getAvailabilitiesByWorker(memberId: number): Observable<AvailabilityModel[]> {
    return this.http.get<AvailabilityModel[]>(
      `${this.BASE_URL}/availabilities/members/${memberId}`
    );
  }

  saveDefaultAvailability(
    memberId: number,
    days: string[],
    morningStart: string,
    morningEnd: string,
    afternoonStart: string,
    afternoonEnd: string
  ): Observable<any> {
    return this.http.post(
      `${this.BASE_URL}/availabilities/members/${memberId}/default`,
      {
        days: days.map(day => this.mapDayStringToDayOfWeek(day)),
        morningStartTime: morningStart,
        morningEndTime: morningEnd,
        afternoonStartTime: afternoonStart,
        afternoonEndTime: afternoonEnd
      }
    );
  }
  // In availability.service.ts
  hasDefaultAvailability(memberId: number): Observable<boolean> {
    if (memberId <= 0) {
      return throwError(() => new Error('Invalid member ID'));
    }
    return this.http.get<boolean>(
      `${this.BASE_URL}/availabilities/members/${memberId}/has-default`
    ).pipe(
      catchError(err => {
        if (err.status === 404) {
          return of(false); // Treat 404 as "no defaults exist"
        }
        return throwError(() => err);
      })
    );
  }

  // Create exception for specific date
  createException(memberId: number, availability: AvailabilityModel): Observable<AvailabilityModel> {
    return this.http.post<AvailabilityModel>(
      `${this.BASE_URL}/availabilities/members/${memberId}/exceptions`,
      {
        ...availability,
        memberId: memberId,
        isException: true
      }
    );
  }

  deleteAvailability(availabilityId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.BASE_URL}/availabilities/${availabilityId}`
    );
  }

  private mapDayStringToDayOfWeek(day: string): string {
    // Map your frontend day strings to backend DayOfWeek enum format
    // Example: "MONDAY" -> "MONDAY" (no change if already matching)
    // Or adjust if your frontend uses different format
    return day.toUpperCase();
  }
}
