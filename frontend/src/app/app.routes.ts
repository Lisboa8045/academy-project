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
import {ConfirmAppointmentComponent} from './confirm-appointment/confirm-appointment.component';
import {GlobalConfigurationEditComponent} from './global-configuration/global-configuration-edit.component';
import { PermissionGuard } from './auth/permission.guard';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'profile/:id', component: ProfileComponent },
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: 'services/:id', component: ServiceDetailsComponent},
  { path: 'services', component: SearchServicesComponent},
  { path: '', component: LandingPageComponent},
  { path: 'resend-email', component: ResendEmailConfirmationComponent },
  { path: 'confirm-email/:token', component: ConfirmEmailComponent },
  { path: 'about', component: AboutComponent },
  { path: 'terms', component: TermsComponent },
  { path: 'privacy', component: PrivacyComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password/:token', component: ResetPasswordComponent },
  {
    path: 'profile',
    component: ProfileComponent,
    canActivate: [PermissionGuard],
  },
  {
    path: 'appointments',
    component: AppointmentHistoryComponent,
    canActivate: [PermissionGuard],
  },
  { path: 'schedule/:id',
    component: ScheduleComponent,
    canActivate: [PermissionGuard]
  },
  { path: 'my-services',
    component: MyServicesComponent,
    canActivate: [PermissionGuard],
    data: { roles: ['ADMIN', 'WORKER']}
  },
  { path: 'confirm-appointment/:id',
    component: ConfirmAppointmentComponent,
    canActivate: [PermissionGuard],
  },
  { path: 'config',
    component: GlobalConfigurationEditComponent,
    canActivate: [PermissionGuard],
    data: { roles: ['ADMIN']}
  },
  { path: 'admin-office',
    component: LandingPageComponent,
    canActivate: [PermissionGuard],
    data: { roles: ['ADMIN']}
  },
  { path: 'availability-management',
    component: LandingPageComponent,
    canActivate: [PermissionGuard],
    data: { roles: ['WORKER', 'ADMIN']}
  },
  { path: 'service-management',
    component: LandingPageComponent,
    canActivate: [PermissionGuard],
    data: { roles: ['WORKER', 'ADMIN']}
  },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];
