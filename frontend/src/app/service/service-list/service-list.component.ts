import type {Signal} from '@angular/core';
import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ServiceModel} from '../service.model';

@Component({
    selector: 'app-service-list',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './service-list.component.html',
    styleUrls: ['./service-list.component.css']
})
export class ServiceListComponent {
    @Input() services!: Signal<ServiceModel[]>;
}
