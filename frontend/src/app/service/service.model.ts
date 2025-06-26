import {ServiceTypeModel} from '../models/service-type.model';

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
  serviceTypeName: ServiceTypeModel;
  tagNames: string[];
  createdAt: string;
  updatedAt: string;
  imageUrl: string
}
