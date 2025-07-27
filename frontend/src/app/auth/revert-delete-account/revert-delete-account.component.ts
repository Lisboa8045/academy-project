import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import { NgIf } from '@angular/common'; // <-- Import NgIf
import { ProfileService } from '../../profile/profile.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-revert-delete-account',
  standalone: true,
  imports: [NgIf],
  templateUrl: './revert-delete-account.component.html',
  styleUrls: ['./revert-delete-account.component.css']
})
export class RevertDeleteAccountComponent implements OnInit {
  public message = '';
  public loading = true;

  constructor(
    private route: ActivatedRoute,
    private profileService: ProfileService,
    private router: Router
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.paramMap.get('token');
    if (token) {
      this.profileService.revertDeleteAccount(token).subscribe({
        next: () => {
          this.message = 'Your account has been restored!';
          this.loading = false;
        },
        error: () => {
          this.message = 'This link is invalid or has expired.';
          this.loading = false;
        }
      });
    } else {
      this.message = 'Invalid link.';
      this.loading = false;
    }
  }

  goToLogin() {
    this.router.navigate(['/auth']);
  }
}
