import { AppointmentReviewModel } from './appointment-review.model';
import { ProviderPermissionEnumModel } from './provider-permission.enum';

export interface ServiceProviderModel {
  id: number;
  active: boolean;
  memberName: string;
  serviceId: number;
  appointmentReviewList: AppointmentReviewModel[];
  permissions: ProviderPermissionEnumModel[];
}

export interface ServiceProviderRequestDTO {
  memberId: number;
  serviceId: number;
  permissions: ProviderPermissionEnumModel[];
  isServiceCreation: boolean;
}
