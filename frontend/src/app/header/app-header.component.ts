import {Component, effect, inject} from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {Router, RouterModule} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {AuthStore} from '../auth/auth.store';
import {ProfileButtonComponent} from './profile-button/profile-button.component';
import {UserProfileService} from "../profile/user-profile.service";

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ProfileButtonComponent, NgOptimizedImage],
  templateUrl: './app-header.component.html',
  styleUrls: ['./app-header.component.css']
})
export class AppHeaderComponent {
  searchQuery: string = '';
  readonly username = inject(AuthStore).username;
  readonly imageUrl = inject(UserProfileService).imageUrl;


  constructor(private router: Router) {}

  onSearch(): void {
    const query = this.searchQuery.trim();

    if (query.length > 100) {
      this.searchQuery = query.slice(0, 100);
    } else {
      this.searchQuery = query;
    }

    if (this.searchQuery) {
      this.router.navigate(['/services'], {
        queryParams: { q: this.searchQuery },
      });
    }
  }
}
