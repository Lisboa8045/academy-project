import {AppointmentStatus} from '../models/appointment.model';

export interface DateTimeRange {
  start: string; // "HH:mm"
  end: string;   // "HH:mm"
}

export interface DaySchedule {
  date: string; // "YYYY-MM-DD"
  timeRanges: DateTimeRange[];
}

export interface AvailabilityDTO {
  daySchedules: DaySchedule[];
}

export interface AppointmentCalendarDTO{
  id: number;
  memberUsername: string;
  serviceName: string;
  startDateTime: string;
  endDateTime: string;
  price: number;
  duration: number;
  status: AppointmentStatus;
}
