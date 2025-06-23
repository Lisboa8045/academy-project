import {Component, inject, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormGroup,
  AbstractControl,
  ValidationErrors
} from '@angular/forms';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import {strongPasswordValidator} from '../shared/validators/password.validator';
import {noSpecialCharsValidator} from '../shared/validators/no-special-chars.validator';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent{
  readonly isLoginMode = signal(true);

  authForm!: FormGroup;
  private fb = inject(FormBuilder);

  errorMessage = '';
  passwordVisible = false;
  confirmPasswordVisible = false;

  constructor(private authService: AuthService, private router: Router) {
    this.buildForm()
  }

  togglePasswordVisibility(event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.passwordVisible = !this.passwordVisible;
  }

  toggleConfirmPasswordVisibility(event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.confirmPasswordVisible = !this.confirmPasswordVisible;
  }

  toggleMode(): void {
    this.isLoginMode.update(mode => !mode);
    this.errorMessage = '';
    this.passwordVisible = false;
    this.confirmPasswordVisible = false;
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
        confirmPassword: ['', [Validators.required, Validators.maxLength(64)]]
      }, { validators: this.passwordsMatchValidator });
    }

    this.authForm.valueChanges.subscribe(() => {
      this.errorMessage = '';
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

    if (this.isLoginMode()) {
      this.authService.login(login!, password!).subscribe({
        next: () => this.router.navigate(['/']),
        error: (err) => {
          console.error('Login failed:', err);
          this.errorMessage = err?.error || 'Login failed. Please try again.';
        }
      });
    } else {
      this.authService.signup(email!, username!, "2", password!).subscribe({
        next: () => {
          alert('Signup successful! Please log in.');
          this.toggleMode()
        },
        error: (err) => {
          console.error('Signup failed:', err);

          this.errorMessage = '';
          const errors = this.getHttpErrors(err);

          if (errors) {
            for (const field in errors) {
              if (this.authForm.controls[field]) {
                this.authForm.get(field)?.setErrors({ serverError: errors[field] });
              }
            }
          } else {
            this.errorMessage = 'Signup failed. Please try again.'; // Fallback error message
          }
        }
      });
    }
  }
}
