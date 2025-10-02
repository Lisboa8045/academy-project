import {Component, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AuthStore} from '../../../auth/auth.store';
import {ProfileService} from '../../../profile/profile.service';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-review-analyse',
  templateUrl: './review-analyse.component.html',
  imports: [
    NgIf
  ],
  styleUrls: ['./review-analyse.component.css']
})
export class ReviewAnalyseComponent {
  loading = signal(false);
  showModal = signal(false);
  result = signal<any>(null);

  constructor(
    private http: HttpClient,
    private authStore: AuthStore,
    private profileService: ProfileService
  ) {
  }

  analyzeReviews() {
    const memberId = this.authStore.id();
    this.loading.set(true);

    this.profileService.getAllReviewsByMemberId(memberId).subscribe({
      next: (reviews) => {
        const webhookUrl = 'http://localhost:5678/webhook/reviews-analysis';

        this.http.post<{
          introduction: string;
          positive: string;
          negative: string;
          conclusion: string;
        }[]>(webhookUrl, {memberId, reviews})
          .subscribe({
            next: (response) => {

              const feedback = response[0];

              this.result.set(feedback);

              this.loading.set(false);
              this.showModal.set(true);
            },
            error: (err) => {
              console.error(err);
              this.loading.set(false);
            }
          });
      },
      error: (err) => {
        console.error(err);
        this.loading.set(false);
      }
    });
  }

  closeModal() {
    this.showModal.set(false);
    this.result.set('');
  }
}
