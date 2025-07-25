import {Component, computed, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterModule} from '@angular/router';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {AuthStore} from '../auth/auth.store';
import {ProfileButtonComponent} from './profile-button/profile-button.component';
import {SearchBarComponent} from '../shared/search-bar/search-bar.component';
import {NotificationButtonComponent} from './notification-button/notification-button.component';
import {UserProfileService} from '../profile/user-profile.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, ProfileButtonComponent, SearchBarComponent, NotificationButtonComponent],
  templateUrl: './app-header.component.html',
  styleUrls: ['./app-header.component.css']
})
export class AppHeaderComponent {
  readonly username = inject(AuthStore).username;
  readonly imageUrl = inject(UserProfileService).imageUrl;
  readonly isAdmin = computed(() => this.authStore.role() === 'ADMIN');

  form = new FormGroup({
    query: new FormControl('')
  });

  constructor(private readonly router: Router, private readonly authStore: AuthStore) {}

  isLandingPage(): boolean {
    return this.router.url === '/';
  }

  onSearchFromChild(query: string): void {
    if (query.length > 100) {
      query = query.slice(0, 100);
    }

    if (query) {
      const currentUrl = this.router.url.split('?')[0];

      if (currentUrl !== '/services') {
        this.router.navigate(['/services'], { queryParams: { q: query } });
      } else {
        this.router.navigate([], { queryParams: { q: query }, queryParamsHandling: 'merge' });
      }
    }
  }
}
