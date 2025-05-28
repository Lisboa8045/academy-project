import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, tap} from 'rxjs';
import {AppHeaderComponent} from '../header/app-header.component';
import {AuthStore} from './auth.store';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/auth';

  constructor(private http: HttpClient,   private authStore: AuthStore) {}

  login(login: string, password: string): Observable<any> {
    return this.http.post<LoginResponseDto>(
      `${this.apiUrl}/login`,
      { login, password },
      { withCredentials: true }
    ).pipe(
      tap(res => this.authStore.setUsername(res.username))
    );
  }

  signup(email: string, username: string, roleId: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, { email: email, username: username, roleId: roleId, password: password });
  }
}

export interface LoginResponseDto {
  message: string;
  memberId: number;
  username: string;
}
