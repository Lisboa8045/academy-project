import {Component, OnInit, signal} from '@angular/core';
import {ServiceModel} from '../service.model';
import {ActivatedRoute, Router} from '@angular/router';
import {ServiceApiService} from '../../shared/service-api.service';
import {LoadingComponent} from '../../loading/loading.component';
import {NgForOf, NgIf} from "@angular/common";
import {UserProfileService} from '../../profile/user-profile.service';
import {ServiceReviewComponent} from '../service-review/service-review.component';
import {TagListComponent} from './tag-list/tag-list.component';
import {switchMap, tap} from 'rxjs';

@Component({
  selector: 'app-service-details',
  imports: [
    LoadingComponent,
    NgIf,
    NgForOf,
    ServiceReviewComponent,
    TagListComponent
  ],
  templateUrl: './service-details.component.html',
  styleUrl: './service-details.component.css'
})
export class ServiceDetailsComponent implements OnInit {
  private apiUrl = 'http://localhost:8080/auth/uploads';
  currentImageIndex = 0;
  discountedPrice: number | null = null;
  formatedTimeHours: number = 0;
  formatedTimeMinutes: number = 0;
  serviceId!: number;
  service?: ServiceModel;
  loading = signal(false);

  constructor(
    private route: ActivatedRoute,
    private serviceApiService: ServiceApiService,
    protected userProfileService: UserProfileService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        tap(() => {
          this.service = undefined;
          this.userProfileService.serviceImageUrl = [];
          this.userProfileService.fetched = false;
          this.discountedPrice = null;
          this.currentImageIndex = 0;
          this.loading.set(true);
        }),
        switchMap(params => {
          const id = Number(params.get('id'));
          this.serviceId = id;
          if (isNaN(id)) {
            this.router.navigate(['/not-found'])
          }
          ;// AQUI PODE-SE DEFINIR O QUE QUEREMOS CARREGAR QUANDO ESTA POR EXEMPLO .../service/a. "a" ná é válido
          return this.serviceApiService.getServiceById(id);
        })
      )
      .subscribe({
        next: (data) => {
          if (!data) return;

          this.service = data;

          if (this.service?.price && this.service?.discount && this.service.discount > 0) {
            const aux = this.service.price - (this.service.price * this.service.discount) / 100;
            this.discountedPrice = parseFloat(aux.toFixed(2));
          }

          this.formatedTimeHours = Math.floor((this.service?.duration || 0) / 60);
          this.formatedTimeMinutes = this.service?.duration % 60;

          if (this.service?.images?.length > 0) {
            this.userProfileService.loadImages(this.service.images);
          }

          this.loading.set(false);
        },
        error: (err) => {
          console.error('Erro ao carregar o serviço:', err);
          this.loading.set(false);
          this.router.navigate(['/not-found'])
        }
      });
  }


  search(string: string) {
    this.router.navigate(['/services'], {queryParams: {q: string}});
  }

  searchServiceType(string: string) {
    this.router.navigate(['/services'], {
      queryParams: {
        name: '',
        page: 0,
        size: 10,
        sort: 'price,asc',
        negotiable: false,
        serviceTypeName: this.service?.serviceTypeName
      }
    });
  }

  prevImage(container: HTMLElement) {
    if (this.currentImageIndex > 0)
      this.currentImageIndex--;
    this.scrollToThumbnail(container);
  }

  nextImage(container: HTMLElement) {
    if (this.currentImageIndex < this.userProfileService.serviceImageUrl.length - 1) {
      this.currentImageIndex++;
      this.scrollToThumbnail(container);
    }
  }

  scrollToThumbnail(container: HTMLElement) {
    const thumbnails = container.querySelectorAll('.thumbnail-image');
    const selectedThumb = thumbnails[this.currentImageIndex] as HTMLElement;

    if (selectedThumb) {
      const containerRect = container.getBoundingClientRect();
      const thumbRect = selectedThumb.getBoundingClientRect();

      if (thumbRect.left < containerRect.left) {
        container.scrollBy({
          left: thumbRect.left - containerRect.left - 8,
          behavior: 'smooth'
        });
      } else if (thumbRect.right > containerRect.right) {
        container.scrollBy({
          left: thumbRect.right - containerRect.right + 8,
          behavior: 'smooth'
        });
      }
    }
  }

  selectThumbnail(index: number, container: HTMLElement) {
    this.currentImageIndex = index;

    const thumbnailWidth = 68; // 60px + 8px
    const scrollPosition = index * thumbnailWidth;

    container.scrollTo({
      left: scrollPosition,
      behavior: 'smooth'
    });
  }

  onClick(id: number | undefined) {
    if (id) {
      this.router.navigate(['/schedule', id]);
    }
  }
}
