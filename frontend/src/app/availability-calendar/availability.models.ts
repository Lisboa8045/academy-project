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
  memberUsername: string;
  serviceName: string;
  startDateTime: string;
  endDateTime: string;
  status: AppointmentStatus;
}
