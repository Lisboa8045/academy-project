import {ProviderPermissionEnumModel} from '../../../models/provider-permission.enum';

export interface UpdatePermissionsRequest {
  permissions: ProviderPermissionEnumModel[];
  memberId: number;
}
