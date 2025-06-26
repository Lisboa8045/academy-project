import { Component } from '@angular/core';
import { ServiceTypesComponent } from './service-types/service-types.component';
import { HighlightedServicesComponent } from './highlighted-services/highlighted-services.component';
import { SearchBarComponent } from '../shared/search-bar/search-bar.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [
    ServiceTypesComponent,
    HighlightedServicesComponent,
    SearchBarComponent
  ],
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.css']
})
export class LandingPageComponent {

  constructor(private router: Router) {}

  onSearch(query: string): void {
    if (query.length > 100) {
      query = query.slice(0, 100);
    }

    if (query) {
      this.router.navigate(['/services'], { queryParams: { q: query } });
    }
  }
}
