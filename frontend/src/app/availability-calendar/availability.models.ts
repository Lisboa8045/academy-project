export interface DateTimeRange {
  start: string; // "HH:mm"
  end: string;   // "HH:mm"
}

export interface DaySchedule {
  date: string; // "YYYY-MM-DD"
  timeRanges: DateTimeRange[];
}

export interface AvailabilityRequestNewDTO {
  daySchedules: DaySchedule[];
}
