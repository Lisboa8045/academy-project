import { Component, inject, signal } from '@angular/core';
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

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent {
  resetForm!: FormGroup;
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  loading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');
  passwordVisible = signal(false);
  confirmPasswordVisible = signal(false);
  resetSuccessful = false;

  // Extract token from URL params
  token = this.route.snapshot.paramMap.get('token') || '';

  constructor() {
    this.buildForm();
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

    fakeResetPasswordApi(this.token, password)
      .then(() => {
        this.loading.set(false);
        this.resetSuccessful = true;
        this.successMessage.set('Password reset successful! You can now log in.');
      })
      .catch((err: any) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error || 'Failed to reset password. Please try again.');
      });
  }
}

function fakeResetPasswordApi(token: string, password: string): Promise<void> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (token && password) resolve();
      else reject({ error: 'Invalid token or password' });
    }, 1000);
  });
}
