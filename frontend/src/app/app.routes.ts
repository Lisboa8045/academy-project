import { Routes } from '@angular/router';
import {AuthComponent} from './auth/auth.component';
import {ProfileComponent} from './profile/profile.component';
import {UnauthorizedComponent} from './unauthorized.component';
import {MyServicesComponent} from './service/my-services.component';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: 'my-services', component: MyServicesComponent}
];
