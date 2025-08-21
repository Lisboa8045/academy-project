import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-resend-account-deletion-email',
  templateUrl: './resend-account-deletion-email.component.html',
  imports: [CommonModule],
  styleUrls: ['./resend-account-deletion-email.component.css'],
})
export class ResendAccountDeletionEmailComponent implements OnInit {
  email: string = '';
  message: string = '';
  error: string = '';
  resendDisabled: boolean = false;
  showBackToLogin: boolean = false;
  loading: boolean = false;

  constructor(
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    const email = sessionStorage.getItem('pendingDeletionEmail');
    if (!email) {
      this.router.navigate(['/auth']);
      return;
    }

    this.email = email;
  }

  resend(): void {
    if (!this.email || this.resendDisabled) return;

    this.loading = true;
    this.resendDisabled = true;
    this.authService.resendDeleteAccountEmail(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.message = 'Account deletion email sent!';
        this.error = '';
        this.showBackToLogin = true;
        sessionStorage.removeItem('pendingCancelEmail');
      },
      error: (err: any) => {
        this.loading = false;
        this.message = '';
        if (err?.error) {
          this.error = err.error;
        } else {
          this.error = 'Failed to resend account deletion email. Please try again later.';
        }
        this.showBackToLogin = true;
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/auth']);
  }
}
