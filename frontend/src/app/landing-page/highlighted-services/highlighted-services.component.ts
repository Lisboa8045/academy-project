import { Component, OnInit } from '@angular/core';
import {CommonModule, DecimalPipe} from '@angular/common';
import { LandingPageService } from '../landing-page.service';

@Component({
  selector: 'app-highlighted-services',
  standalone: true,
  imports: [
    DecimalPipe,
    CommonModule
  ],
  templateUrl: './highlighted-services.component.html',
  styleUrls: ['./highlighted-services.component.css']
})
export class HighlightedServicesComponent implements OnInit {
  services: any[] = [];

  constructor(private landingService: LandingPageService) {}

  ngOnInit(): void {
    this.landingService.getTopRatedServices().subscribe(data => {
      this.services = data;
    });
  }
}
