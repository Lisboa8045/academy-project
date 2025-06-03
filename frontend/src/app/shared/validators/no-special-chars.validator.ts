import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function noSpecialCharsValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;

    if (!value) {
      return null; // don't validate empty values (use `Validators.required` for that)
    }

    const isValid = /^[a-zA-Z0-9]+$/.test(value);

    return isValid ? null : { specialCharsNotAllowed: true };
  };
}
