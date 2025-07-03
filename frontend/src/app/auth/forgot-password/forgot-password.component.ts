import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);

  forgotForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  resetSent = false;
  loading = false;
  message = '';
  error = '';

  submit() {
    if (this.forgotForm.invalid) return;

    this.loading = true;
    this.error = '';
    this.message = '';

    const email = this.forgotForm.value.email;

    this.authService.requestPasswordReset(email).subscribe({
      next: () => {
        this.loading = false;
        this.message = `If an account with "${email}" exists, a reset link was sent.`;
        this.resetSent = true;
      },
      error: (err) => {
        if (err.status === 404) {
          // Email not found — still show success message
          this.message = `If an account with "${email}" exists, a reset link was sent.`;
          this.loading = false;
          this.resetSent = true;
        }
        else {
          this.loading = false;
          this.error = 'Failed to send reset link. Please try again later.';
          console.error(err);
        }
      }
    });
  }
}
