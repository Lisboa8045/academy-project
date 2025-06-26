import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AvailabilityModel } from '../../models/availability.model';

@Injectable({ providedIn: 'root' })
export class AvailabilityService {
  private BASE_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  // Get all availabilities for a worker
  getAvailabilitiesByWorker(memberId: number): Observable<AvailabilityModel[]> {
    return this.http.get<AvailabilityModel[]>(
      `${this.BASE_URL}/availabilities/members/${memberId}`
    );
  }

  // Create multiple availabilities
  createAvailabilities(memberId: number, availabilities: AvailabilityModel[]): Observable<any> {
    return this.http.post(
      `${this.BASE_URL}/availabilities/member/${memberId}`,
      availabilities
    );
  }

  // Update an availability
  updateAvailability(availability: AvailabilityModel): Observable<AvailabilityModel> {
    return this.http.put<AvailabilityModel>(
      `${this.BASE_URL}/availabilities/${availability.id}`,
      {
        startDateTime: availability.startDateTime,
        endDateTime: availability.endDateTime,
        dayOfWeek: availability.dayOfWeek
      }
    );
  }

  // Delete an availability
  deleteAvailability(availabilityId: number): Observable<void> {
    if (typeof availabilityId !== 'number') {
      throw new Error('Invalid availability ID');
    }
    return this.http.delete<void>(
      `${this.BASE_URL}/availabilities/${availabilityId}`
    );
  }

  // Create a single availability
  createAvailability(memberId: number, availability: AvailabilityModel): Observable<AvailabilityModel> {
    return this.http.post<AvailabilityModel>(
      `${this.BASE_URL}/availabilities/member/${memberId}`,
      [availability] // Send as array to match existing endpoint
    );
  }
}
