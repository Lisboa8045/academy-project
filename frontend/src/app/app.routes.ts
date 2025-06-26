import { Routes } from '@angular/router';
import {AuthComponent} from './auth/auth.component';
import {ProfileComponent} from './profile/profile.component';
import {UnauthorizedComponent} from './unauthorized.component';
import {EditServiceComponent} from './back-office/edit-service/edit-service.component';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: 'backoffice/service/:id', component: EditServiceComponent },
];
