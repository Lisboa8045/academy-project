import { Component, Input, Output, EventEmitter } from '@angular/core';
import {CurrencyPipe, NgIf} from '@angular/common';
import {RatedServiceModel} from '../../service/service.model';

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
  @Input() service!: RatedServiceModel;
  @Input() defaultImage: string = 'https://placehold.co/300x200?text=No+Image';
  @Output() cardClick = new EventEmitter<number>();

  onImgError(event: Event) {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = this.defaultImage;
  }

  onClick() {
    this.cardClick.emit(this.service?.id);
  }
}
