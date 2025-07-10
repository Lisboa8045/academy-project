import {Component, Input, Output, EventEmitter, OnInit, signal} from '@angular/core';
import {NgIf, NgStyle} from '@angular/common';
import {UserProfileService} from '../../profile/user-profile.service';
import {AuthStore} from '../../auth/auth.store';
import {Router} from '@angular/router';

type Tag = {
  label: string;
  color: `#${string}`;
};

@Component({
  selector: 'app-service-card',
  templateUrl: './service-card.component.html',
  imports: [
    NgIf,
    NgStyle
  ],
  styleUrls: ['./service-card.component.css']
})
export class ServiceCardComponent implements OnInit {
  @Input() service: any;
  serviceImage = signal('https://placehold.co/300x200?text=No+Image');
  @Output() cardClick = new EventEmitter<number>();
  discountedPrice: number | null = null;
  labels: (Tag)[] = [];


  constructor(private userProfileService: UserProfileService, private authStore: AuthStore, private router :Router) {
  }

  onImgError(event: Event) {
    const imgElement = event.target as HTMLImageElement;
    this.serviceImage.set(imgElement.src);
  }

  onClick() {
    this.cardClick.emit(this.service.id);
  }

  ngOnInit(){
    this.loadServiceImage();
    if(this.service.discount){
      this.calcDiscountPrice();
    }
    this.loadTags()
  }

  loadServiceImage() {
    console.log(this.service.images[0])
    this.userProfileService.getImage(this.service.images[0]).then((url) => {
      console.log(url);
      if(url !== null) this.serviceImage.set(url);
    });
  }

  get formattedDuration(): string {
    if (!this.service?.duration) return '';

    const duration = this.service.duration;

    if (duration >= 60) {
      const hours = Math.floor(duration / 60);
      const minutes = duration % 60;
      return minutes > 0 ? `${hours}h${minutes}m` : `${hours}H`;
    }
    return `${duration}m`;
  }

  private calcDiscountPrice() {
    if (this.service.discount > 0) {
      const aux = this.service.price - (this.service.price * this.service.discount) / 100;
      this.discountedPrice = parseFloat(aux.toFixed(2));
    } else{
      this.discountedPrice = null;
    }
  }

  private loadTags(){
    if(this.router.url.includes('my-services') || this.router.url.includes('profile')){
      this.labels.push({label: this.service.ownerId === this.authStore.id() ? 'owner' : 'worker', color: this.service.ownerId === this.authStore.id() ?'#3f51b5':'#4caf50'});
    }
    if (this.service.createdAt) {
      const createdDate = new Date(this.service.createdAt);
      const twoMonthsAgo = new Date();
      twoMonthsAgo.setMonth(twoMonthsAgo.getMonth() - 2);

      const isNew = createdDate >= twoMonthsAgo;

      if (isNew) {
        this.labels.push({label: 'new', color: '#FFC107'});
      }
    }
  }
}

