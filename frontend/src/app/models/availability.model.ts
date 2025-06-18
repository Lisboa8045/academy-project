export interface AvailabilityModel {
  id?: number;
  memberId?: number;
  dayOfWeek: string;         // "MONDAY", "TUESDAY", ...
  startDateTime: string;     // ISO string
  endDateTime: string;       // ISO string
}
