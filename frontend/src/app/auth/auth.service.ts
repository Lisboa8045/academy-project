import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, tap} from 'rxjs';
import {AuthStore} from './auth.store';
import {LoginResponseDto} from './login-response-dto.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/auth';

  constructor(private http: HttpClient,   private authStore: AuthStore) {}

  login(login: string, password: string): Observable<any> {
    return this.http.post<LoginResponseDto>(
      `${this.apiUrl}/login`,
      { login, password }
    ).pipe(
      tap(res => {this.authStore.setUsername(res.username);
        this.authStore.setId(res.memberId);
        this.authStore.setProfilePicture(res.profilePicture);
      })
    );
  }

  logout(): Observable<any> {
    console.log("logged out")
    return this.http.get(`${this.apiUrl}/logout`).pipe(
      tap(() => this.authStore.clear())
    );
  }

  signup(email: string, username: string, roleId: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, { email: email, username: username, roleId: roleId, password: password });
  }
}


