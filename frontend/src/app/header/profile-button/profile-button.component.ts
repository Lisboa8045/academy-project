import {Component, inject, OnInit, signal} from '@angular/core';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {NgIf} from '@angular/common';
import {MenuComponent} from '../../shared/menu/menu.component';
import {MenuItem} from '../../shared/menu/menu.model';
import {AuthService} from '../../auth/auth.service';
import {Router} from '@angular/router';

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
  router = inject(Router);
  menuItems: MenuItem[] = [];

  ngOnInit() {
    this.menuItems = [
      {
        label: 'Profile',
      },
      {
        label: 'Appointments',
      },
      {
        label: 'BackOffice',
      },
      {
        label: 'Settings',
      },
      {
        label: 'Logout',
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
}
