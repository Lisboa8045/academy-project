import { Component } from '@angular/core';
import { ServiceTypesComponent } from './service-types/service-types.component';
import { HighlightedServicesComponent } from './highlighted-services/highlighted-services.component';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [
    ServiceTypesComponent,
    HighlightedServicesComponent,
  ],
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.css']
})
export class LandingPageComponent {}
