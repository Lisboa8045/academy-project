import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LandingPageService } from '../landing-page.service';
import { Observable } from 'rxjs';
import { ServiceTypeModel } from '../../models/service-type.model';

@Component({
  selector: 'app-service-types',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './service-types.component.html',
  styleUrls: ['./service-types.component.css']
})
export class ServiceTypesComponent {
  serviceTypes$: Observable<ServiceTypeModel[]>;

  constructor(private landingService: LandingPageService) {
    this.serviceTypes$ = this.landingService.getServiceTypes();
  }
}
