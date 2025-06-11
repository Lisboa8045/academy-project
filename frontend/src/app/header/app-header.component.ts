import {Component, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import {Router, RouterModule} from '@angular/router';
import {AuthStore} from '../auth/auth.store';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './app-header.component.html',
  styleUrls: ['./app-header.component.css']
})
export class AppHeaderComponent {
  searchQuery: string = '';
  readonly username = inject(AuthStore).username;

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
