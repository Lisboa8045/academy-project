// countdown-snackbar.component.ts
import { Component, Inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';

@Component({
  selector: 'app-countdown-snackbar',
  template: `<span class="countdown-snackbar">{{ data.message }}</span>`,
  styles: [`.countdown-snackbar { color: white; }`]
})
export class CountdownSnackbarComponent {
  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: { message: string }) {}
}
