import {Component, effect, OnInit, signal} from '@angular/core';
import {ServiceModel} from './service.model';
import {ServiceApiService, PagedServicesResponse} from '../shared/service-api.service';
import {LoadingComponent} from '../loading/loading.component';
import {DatePipe, NgForOf} from '@angular/common';

@Component({
  selector: 'app-my-services',
  templateUrl: './my-services.component.html',
  styleUrls: ['./my-services.component.css'],
  imports: [LoadingComponent, NgForOf, DatePipe]
})
export class MyServicesComponent implements OnInit{

    ngOnInit(): void {

    }

}
