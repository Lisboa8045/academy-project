import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LandingPageService } from '../landing-page.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-highlighted-services',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './highlighted-services.component.html',
  styleUrls: ['./highlighted-services.component.css']
})
export class HighlightedServicesComponent implements OnInit {
  services: any[] = [];
  defaultImage = 'https://placehold.co/300x200?text=No+Image';

  constructor(
    private readonly landingService: LandingPageService,
    private readonly router: Router
  ) {}


  onImgError(event: Event) {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = this.defaultImage;
  }

  ngOnInit(): void {
    console.log('LOADING SERVICES');
    this.landingService.getTopRatedServices().subscribe(data => {
      this.services = data;
      console.log(data);
    });
    console.log('[SERVICES]: ' + this.services);
  }

  onCardClick(id: number) {
    this.router.navigate(['/services', id]);
  }
}
