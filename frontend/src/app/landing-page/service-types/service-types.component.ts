import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LandingPageService } from '../landing-page.service';
import { Observable } from 'rxjs';
import { ServiceTypeModel } from '../../models/service-type.model';
import {Router} from '@angular/router';

@Component({
  selector: 'app-service-types',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './service-types.component.html',
  styleUrls: ['./service-types.component.css']
})
export class ServiceTypesComponent {
  serviceTypes$: Observable<ServiceTypeModel[]>;
  defaultIcon = 'https://cdn-icons-png.flaticon.com/512/847/847969.png';

  constructor(
    private landingService: LandingPageService,
    private router: Router
  ) {
    this.serviceTypes$ = this.landingService.getServiceTypes();
  }

  onIconError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = this.defaultIcon;
  }

  onIconClick(type: string) {
    this.router.navigate(['/services'], {
      queryParams: { serviceType: type }
    });
  }

}
