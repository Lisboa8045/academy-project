import {Component, HostListener} from '@angular/core';
import { CommonModule } from '@angular/common';
import { LandingPageService } from '../landing-page.service';
import { Observable } from 'rxjs';
import { ServiceTypeModel } from '../../models/service-type.model';
import {Router} from '@angular/router';
import {FaIconComponent, FaIconLibrary, FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {IconProp} from '@fortawesome/fontawesome-svg-core';
import { fas } from '@fortawesome/free-solid-svg-icons';
import {ShiftArrayPipe} from '../../shared/pipes/shift-array.pipe';

@Component({
  selector: 'app-service-types',
  standalone: true,
  imports: [CommonModule, FaIconComponent, ShiftArrayPipe],
  templateUrl: './service-types.component.html',
  styleUrls: ['./service-types.component.css']
})
export class ServiceTypesComponent {
  serviceTypes$: Observable<ServiceTypeModel[]>;
  isAllOpen = false;

  constructor(
    private landingService: LandingPageService,
    private router: Router,
    library: FaIconLibrary
  ) {
    this.serviceTypes$ = this.landingService.getServiceTypes();
    library.addIconPacks(fas);
  }

  getIconTuple(name: string): IconProp {
    const normalized = name
      .replace(/^fa-?/i, '')
      .replace(/[A-Z]/g, m => '-' + m.toLowerCase())
      .replace(/^-/, '');
    return ['fas', normalized] as IconProp;
  }

  onIconClick(type: string) {
    this.router.navigate(['/services'], {
      queryParams: { serviceType: type }
    });
  }

  openAll() {
    this.isAllOpen = true;
    // Optional: lock page scroll when modal is open
    document.body.style.overflow = 'hidden';
  }

  closeAll() {
    this.isAllOpen = false;
    document.body.style.overflow = '';
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEsc(ev: KeyboardEvent) {
    if (this.isAllOpen) this.closeAll();
  }

}
