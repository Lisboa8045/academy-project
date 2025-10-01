import {Component, ElementRef, Input, ViewChild} from '@angular/core';
import { CommonModule } from '@angular/common';
import {Router} from '@angular/router';
import {ServiceCardComponent} from '../../shared/service-card/service-card.component';
import {ServiceModel} from '../../service/service.model';

@Component({
  selector: 'app-highlighted-services',
  standalone: true,
  imports: [
    CommonModule,
    ServiceCardComponent
  ],
  templateUrl: './highlighted-services.component.html',
  styleUrls: ['./highlighted-services.component.css']
})
export class HighlightedServicesComponent {
  @Input() title!: string;
  @Input() services: ServiceModel[] = [];
  @ViewChild('carouselWrapper', { static: false }) carouselWrapper!: ElementRef<HTMLDivElement>;

  constructor(
    private readonly router: Router
  ) {}

  onCardClick(id: number) {
    this.router.navigate(['/services', id]);
  }

  scrollLeft() {
    const wrapper = this.carouselWrapper.nativeElement;
    const track = wrapper.querySelector('.carousel-track') as HTMLElement;
    const card = track.querySelector('app-service-card') as HTMLElement;
    let gap = 0;
    if (track) {
      gap = parseInt(getComputedStyle(track).gap) || 0;
    }
    const scrollBy = card ? card.offsetWidth + gap : 320;
    wrapper.scrollBy({ left: -scrollBy, behavior: 'smooth' });
  }

  scrollRight() {
    const wrapper = this.carouselWrapper.nativeElement;
    const track = wrapper.querySelector('.carousel-track') as HTMLElement;
    const card = track.querySelector('app-service-card') as HTMLElement;
    let gap = 0;
    if (track) {
      gap = parseInt(getComputedStyle(track).gap) || 0;
    }
    const scrollBy = card ? card.offsetWidth + gap : 320;
    wrapper.scrollBy({ left: scrollBy, behavior: 'smooth' });
  }
}
