import {Component, effect, inject} from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import { RouterModule } from '@angular/router';
import {AuthStore} from '../auth/auth.store';
import {ProfileButtonComponent} from './profile-button/profile-button.component';
import {UserProfileService} from "../profile/user-profile.service";

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, ProfileButtonComponent, NgOptimizedImage],
  templateUrl: './app-header.component.html',
  styleUrls: ['./app-header.component.css']
})
export class AppHeaderComponent {
  readonly username = inject(AuthStore).username;
  readonly imageUrl = inject(UserProfileService).imageUrl;


}
