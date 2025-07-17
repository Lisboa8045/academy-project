import {Component, inject, OnInit, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {strongPasswordValidator} from '../../shared/validators/password.validator';
import {AuthService} from '../auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  resetForm!: FormGroup;
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  loading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');
  passwordVisible = signal(false);
  confirmPasswordVisible = signal(false);
  resetSuccessful = signal(false);
  isTokenVerified = signal(false);

  token: string = '';

  constructor() {
    this.buildForm();
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token') || '';
    if (!this.token) {
      this.errorMessage.set('Invalid reset link.');
      this.loading.set(false);
      return;
    }

    this.authService.verifyResetToken(this.token).subscribe({
      next: () => {
        this.loading.set(false);
        this.isTokenVerified.set(true);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 400) {
          this.errorMessage.set('This reset link is invalid.');
        } else if (err.status === 410) {
          this.errorMessage.set('This reset link has expired. Please request a new one.');
        } else {
          this.errorMessage.set('An unexpected error occurred.');
        }
      }
    });
  }

  private buildForm(): void {
    this.resetForm = this.fb.group({
      password: ['', [Validators.required, strongPasswordValidator(), Validators.minLength(8), Validators.maxLength(64)]],
      confirmPassword: ['', [Validators.required, Validators.maxLength(64)]]
    }, { validators: this.passwordsMatchValidator });
  }

  private passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return password === confirm ? null : { passwordsMismatch: true };
  }

  togglePasswordVisibility(event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.passwordVisible.update(v => !v);
  }

  toggleConfirmPasswordVisibility(event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.confirmPasswordVisible.update(v => !v);
  }

  goToLogin() {
    this.router.navigate(['/auth']);
  }

  submit(): void {
    if (!this.resetForm.valid) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const { password } = this.resetForm.value;

    this.authService.resetPassword(this.token, password).subscribe({
      next: () => {
        this.successMessage.set('Password reset successful! You can now log in.');
        this.loading.set(false);
        this.resetSuccessful.set(true);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error || 'Failed to reset password. Please try again.');
      }
    });
  }

  goToForgotPassword() {
    this.router.navigate(['/forgot-password']);
  }
}
