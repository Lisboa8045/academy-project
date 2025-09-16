export type ServiceStatus = 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED';

export interface ServiceModel {
  id: number;
  name: string;
  description: string;
  ownerId: number;
  price: number;
  discount: number;
  negotiable: boolean;
  duration: number;
  entity: string;
  permissions: string[];
  status:  ServiceStatus;
  serviceTypeName: string;
  tagNames: string[];
  createdAt: string;
  updatedAt: string;
  images: string[];
  rating: number;
}
