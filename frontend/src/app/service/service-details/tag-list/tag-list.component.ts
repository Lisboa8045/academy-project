import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgForOf, NgIf, SlicePipe} from '@angular/common';

@Component({
  selector: 'tag-list',
  imports: [
    NgIf,
    NgForOf,
    SlicePipe
  ],
  templateUrl: './tag-list.component.html',
  styleUrl: './tag-list.component.css'
})
export class TagListComponent {

  @Input() tags: string[] = [];
  @Output() tagClicked = new EventEmitter<string>();

  showModal = false;

  onTagClick(tag: string) {
    this.tagClicked.emit(tag);
  }

  openTagModal(): void {
    this.showModal = true;
    document.body.classList.add('no-scroll');
  }

  closeModal(): void {
    this.showModal = false;
    document.body.classList.remove('no-scroll');
  }
}
