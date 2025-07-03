import {Component, inject, OnInit, signal} from '@angular/core';
import {LoadingComponent} from '../../loading/loading.component';
import {ActivatedRoute} from '@angular/router';
import {EditServiceService} from './edit-service.service';
import {ServiceDetailsService} from '../../service/service-details.service';
import {ServiceModel} from '../../service/service.model';
import {ServiceProviderModel} from '../../models/service-provider.model';
import {JsonPipe} from '@angular/common';

@Component({
  selector: 'app-edit-service',
  imports: [
    LoadingComponent,
    JsonPipe
  ],
  templateUrl: './edit-service.component.html',
  styleUrl: './edit-service.component.css'
})
export class EditServiceComponent implements OnInit {
  loading = signal(false);
  route = inject(ActivatedRoute);
  editServiceService = inject(EditServiceService);
  serviceDetailsService = inject(ServiceDetailsService);
  service?: ServiceModel;
  serviceProviders?: ServiceProviderModel[];

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.loading.set(true);
    this.serviceDetailsService.getServiceById(id).subscribe({
      next: (data) => {
        this.service = data;
      },
      error: (err) => {
        console.error("Error loading service");
      }
    });
    this.editServiceService.getServiceProvidersByServiceId(id).subscribe({
      next: (data) => {
        this.serviceProviders = data;
      },
      error: (err) => {
        console.error("Error loading service providers");
      },
      complete: () => {
        this.loading.set(false);
      }
    });
  }
}
