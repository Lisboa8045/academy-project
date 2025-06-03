import { Routes } from '@angular/router';
import {AuthComponent} from './auth/auth.component';
import {AppDummyComponent} from './dummy/app-dummy.component';
import {ProfileComponent} from './profile/profile.component';
import {ServiceListComponent} from './service/service-list.component';
import {DontHavePermissionComponent} from './403.component';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'dont-have-permission', component: DontHavePermissionComponent },
];
