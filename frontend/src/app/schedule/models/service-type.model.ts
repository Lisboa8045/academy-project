import {ServiceModel} from '../../service/service.model';

export interface ServiceTypeModel {
  id: number;
  name: string;
  icon: string;
  services: ServiceModel[];
  createdAt: string; // ISO datetime string
  updatedAt: string; // ISO datetime string
}
