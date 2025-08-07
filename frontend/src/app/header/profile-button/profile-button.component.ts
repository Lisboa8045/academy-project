import {Component, effect, inject, OnInit, signal} from '@angular/core';
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
  readonly role = inject(AuthStore).role;
  router = inject(Router);
  menuItems: MenuItem[] = [];

  constructor() {
    effect(() =>{this.insertByRole()});
  }

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
        icon: '📅',
        command: () => {
          this.router.navigate(['/appointments'])
        }
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

  insertByRole(){
    this.menuItems = this.menuItems.filter(item => item.label !== 'My Services');
    if (this.role() === 'WORKER') {
      this.menuItems.splice(2, 0, {
        label: 'My Services',
        icon: '🛎️',
        command: () => this.router.navigate(['/my-services'])
      });
    } else if (this.role() === 'ADMIN') {
      this.menuItems.splice(2, 0,
        {
          label: 'Admin Services',
          icon: '👔',
          command: () => {
            this.router.navigate(['/administrate-services'])
          }
        });
    }
  }
}
