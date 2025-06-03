import {Component, inject, OnInit, signal} from '@angular/core';
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


  constructor(private authService: AuthService, private router: Router) {
    this.buildForm()
  }


  toggleMode(): void {
    this.isLoginMode.update(mode => !mode);
    this.buildForm()
  }

  private buildForm(): void {
    if (this.isLoginMode()) {
      this.authForm = this.fb.group({
        login: ['', [Validators.required]],
        password: ['', [Validators.required]]
      });
    } else {
      this.authForm = this.fb.group({
        email: ['', [Validators.required, Validators.email]],
        username: ['', [Validators.required, noSpecialCharsValidator()]],
        password: ['', [Validators.required, strongPasswordValidator()]],
        confirmPassword: ['', [Validators.required]]
      }, { validators: this.passwordsMatchValidator });
    }
  }

  private passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;

    return password === confirm ? null : { passwordsMismatch: true };
  }

  submit(): void {
    const { login, email, username, password } = this.authForm.value;

    if (!this.authForm.valid) return;

    if (this.isLoginMode()) {
      this.authService.login(login!, password!).subscribe({
        next: () => this.router.navigate(['/']),
        error: (err) => console.error('Login failed:', err)
      });
    } else {
      this.authService.signup(email!, username!, "2", password!).subscribe({
        next: () => {
          alert('Signup successful! Please log in.');
          this.toggleMode()
        },
        error: (err) => console.error('Signup failed:', err)
      });
    }
  }


}
