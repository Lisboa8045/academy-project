import {Routes} from '@angular/router';
import {AuthComponent} from './auth/auth.component';
import {ProfileComponent} from './profile/profile.component';
import {UnauthorizedComponent} from './unauthorized.component';
import {ResendEmailConfirmationComponent} from "./auth/resend-email/resend-email-confirmation.component";
import {ConfirmEmailComponent} from './auth/confirm-email/confirm-email.component';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: 'resend-email', component: ResendEmailConfirmationComponent },
  { path: 'confirm-email/:token', component: ConfirmEmailComponent },
];
