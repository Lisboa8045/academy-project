import { MemberStatusEnum } from '../models/member-status-enum.model';

export interface MemberResponseDTO {
  id: number;
  username: string;
  email: string;
  address: string;
  postalCode: string;
  phoneNumber: string;
  role: string;
  profilePicture: string;
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
  status: MemberStatusEnum;
}
