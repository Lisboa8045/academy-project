export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'FINISHED';

export interface AppointmentModel {
  serviceProviderId: number;
  startDateTime: string;
  endDateTime: string;
  rating?: number;
  comment?: string;
  id?: number;
  status?: AppointmentStatus;
}
