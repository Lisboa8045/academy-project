import {Component, OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {AppHeaderComponent} from './header/app-header.component';
import {AppFooterComponent} from './footer/app-footer.component';
import {LoadingComponent} from './loading/loading.component';
import {HttpClient} from '@angular/common/http';
import {AuthStore} from './auth/auth.store';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, AppHeaderComponent, AppFooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  constructor(private http: HttpClient, private authStore: AuthStore) {}
  ngOnInit(): void {
    this.http.get<{ username: string }>('http://localhost:8080/auth/me', {
      withCredentials: true
    }).subscribe({
      next: res => {
        console.log('Auto-login success, username =', res.username);
        this.authStore.setUsername(res.username);
      },  error: err => {
        console.warn('Auto-login failed', err.status, err.message);
        this.authStore.clear();
      }
    });
  }
}
