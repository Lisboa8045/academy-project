import { Routes } from '@angular/router';
import {AuthComponent} from './auth/auth.component';
import {AppDummyComponent} from './dummy/app-dummy.component';
import {ProfileComponent} from './profile/profile.component';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'appointments', component: AppDummyComponent}
];
