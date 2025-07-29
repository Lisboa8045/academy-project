
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
  serviceTypeName: string;
  tagNames: string[];
  createdAt: string;
  updatedAt: string;
  images: string[];
  rating: number;
}
