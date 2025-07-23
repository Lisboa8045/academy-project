import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function intRangeValidator(min: number = -2147483648, max: number = 2147483647): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;

    if (value === null || value === undefined || value === '') return null;

    const num = Number(value);

    if (!Number.isInteger(num)) return { notAnInteger: true };
    if (num < min || num > max) return { intOutOfRange: true };

    return null;
  };
}
