import type {Signal} from '@angular/core';
import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ServiceModel} from '../service.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-service-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './service-list.component.html',
  styleUrls: ['./service-list.component.css']
})
export class ServiceListComponent {

  constructor(private router: Router) {}

  @Input() services!: Signal<ServiceModel[]>;

  onCardClick(id: number) {
    this.router.navigate(['/services', id]);
  }
}
