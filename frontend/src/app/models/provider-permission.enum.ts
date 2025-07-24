export enum ProviderPermissionEnumModel {
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  SERVE = 'SERVE',
  UPDATE_PERMISSIONS = 'UPDATE_PERMISSIONS',
  ADD_SERVICE_PROVIDER = 'ADD_SERVICE_PROVIDER',
  OWNER = 'OWNER'
}

export function getProviderPermissionEnumLabel(permission: ProviderPermissionEnumModel): string {
  let label = permission.charAt(0).toUpperCase() + permission.slice(1).toLowerCase();
  return label.replace( /_/g, ' ');
}
