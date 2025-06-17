import {MatSnackBar} from '@angular/material/snack-bar';

export function snackBarSuccess(snackBar: MatSnackBar, message: string) {
  snackBar.open(message, 'Close', {
    duration: 3000,
    panelClass: ['success-snackbar'],
    horizontalPosition: 'right',
    verticalPosition: 'top'
  });
}
