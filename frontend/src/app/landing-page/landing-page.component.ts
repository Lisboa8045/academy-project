import {Component, OnInit} from '@angular/core';
import { ServiceTypesComponent } from './service-types/service-types.component';
import { HighlightedServicesComponent } from './highlighted-services/highlighted-services.component';
import { SearchBarComponent } from '../shared/search-bar/search-bar.component';
import { Router } from '@angular/router';
import {ServiceModel} from "../service/service.model";
import {LandingPageService} from "./landing-page.service";

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
export class LandingPageComponent implements OnInit {
  topRatedServices: ServiceModel[] = [];
  discountedServices: ServiceModel[] = [];
  popularServices: ServiceModel[] = [];

  constructor(private readonly router: Router,
              private readonly landingService: LandingPageService) {}

  ngOnInit(): void {
    this.landingService.getTopRatedServices().subscribe(data => this.topRatedServices = data);
    this.landingService.getDiscountedServices().subscribe(data => this.discountedServices = data);
    this.landingService.getTrendingServices().subscribe(data => this.popularServices = data);
  }

  onSearch(query: string): void {
    if (query.length > 100) {
      query = query.slice(0, 100);
    }

    if (query) {
      this.router.navigate(['/services'], { queryParams: { q: query } });
    }
  }
}
