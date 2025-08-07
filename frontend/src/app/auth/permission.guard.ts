import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class PermissionGuard implements CanActivate {
  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    return this.http.get<any>('http://localhost:8080/auth/me', { withCredentials: true }).pipe(
      map(user => {
        if (!user?.id) {
          localStorage.setItem('redirectAfterLogin', state.url);
          return this.router.createUrlTree(['/auth']);
        }

        const requiredRoles = route.data['roles'] as string[] | string | undefined;
        const userRole = user.role;

        if (requiredRoles) {
          const hasAccess = Array.isArray(requiredRoles)
            ? requiredRoles.includes(userRole)
            : userRole === requiredRoles;

          if (!hasAccess) {
            return this.router.createUrlTree(['/unauthorized']);
          }
        }

        return true;
      }),
      catchError(() => {
        localStorage.setItem('redirectAfterLogin', state.url);
        return of(this.router.createUrlTree(['/auth']));
      })
    );
  }
}
