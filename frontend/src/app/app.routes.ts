import { Routes } from '@angular/router';
import { AuthComponent } from './auth/auth.component';
import { ProfileComponent } from './profile/profile.component';
import { UnauthorizedComponent } from './unauthorized.component';
import { SearchServicesComponent} from "./service/search/search-services.component";
import { ScheduleComponent } from './schedule/schedule.component';
import { LandingPageComponent } from './landing-page/landing-page.component';
import { ResendEmailConfirmationComponent } from "./auth/resend-email/resend-email-confirmation.component";
import { ConfirmEmailComponent } from './auth/confirm-email/confirm-email.component';
import {ServiceDetailsComponent} from './service/service-details/service-details.component';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: 'services/:id', component: ServiceDetailsComponent},
  { path: 'services', component: SearchServicesComponent},
  { path: 'schedule/:id', component: ScheduleComponent },
  { path: '', component: LandingPageComponent},
  { path: 'resend-email', component: ResendEmailConfirmationComponent },
  { path: 'confirm-email/:token', component: ConfirmEmailComponent },
];
