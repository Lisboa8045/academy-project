export interface MemberResponseDTO {
  id: number;
  username: string;
  email: string;
  address: string;
  postalCode: string;
  phoneNumber: string;
  role: string;
  roleId: number;
  profilePicture: string;
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}
