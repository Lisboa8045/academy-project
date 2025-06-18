export interface ServiceModel {
  id: number;
  name: string;
  description: string;
  ownerId: number;
  price: number;
  discount: number;
  negotiable: boolean;
  duration: number;
  permissions: string[];
  serviceType: string;
  serviceTypeId: number;
  tagNames: string[];
  createdAt: string;
  updatedAt: string;
}
