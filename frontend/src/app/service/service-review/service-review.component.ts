import {Component, Input, OnInit} from '@angular/core';
import {ServiceDetailsService} from '../service-details.service';
import {ServiceAppointmentReviewModel} from '../service-details/service-appointment-review-model';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-service-review',
  imports: [
    NgClass,
    NgForOf,
    NgIf,
    DatePipe
  ],
  templateUrl: './service-review.component.html',
  styleUrl: './service-review.component.css'
})
export class ServiceReviewComponent implements OnInit {

  imageUrls: string[] = [];
  private fetched = false;
  private apiUrl = 'http://localhost:8080/auth/uploads';
  @Input() serviceId!: number;
  reviews?: ServiceAppointmentReviewModel[] = [];

  constructor(
    private serviceDetailsService: ServiceDetailsService,
  ) {

  }

  async loadReviewImage(fileName: string): Promise<string | null> {
    if (!fileName || fileName.length === 0 || this.fetched) {
      return null;
    }

    try {
      console.log("Fetching image..." + fileName);
      const res = await fetch(`${this.apiUrl}/${fileName}`);
      if (!res.ok) return null;

      console.log("Fetched image..." + fileName);

      const blob = await res.blob();
      const objectUrl = URL.createObjectURL(blob);
      return objectUrl;
    } catch (error) {
      console.error("Error loading the image", fileName, error)
    }
    return null;
  }

  ngOnInit(): void {
    if (!this.serviceId) return;

    this.serviceDetailsService.getReviewsByServiceId(this.serviceId).subscribe({
      next: (data) => {
        this.reviews = data;
        this.reviews.forEach((review, idx) => {
          this.loadReviewImage(review.memberProfilePicture).then(url => {
            this.imageUrls[idx] = url || '';
          });
        });
        console.log("Fetched reviews", this.reviews);
      },
      error: (err) => {
        console.error("Error loading reviews", err);
      }
    })
  }
}
