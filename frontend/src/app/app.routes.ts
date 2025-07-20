import { Routes } from '@angular/router';
import {AppointmentHistoryComponent} from './appointment/appointment-history/appointment-history.component';
import { AuthComponent } from './auth/auth.component';
import { ProfileComponent } from './profile/profile.component';
import { UnauthorizedComponent } from './unauthorized.component';
import { SearchServicesComponent} from "./service/search/search-services.component";
import { ScheduleComponent } from './schedule/schedule.component';
import { LandingPageComponent } from './landing-page/landing-page.component';
import {MyServicesComponent} from './service/my-services/my-services.component';
import { ResendEmailConfirmationComponent } from "./auth/resend-email/resend-email-confirmation.component";
import { ConfirmEmailComponent } from './auth/confirm-email/confirm-email.component';
import { ServiceDetailsComponent } from './service/service-details/service-details.component';
import { AboutComponent } from './info-pages/about/about.component';
import { TermsComponent } from './info-pages/terms/terms.component';
import { PrivacyComponent } from './info-pages/privacy/privacy.component';
import { ForgotPasswordComponent } from './auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './auth/reset-password/reset-password.component';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'appointments', component: AppointmentHistoryComponent },
  { path: 'profile/:id', component: ProfileComponent },
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: 'services/:id', component: ServiceDetailsComponent},
  { path: 'services', component: SearchServicesComponent},
  { path: 'schedule/:id', component: ScheduleComponent },
  { path: '', component: LandingPageComponent},
  { path: 'my-services', component: MyServicesComponent},
  { path: 'resend-email', component: ResendEmailConfirmationComponent },
  { path: 'confirm-email/:token', component: ConfirmEmailComponent },
  { path: 'about', component: AboutComponent },
  { path: 'terms', component: TermsComponent },
  { path: 'privacy', component: PrivacyComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password/:token', component: ResetPasswordComponent },
  { path: '**', redirectTo: '', pathMatch: 'full' },
];
