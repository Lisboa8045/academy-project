import { AppointmentReviewModel } from './appointment-review.model';
import { ProviderPermissionEnumModel } from './provider-permission.enum';

export interface ServiceProviderModel {
  id: number;
  memberName: string;
  serviceId: number;
  appointmentReviewList: AppointmentReviewModel[];
  permissions: ProviderPermissionEnumModel[];
}
