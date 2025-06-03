import {Component, OnInit, signal} from '@angular/core';
import {Router} from '@angular/router';
import {AuthService} from '../shared/auth.service';
import {tap} from 'rxjs';
import {MemberResponseDTO, ProfileService} from './profile.service';
import {AuthStore} from '../shared/auth.store';

@Component({
  selector: 'app-profile',
  imports: [],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit{
  user: MemberResponseDTO | undefined;
  id = signal(0);
  loading = signal(false);

  constructor(private router: Router, private authStore: AuthStore, private authService :AuthService, private profileService: ProfileService) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.id.set(this.authStore.id());
      /*this.profileService.getMemberById(this.authStore.id).subscribe({
        next: (res: MemberResponseDTO) => {
          this.loading.set(false)
          this.user = res;

        },
        error: (err: any) => {
          console.error('Member Retrieval Failed', err);
        }
      });*/
    }

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
