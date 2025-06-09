import {Component, OnInit, signal} from '@angular/core';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {NgIf} from '@angular/common';
import {MenuComponent} from '../../shared/menu/menu.component';
import {MenuItem} from '../../shared/menu/menu.model';

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
      },
    ];
  }

  toggleMenu() {
    this.showMenu.set(!this.showMenu());
  }
}
