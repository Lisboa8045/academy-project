import {AbstractControl, ValidationErrors} from '@angular/forms';

export function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const password = group.get('newPassword')?.value;
  const confirm = group.get('confirmPassword')?.value;

  if (!password || !confirm)
    return null;

    return password === confirm ? null : {passwordsMismatch: true};

}

