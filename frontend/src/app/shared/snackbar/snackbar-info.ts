import {MatSnackBar} from '@angular/material/snack-bar';

export function snackBarInfo(snackBar: MatSnackBar, message: string) {
  snackBar.open(message, 'Close', {
    duration: 3000,
    panelClass: ['info-snackbar'],
    horizontalPosition: 'right',
    verticalPosition: 'top'
  });
}
