import { Routes } from '@angular/router';
import {AuthComponent} from './auth/auth.component';
import {ProfileComponent} from './profile/profile.component';
import {UnauthorizedComponent} from './unauthorized.component';
import {AppointmentHistoryComponent} from './appointment-history/appointment-history.component';
import {AppointmentComponent} from './appointment/appointment.component';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'profile', component: ProfileComponent },
  {path: 'appointments', component: AppointmentHistoryComponent },
  {path: 'appointments/:id', component: AppointmentComponent},
  { path: 'unauthorized', component: UnauthorizedComponent },
];
