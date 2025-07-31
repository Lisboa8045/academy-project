import {Component, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import {AuthService} from './auth.service';
import {Router, RouterLink} from '@angular/router';
import {strongPasswordValidator} from '../shared/validators/password.validator';
import {noSpecialCharsValidator} from '../shared/validators/no-special-chars.validator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarSuccess} from '../shared/snackbar/snackbar-success';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent{
  readonly isLoginMode = signal(true);

  authForm!: FormGroup;
  private readonly fb = inject(FormBuilder);

  errorMessage = signal('');
  passwordVisible = signal(false);
  confirmPasswordVisible = signal(false);
  loading = signal(false);

  constructor(private readonly authService: AuthService, private readonly router: Router, private readonly snackBar: MatSnackBar) {
    this.buildForm()
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

  toggleMode(): void {
    this.isLoginMode.update(mode => !mode);
    this.errorMessage.set('');
    this.passwordVisible.set(false);
    this.confirmPasswordVisible.set(false);
    this.buildForm();
  }

  private buildForm(): void {
    if (this.isLoginMode()) {
      this.authForm = this.fb.group({
        login: ['', [Validators.required, Validators.maxLength(254)]],
        password: ['', [Validators.required, Validators.maxLength(64)]]
      });
    } else {
      this.authForm = this.fb.group({
        email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
        username: ['', [Validators.required, noSpecialCharsValidator(), Validators.minLength(4), Validators.maxLength(20)]],
        password: ['', [Validators.required, strongPasswordValidator(), Validators.minLength(8), Validators.maxLength(64)]],
        confirmPassword: ['', [Validators.required, Validators.maxLength(64)]],
        agreedToTerms: [false, Validators.requiredTrue],
        agreedToPrivacy: [false, Validators.requiredTrue]
      }, { validators: this.passwordsMatchValidator });
    }

    this.authForm.valueChanges.subscribe(() => {
      this.errorMessage.set('');
    });
  }

  private passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;

    return password === confirm ? null : { passwordsMismatch: true };
  }

  private getHttpErrors(error: any): any[] | null {
    return error?.error?.errors ?? null;
  }

  submit(): void {
    const { login, email, username, password } = this.authForm.value;

    if (!this.authForm.valid) return;

    this.loading.set(true);

    if (this.isLoginMode()) {
      this.authService.login(login, password).subscribe({
        next: () => {
          const redirectUrl = localStorage.getItem('redirectAfterLogin');
          if (redirectUrl) {
            localStorage.removeItem('redirectAfterLogin');
            this.router.navigateByUrl(redirectUrl);
          } else {
            this.router.navigate(['/']);
          }
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          if (err?.type === 'EMAIL_NOT_CONFIRMED') {
            sessionStorage.setItem('pendingResendEmail', err.email ?? login);
            this.router.navigate(['/resend-email']);
            return;
          }
          console.error('Login failed:', err);
          this.errorMessage.set(err?.error ?? 'Login failed. Please try again.');
        }
      });
    } else {
      this.authService.signup(email, username, "2", password).subscribe({
        next: () => {
          this.loading.set(false);
          sessionStorage.setItem('signupConfirmEmail', email);
          this.router.navigate(['/auth/confirm-prompt']);
        },
        error: (err) => {
          console.error('Signup failed:', err);
          this.loading.set(false);

          this.errorMessage.set('');
          const errors = this.getHttpErrors(err);

          if (errors) {
            for (const field in errors) {
              if (this.authForm.controls[field]) {
                this.authForm.get(field)?.setErrors({ serverError: errors[field] });
              }
            }
          } else {
            this.errorMessage.set('Signup failed. Please try again.'); // Fallback error message
          }
        }
      });
    }
  }
}
