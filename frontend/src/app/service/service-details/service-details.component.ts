import {Component, OnInit, signal} from '@angular/core';
import {ServiceModel} from '../service.model';
import {ActivatedRoute} from '@angular/router';
import {ServiceDetailsService} from '../service-details.service';
import {LoadingComponent} from '../../loading/loading.component';

@Component({
  selector: 'app-service-details',
  imports: [
    LoadingComponent
  ],
  templateUrl: './service-details.component.html',
  styleUrl: './service-details.component.css'
})
export class ServiceDetailsComponent implements OnInit {
  service?: ServiceModel;
  loading = signal(false);


  constructor(
    private route: ActivatedRoute,
    private serviceDetailsService: ServiceDetailsService
  ) {
  }


  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading.set(true);
    this.serviceDetailsService.getServiceById(id).subscribe({
      next: (data) => {
        this.service = data;
        this.loading.set(false);
      },
      error: (err) => {
        console.error("Error loading service");
        this.loading.set(false);
      }

    });
  }

}
