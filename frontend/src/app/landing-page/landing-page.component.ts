import { Component } from '@angular/core';
import { ServiceTypesComponent } from './service-types/service-types.component';
import { HighlightedServicesComponent } from './highlighted-services/highlighted-services.component';
import { ServiceProvidersComponent } from './service-providers/service-providers.component';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [
    ServiceTypesComponent,
    HighlightedServicesComponent,
    ServiceProvidersComponent
  ],
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.css']
})
export class LandingPageComponent {}
