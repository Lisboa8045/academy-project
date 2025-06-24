import {Routes} from '@angular/router';
import {AuthComponent} from './auth/auth.component';
import {ProfileComponent} from './profile/profile.component';
import {UnauthorizedComponent} from './unauthorized.component';
import {ScheduleComponent} from './schedule/schedule.component';
import {LandingPageComponent} from './landing-page/landing-page.component';
import {SearchServicesComponent} from "./service/search/search-services.component";

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: 'schedule', component: ScheduleComponent },
  {path: '', component: LandingPageComponent},
  { path: 'services', component: SearchServicesComponent},
];
