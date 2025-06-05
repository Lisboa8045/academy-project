import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function noSpecialCharsValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;

    if (!value) {
      return null;
    }

    const isValid = /^[a-zA-Z0-9_]+$/.test(value);

    return isValid ? null : { specialCharsNotAllowed: true };
  };
}
