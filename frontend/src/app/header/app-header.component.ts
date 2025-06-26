import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterModule} from '@angular/router';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {AuthStore} from '../auth/auth.store';
import {ProfileButtonComponent} from './profile-button/profile-button.component';
import {UserProfileService} from "../profile/user-profile.service";

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, ProfileButtonComponent],
  templateUrl: './app-header.component.html',
  styleUrls: ['./app-header.component.css']
})
export class AppHeaderComponent {
  readonly username = inject(AuthStore).username;
  readonly imageUrl = inject(UserProfileService).imageUrl;

  form = new FormGroup({
    query: new FormControl('')
  });

  constructor(private router: Router) {}

  onSearch(): void {
    const query = this.form.value.query?.trim() ?? '';

    if (query.length > 100) {
      this.form.controls.query.setValue(query.slice(0, 100));
    } else {
      this.form.controls.query.setValue(query);
    }

    if (query) {
      const currentUrl = this.router.url.split('?')[0];

      if (currentUrl !== '/services') {
        this.router.navigate(['/services'], {
          queryParams: {q: query},
        });
      } else {
        this.router.navigate([], {
          queryParams: {q: query},
          queryParamsHandling: 'merge',
        });
      }
    }
  }
}