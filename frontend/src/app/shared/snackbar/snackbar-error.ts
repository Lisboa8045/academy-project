import {MatSnackBar} from '@angular/material/snack-bar';

export function snackBarError(snackBar: MatSnackBar, message: string) {
  snackBar.open(message, 'Close', {
    duration: 3000,
    panelClass: ['error-snackbar'],
    horizontalPosition: 'right',
    verticalPosition: 'top'
  });
}
