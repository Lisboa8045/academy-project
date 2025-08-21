import {Component, Input, OnInit, signal} from '@angular/core';
import {NgIf} from '@angular/common';
import {RouterLink} from '@angular/router';
import {ServiceApiService} from '../../../shared/service-api.service';
import {ServiceProvidersBubblesModel} from '../../serviceProvidersBubbles.model';
import {LoadingComponent} from '../../../loading/loading.component';

@Component({
  selector: 'app-providers-bubbles',
  imports: [
    NgIf,
    RouterLink,
    LoadingComponent
  ],
  templateUrl: './providers-bubbles.component.html',
  styleUrl: './providers-bubbles.component.css'
})
export class ProvidersBubblesComponent implements OnInit {
  @Input() serviceId?: number;
  loading = signal(false);
  protected serviceProviderBubbles?: ServiceProvidersBubblesModel[];

  constructor(private serviceApiService: ServiceApiService) {
  }

  ngOnInit(){
    if(this.serviceId){
      this.initServiceProvidersBubbles()
    }
  }

  initServiceProvidersBubbles(){
    this.loading.set(true);
    this.serviceApiService.getServiceProvidersBubbles(this.serviceId!).subscribe({
      next:(data) => {
        this.serviceProviderBubbles = data;
        console.log("Fetched Service Providers Bubbles", this.serviceProviderBubbles);
      },
      error: (err) => {
        console.error('Error loading Service Providers Bubbles:', err);
      }
    });
    this.loading.set(false);
  }


  onImgError(e: Event) {
    (e.target as HTMLImageElement).src = 'assets/avatar-placeholder.png';
  }
}
