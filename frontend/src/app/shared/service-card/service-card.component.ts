import { Component, Input, Output, EventEmitter } from '@angular/core';
import {CurrencyPipe, NgIf} from '@angular/common';
import {ServiceModel} from '../../service/service.model';

@Component({
  selector: 'app-service-card',
  templateUrl: './service-card.component.html',
  imports: [
    CurrencyPipe,
    NgIf
  ],
  styleUrls: ['./service-card.component.css']
})
export class ServiceCardComponent {
  @Input() service: any;
  @Input() defaultImage: string = 'https://placehold.co/300x200?text=No+Image';
  @Output() cardClick = new EventEmitter<number>();

  onImgError(event: Event) {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = this.defaultImage;
  }

  onClick() {
    this.cardClick.emit(this.service.id);
  }

  get formattedDuration(): string {
    if (!this.service?.duration) return '';

    const duration = this.service.duration;

    if (duration >= 60) {
      const hours = Math.floor(duration / 60);
      const minutes = duration % 60;
      return `${hours}h ${minutes}m`;
    }

    return `${duration}m`;
  }
}
