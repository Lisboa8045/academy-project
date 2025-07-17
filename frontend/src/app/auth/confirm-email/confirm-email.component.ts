import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-confirm-email',
  templateUrl: './confirm-email.component.html',
  styleUrls: ['./confirm-email.component.css'],
  imports: [CommonModule],
})
export class ConfirmEmailComponent implements OnInit {
  message: string = '';
  error: string = '';
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token');
    if (!token) {
      this.error = 'Invalid verification link.';
      this.loading = false;
      return;
    }

    this.authService.confirmEmail(token).subscribe({
      next: () => {
        this.message = 'Your email has been successfully confirmed!';
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 400) {
          this.error = 'This confirmation link is invalid.';
        }
        else if (err.status === 410) {
          this.error = 'This confirmation link has expired. Please request a new one.'
        }
        else {
          this.error = 'An unexpected error occurred.';
        }
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/auth']);
  }
}
