import { Component, EventEmitter, Output } from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgForOf} from '@angular/common';
import {Review} from '../review.model';

@Component({
  selector: 'app-review-modal',
  templateUrl: './review-modal.component.html',
  imports: [
    FormsModule,
    NgForOf
  ],
  styleUrls: ['./review-modal.component.css']
})
export class ReviewModalComponent {
  @Output() close = new EventEmitter<void>();
  @Output() submitReview = new EventEmitter<Review>();

  rating: number = 0;
  comment: string = '';

  setRating(value: number) {
    this.rating = value;
  }

  submit() {
    this.submitReview.emit({ rating: this.rating, comment: this.comment });
    this.close.emit();
  }
}
