import {Component, OnInit, signal} from '@angular/core';
import {ServiceModel} from '../service.model';
import {ActivatedRoute} from '@angular/router';
import {ServiceDetailsService} from '../service-details.service';
import {LoadingComponent} from '../../loading/loading.component';
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-service-details',
  imports: [
    LoadingComponent,
    NgIf,
    NgForOf
  ],
  templateUrl: './service-details.component.html',
  styleUrl: './service-details.component.css'
})
export class ServiceDetailsComponent implements OnInit {
  private apiUrl = 'http://localhost:8080/auth/uploads';
  fetched = false;
  imageUrl = signal("");
  discountedPrice: number | null = null;
  formatedTimeHours: number | null = null;
  formatedTimeMinutes: number | null = null;


  service?: ServiceModel;
  loading = signal(false);

  async loadImage(fileName: string) {
    if (!fileName || this.fetched) return;

    console.log("Fetching image..." + fileName);
    const res = await fetch(`${this.apiUrl}/${fileName}`);
    if (!res.ok) return;

    console.log("Fetched image..." + fileName);

    const blob = await res.blob();
    const objectUrl = URL.createObjectURL(blob);
    this.imageUrl.set(objectUrl);
    this.fetched = true;
  }

  constructor(
    private route: ActivatedRoute,
    private serviceDetailsService: ServiceDetailsService
  ) {
  }


  ngOnInit(): void {
    this.loadImage("image1.png");

    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading.set(true);
    this.serviceDetailsService.getServiceById(id).subscribe({
      next: (data) => {
        this.service = data;
        if (this.service?.price && this.service?.discount && this.service.discount > 0) {
          const aux = this.service.price - (this.service.price * this.service.discount) / 100;
          this.discountedPrice = parseFloat(aux.toFixed(2));
        } else{
          this.discountedPrice = null;
        }

        if(this.service?.duration >= 60){
          this.formatedTimeHours = Math.floor((this.service?.duration || 0) / 60);
          this.formatedTimeMinutes = this.service?.duration % 60;
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error("Error loading service");
        this.loading.set(false);
      }

    });

    }

  protected readonly length = length;



}
