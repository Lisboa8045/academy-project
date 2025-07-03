import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {catchError, Observable, tap, throwError} from 'rxjs';
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
        {login, password}
    ).pipe(
        tap(res => {
          this.authStore.setUsername(res.username);
          this.authStore.setId(res.memberId);
          this.authStore.setProfilePicture(res.profilePicture);
        }),
        catchError((error: HttpErrorResponse) => {
          if (error.status === 403 && error.error?.startsWith('Member is Inactive with status WAITING_FOR_EMAIL_APPROVAL')) {
            const email = error.error.split(':')[1] || '';
            return throwError(() => ({
              type: 'EMAIL_NOT_CONFIRMED',
              email
            }));
          }

          return throwError(() => error);
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

  resendConfirmation(login: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/recreate-confirmation-token`, {
      login: login
    });
  }

  confirmEmail(token: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/confirm-email/${token}`);
  }

  requestPasswordReset(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/password-reset-token`, {
      email: email
    });
  }

  verifyResetToken(token: string) {
    return this.http.get(`${this.apiUrl}/password-reset/${token}`);
  }
}


