export interface AppointmentModel {
  serviceProviderId: number;
  memberId?: number; // required now
  startDateTime: string;
  endDateTime: string;
  rating?: number;
  comment?: string;
  id?: number;       // optional, ignored by backend
  status?: string;   // optional, ignored by backend
}
