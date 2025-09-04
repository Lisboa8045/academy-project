import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'shiftArray' })
export class ShiftArrayPipe implements PipeTransform {
  transform<T>(array: T[], shift: number): T[] {
    if (!array || array.length === 0) return [];
    const actualShift = shift % array.length;
    return array.slice(actualShift).concat(array.slice(0, actualShift));
  }
}
