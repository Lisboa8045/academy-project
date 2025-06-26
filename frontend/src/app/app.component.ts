import {Component, OnInit} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';
import {AppHeaderComponent} from './header/app-header.component';
import {HttpClient} from '@angular/common/http';
import {AuthStore} from './auth/auth.store';
import {MemberResponseDTO} from "./auth/member-response-dto.model";
import {AppFooterComponent} from './footer/app-footer.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, AppHeaderComponent, AppFooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  constructor(private http: HttpClient, private authStore: AuthStore, private router: Router) {}
  ngOnInit(): void {
    this.http.get<MemberResponseDTO>('http://localhost:8080/auth/me', {
      withCredentials: true
    }).subscribe({
      next: res => {
        console.log('Auto-login success, username =', res.username, ' id=', res.id);
        this.authStore.setUsername(res.username);
        this.authStore.setId(res.id);
        this.authStore.setProfilePicture(res.profilePicture)
      },  error: err => {
        console.warn('Auto-login failed', err.status, err.message);
        this.authStore.clear();
        //this.router.navigate(['/auth']);
      }
    });
  }
}
