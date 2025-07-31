import {input, Signal} from '@angular/core';
import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ServiceModel} from '../service.model';
import { Router } from '@angular/router';
import {ServiceCardComponent} from '../../shared/service-card/service-card.component';

@Component({
  selector: 'app-service-list',
  standalone: true,
  imports: [CommonModule, ServiceCardComponent],
  templateUrl: './service-list.component.html',
  styleUrls: ['./service-list.component.css']
})
export class ServiceListComponent {

  constructor(private router: Router) {}

  services = input.required<ServiceModel[]>();
  clickPath = input.required<string>();

  onCardClick(id: number) {
    this.router.navigate([this.clickPath(), id]);
  }
}
