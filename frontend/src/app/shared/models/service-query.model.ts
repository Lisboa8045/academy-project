export interface ServiceQuery {
  page: number;
  pageSize: number;
  sortOrder: string;
  minPrice?: number;
  maxPrice?: number;
  minDuration?: number;
  maxDuration?: number;
  negotiable?: boolean;
  serviceTypeName?: string;
  enabled?: boolean;
  status?: string;
}
