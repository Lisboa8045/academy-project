import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServiceModel } from '../../service/service.model';

@Component({
  selector: 'app-service-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './service-list.component.html',
  styleUrls: ['../schedule.component.css']
})
export class ServiceListComponent {
  @Input() services: ServiceModel[] = [];
  @Input() selectedServiceId?: number;
  @Output() serviceSelected = new EventEmitter<ServiceModel>();

  selectService(service: ServiceModel) {
    this.serviceSelected.emit(service);
  }
}
