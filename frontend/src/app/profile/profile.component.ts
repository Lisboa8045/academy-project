import { Component } from '@angular/core';
import {Router} from '@angular/router';
import {AuthService} from '../shared/auth.service';

@Component({
  selector: 'app-profile',
  imports: [],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {
  user = {
    image: 'https://i.pravatar.cc/300',
    username: 'john_doe',
    email: 'john.doe@example.com',
    fullName: 'John Doe'
  };

  constructor(private router: Router, private authService :AuthService) {}

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Logout failed', err);
      }
    });
  }
}
