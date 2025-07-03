import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router} from '@angular/router';

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

  forgotForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  resetSent = false;
  loading = false;
  message = '';

  submit() {
    if (this.forgotForm.invalid) return;

    this.loading = true;
    const email = this.forgotForm.value.email;

    setTimeout(() => {
      this.message = `If an account with "${email}" exists, a reset link was sent.`;
      this.resetSent = true;
      this.loading = false;
    }, 1000);
  }
}
