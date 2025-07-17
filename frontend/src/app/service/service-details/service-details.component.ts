import {Component, OnInit, signal} from '@angular/core';
import {ServiceModel} from '../service.model';
import {ActivatedRoute} from '@angular/router';
import {ServiceDetailsService} from '../service-details.service';
import {LoadingComponent} from '../../loading/loading.component';
import {NgForOf, NgIf} from "@angular/common";
import { Router } from '@angular/router';

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
  imageUrls: string[] = [];
  currentImageIndex = 0;
  discountedPrice: number | null = null;
  formatedTimeHours: number | null = null;
  formatedTimeMinutes: number | null = null;

  service?: ServiceModel;
  loading = signal(false);

  async loadImages(fileNames: string[]) {
    if (!fileNames || fileNames.length === 0 || this.fetched) {
      return;
    }

    for (const fileName of fileNames) {
      try {
        console.log("Fetching image..." + fileName);
        const res = await fetch(`${this.apiUrl}/${fileName}`);
        if (!res.ok) return;

        console.log("Fetched image..." + fileName);

        const blob = await res.blob();
        const objectUrl = URL.createObjectURL(blob);
        this.imageUrls.push(objectUrl);
        this.fetched = true;
      } catch (error) {
        console.error("Error loading the image", fileName, error)
      }

    }
  }

  constructor(
    private route: ActivatedRoute,
    private serviceDetailsService: ServiceDetailsService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
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


        if (this.service?.images && this.service.images.length > 0) {
          this.loadImages(this.service.images);
        }


        this.loading.set(false);
      },
      error: (err) => {
        console.error("Error loading service");
        this.loading.set(false);
      }
    });
  }

  prevImage(container: HTMLElement) {
    if (this.currentImageIndex > 0)
      this.currentImageIndex--;
    this.scrollToThumbnail(container);
  }

  nextImage(container: HTMLElement) {
    if (this.currentImageIndex < this.imageUrls.length - 1) {
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
