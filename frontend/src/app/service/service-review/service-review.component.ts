import {Component, Input, OnInit, signal} from '@angular/core';
import {ServiceApiService} from '../../shared/service-api.service';
import {ServiceAppointmentReviewModel} from '../service-details/service-appointment-review-model';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {ProfileService} from '../../profile/profile.service';
import {RouterLink} from '@angular/router';
import {MemberResponseDTO} from '../../auth/member-response-dto.model';
import {AuthStore} from '../../auth/auth.store';
import {ReviewAnalyseComponent} from '../review-analyse/review-analyse/review-analyse.component';
import {PaginationBarComponent} from "../search/pagination-bar/pagination-bar.component";

interface PagedReviewResponse {
  content: ServiceAppointmentReviewModel[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Component({
  selector: 'app-service-review',
  imports: [
    NgClass,
    NgForOf,
    NgIf,
    DatePipe,
    RouterLink,
    ReviewAnalyseComponent,
    PaginationBarComponent
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

  currentPage = signal(0);
  totalPages = signal(0);
  pageSize = 10;

  constructor(
    private serviceApiService: ServiceApiService,
    private profileService: ProfileService,
    private authStore: AuthStore,
  ) {
  }

  ngOnInit(): void {
    this.isMyProfile = this.member?.id === this.authStore.id();
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.currentPage.set(page);

    if (this.serviceId) {
      this.serviceApiService
          .getReviewsByServiceId(this.serviceId, this.currentPage(), this.pageSize)
          .subscribe({
            next: (res: PagedReviewResponse) => this.handlePagedReviews(res),
            error: (err) => console.error('Error loading service reviews', err),
          });
    } else if (this.member?.id) {
      this.profileService
          .getReviewsByMemberId(this.member.id, this.currentPage(), this.pageSize)
          .subscribe({
            next: (res: PagedReviewResponse) => this.handlePagedReviews(res),
            error: (err) => console.error('Error loading profile reviews', err),
          });
    }
  }

  async loadReviewImage(fileName: string): Promise<string | null> {
    if (!fileName || fileName.length === 0 || this.fetched) {
      return null;
    }

    try {
      const res = await fetch(`${this.apiUrl}/${fileName}`);
      if (!res.ok) return null;

      const blob = await res.blob();
      const objectUrl = URL.createObjectURL(blob);
      return objectUrl;
    } catch (error) {
      console.error("Error loading the image", fileName, error)
    }
    return null;
  }

  private handlePagedReviews(data: PagedReviewResponse): void {
    this.reviews = data.content;
    this.totalPages.set(data.totalPages);
    this.hasReviews = data.totalElements > 0;

    this.imageUrls = [];
    this.reviews.forEach((review, idx) => {
      this.loadReviewImage(review.memberProfilePicture).then((url) => {
        this.imageUrls[idx] = url || '';
      });
    });
  }

  goToPreviousPage(): void {
    if (this.currentPage() > 0) this.loadPage(this.currentPage() - 1);
  }

  goToNextPage(): void {
    if (this.currentPage() + 1 < this.totalPages()) this.loadPage(this.currentPage() + 1);
  }

  goToPage(page: number | string): void {
    const pageNumber = Number(page);
    if (pageNumber >= 0 && pageNumber < this.totalPages()) this.loadPage(pageNumber);
  }
}
