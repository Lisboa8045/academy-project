import {Component, OnInit} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {AppHeaderComponent} from './header/app-header.component';
import {HttpClient} from '@angular/common/http';
import {AuthStore} from './auth/auth.store';
import {MemberResponseDTO} from "./auth/member-response-dto.model";
import {AppFooterComponent} from './footer/app-footer.component';
import {filter} from 'rxjs';
import {AppChatbotComponent} from './app-chatbot/app-chatbot.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, AppHeaderComponent, AppFooterComponent, AppChatbotComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {

  constructor(private readonly http: HttpClient, private readonly authStore: AuthStore, private readonly router: Router) {}

  ngOnInit(): void {

    this.http.get<MemberResponseDTO>('http://localhost:8080/auth/me', {
      withCredentials: true
    }).subscribe({
      next: res => {
        console.log('Auto-login success, username =', res.username, ' id=', res.id, ' role= ', res.role);
        this.authStore.setUsername(res.username);
        this.authStore.setId(res.id);
        this.authStore.setProfilePicture(res.profilePicture);
        this.authStore.setRole(res.role);
      },  error: err => {
        console.warn('Auto-login failed', err.status, err.message);
        this.authStore.clear();
        //this.router.navigate(['/auth']);
      }
    });

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        setTimeout(() => window.scrollTo({ top: 0, behavior: 'smooth' }), 0);
      });
  }
}
