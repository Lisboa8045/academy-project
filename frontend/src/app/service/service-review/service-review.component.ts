import {Component, Input, OnInit} from '@angular/core';
import {ServiceApiService} from '../../shared/service-api.service';
import {ServiceAppointmentReviewModel} from '../service-details/service-appointment-review-model';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {ProfileService} from '../../profile/profile.service';
import {RouterLink} from '@angular/router';
import {MemberResponseDTO} from '../../auth/member-response-dto.model';
import {AuthStore} from '../../auth/auth.store';
import {ReviewAnalyseComponent} from '../review-analyse/review-analyse/review-analyse.component';

@Component({
  selector: 'app-service-review',
  imports: [
    NgClass,
    NgForOf,
    NgIf,
    DatePipe,
    RouterLink,
    ReviewAnalyseComponent
  ],
  templateUrl: './service-review.component.html',
  styleUrl: './service-review.component.css'
})
export class ServiceReviewComponent implements OnInit {

  imageUrls: string[] = [];
  private fetched = false;
  private apiUrl = 'http://localhost:8080/auth/uploads';
  @Input() serviceId?: number;
  @Input() member : MemberResponseDTO | undefined;
  reviews?: ServiceAppointmentReviewModel[] = [];
  hasReviews = false;
  isMyProfile?: boolean;


  constructor(
    private serviceApiService: ServiceApiService,
    private profileService: ProfileService,
    private authStore: AuthStore,
  ) {
  }

  getReviews() {
    this.profileService.getReviewsByMemberId(this.authStore.id()).subscribe({
      next: (res) => {
        this.hasReviews = res.length > 0;
      }
    })
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
    if (this.serviceId) {
      this.serviceApiService.getReviewsByServiceId(this.serviceId).subscribe({
        next: (data) => {
          this.handleReviews(data);
        },
        error: (err) => {
          console.error("Error loading service reviews", err);
        }
      });
    } else if (this.member?.id) {
      this.isMyProfile = this.authStore.id() === this.member.id;
      this.profileService.getReviewsByMemberId(this.member.id).subscribe({
        next: (data) => {
          this.handleReviews(data);
        },
        error: (err) => {
          console.error("Error loading profile reviews", err);
        }
      });
    }
    this.getReviews();
  }

  private handleReviews(data: ServiceAppointmentReviewModel[]) {
    this.reviews = data;
    this.reviews.forEach((review, idx) => {
      this.loadReviewImage(review.memberProfilePicture).then(url => {
        this.imageUrls[idx] = url || '';
      });
    });
    console.log("Fetched reviews", this.reviews);
  }
}
