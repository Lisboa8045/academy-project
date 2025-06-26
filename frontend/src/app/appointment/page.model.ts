export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
} //TODO trocar para utilizar a do Diogo?
