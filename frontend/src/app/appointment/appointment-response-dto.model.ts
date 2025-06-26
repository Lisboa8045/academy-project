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
}

export interface AppointmentResponseDTO{
  id: number;
  serviceProviderUsername: string;
  memberUsername: string;
  serviceName: string;
  startDateTime: Date;
}

