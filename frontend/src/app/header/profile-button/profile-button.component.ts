import {Component, inject, OnInit, signal} from '@angular/core';
import {NgIf} from '@angular/common';
import {MenuComponent} from '../../shared/menu/menu.component';
import {MenuItem} from '../../shared/menu/menu.model';
import {AuthService} from '../../auth/auth.service';
import {Router} from '@angular/router';
import {AuthStore} from '../../auth/auth.store';
import {UserProfileService} from '../../profile/user-profile.service';

@Component({
  selector: 'app-profile-button',
  imports: [
    NgIf,
    MenuComponent
  ],
  templateUrl: './profile-button.component.html',
  styleUrl: './profile-button.component.css'
})
export class ProfileButtonComponent implements OnInit {
  showMenu = signal(false);
  authService = inject(AuthService);
  readonly username = inject(AuthStore).username;
  readonly imageUrl = inject(UserProfileService).imageUrl;
  router = inject(Router);
  menuItems: MenuItem[] = [];

  ngOnInit() {
    this.menuItems = [
      {
        label: 'Profile',
        icon: '👤',
        command: () => {
          this.router.navigate(['/profile'])
        }
      },
      {
        label: 'Appointments',
        icon: '📅'
      },
      {
        label: 'BackOffice',
        icon: '👔',
        command: () => {
          this.router.navigate(['/backoffice/availability'])
        }

      },
      {
        label: 'Schedule',
        icon: '⚙️',
        command: () => {
          this.router.navigate(['/schedule'])
        }
      },
      {
        label: 'Settings',
        icon: '⚙️'
      },
      {
        label: 'Logout',
        icon: '↩',
        command: () => {
          this.authService.logout().subscribe({
            next: () => this.router.navigate(['/']),
            error: (err) => console.error('Logout failed:', err)
          });
        }
      },
    ];
  }

  toggleMenu() {
    this.showMenu.set(!this.showMenu());
  }

  closeMenu() {
    this.showMenu.set(false);
  }
}
