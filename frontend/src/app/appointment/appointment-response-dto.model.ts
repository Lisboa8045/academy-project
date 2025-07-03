import {AppointmentStatusEnumModel} from './appointment-status.model';

export interface AppointmentResponseDetailedDTO {
  id: number;
  serviceProviderId: string;
  serviceProviderUsername: string;
  memberId: number;
  memberUsername: string;
  rating: number;
  comment: string;
  serviceName: string;
  startDateTime: Date;
  price: number;
  duration: number;
  status:AppointmentStatusEnumModel;
}

export interface AppointmentResponseDTO{
  id: number;
  serviceProviderUsername: string;
  memberUsername: string;
  serviceName: string;
  startDateTime: Date;
  status: AppointmentStatusEnumModel;
}

