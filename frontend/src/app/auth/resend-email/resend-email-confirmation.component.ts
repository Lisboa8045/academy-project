import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from "../auth.service";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-confirm-email',
  templateUrl: './resend-email-confirmation.component.html',
  imports: [CommonModule],
  styleUrls: ['./resend-email-confirmation.component.css']
})
export class ResendEmailConfirmationComponent implements OnInit {
  email: string = '';
  message: string = '';
  error: string = '';
  resendDisabled: boolean = false;
  showBackToLogin: boolean = false;
  loading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.email = params['email'] || '';
    });
  }

  resend(): void {
    if (!this.email || this.resendDisabled) return;

    this.loading = true;
    this.resendDisabled = true;
    this.authService.resendConfirmation(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.message = 'Verification email sent!';
        this.error = '';
        this.showBackToLogin = true;
      },
      error: () => {
        this.loading = false;
        this.message = '';
        this.error = 'Failed to resend verification. Please try again later.';
        this.showBackToLogin = true;
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/auth']);
  }
}
