import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function strongPasswordValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const password: string = control.value;

    if (!password) return null;

    const hasDigit = /[0-9]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasUpper = /[A-Z]/.test(password);
    const hasSpecial = /[^a-zA-Z0-9]/.test(password);

    const valid = hasDigit && hasLower && hasUpper && hasSpecial;

    return valid ? null : { weakPassword: true };
  };
}
