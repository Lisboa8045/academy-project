import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {buildServiceForm} from '../service-form/service-form.builder';
import {ServiceApiService} from '../../shared/service-api.service';
import {NgForOf, NgIf} from '@angular/common';
import {ServiceTypeResponseDTO} from '../../shared/models/service-type.model';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faPlus, faTimes} from '@fortawesome/free-solid-svg-icons';
import {Router} from '@angular/router';


@Component({
  selector: 'app-create-service',
  imports: [
    NgIf,
    ReactiveFormsModule,
    NgForOf,
    FontAwesomeModule,
    FormsModule
  ],
  templateUrl: './create-service.component.html',
  standalone: true,
  styleUrl: './create-service.component.css'
})
export class CreateServiceComponent implements OnInit {
  selectedFiles: File[] = [];
  readonly imageUrl = 'https://placehold.co/300x200?text=No+Image';
  faPlus=faPlus
  faTimes=faTimes
  form!: FormGroup;
  serviceTypeOptions!: ServiceTypeResponseDTO[];
  imageUrls: string[] = [];
  currentImageIndex = 0;
  newTag: string = '';

  constructor(private fb: FormBuilder, private serviceApi: ServiceApiService, private router: Router) {}

  ngOnInit() {
    this.form = buildServiceForm(this.fb);
    this.fetchServiceTypes();
  }

  fetchServiceTypes() {
    this.serviceApi.getServiceTypes().subscribe((response: ServiceTypeResponseDTO[]) => {
      this.serviceTypeOptions = response;
      console.log(this.serviceTypeOptions);
    });
  }

  get tagNames(): FormArray {
    return this.form.get('tagNames') as FormArray;
  }

  addTag(): void {
    const trimmed = this.newTag.trim();
    if (trimmed && !this.tagNames.value.includes(trimmed)) {
      this.tagNames.push(this.fb.control(trimmed));
      this.newTag = '';
    }
  }

  removeTag(index: number): void {
    this.tagNames.removeAt(index);
  }

  submit() {
    if(this.form.valid) {
      this.serviceApi.createService(this.form.value).subscribe({
        next: (res) => {
          if (this.selectedFiles.length > 0) {
            const formData = new FormData();
            this.selectedFiles.forEach(file => {
              formData.append('files', file);
            });
            this.serviceApi.uploadServiceImages(formData, res.id).subscribe({
              next: () => console.log('Uploaded'),
              error: (err) => {
                alert('Upload failed.');
                console.error(err);
              },
              complete: () => {
                this.router.navigate([`/backoffice/services/${res.id}`]);
              }
            });
          }
        },
        error: (err) => {
          alert('Service creation failed.');
          console.error(err);
        }
      });

    }
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

  onImagesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    const newFiles = Array.from(input.files);

    newFiles.forEach(file => {
      this.selectedFiles.push(file);
      const objectUrl = URL.createObjectURL(file);
      this.imageUrls.push(objectUrl);
    });
  }

  deleteImage(index: number) {
    console.log("DELETE IMAGE: " + this.selectedFiles[index].name);
    this.selectedFiles.splice(index, 1);
    this.imageUrls.splice(index, 1);
  }

  moveImageLeft(imageIndex: number) {
    if (imageIndex === 0) return;
    this.swapImages(imageIndex, imageIndex - 1);
  }

  moveImageRight(imageIndex: number) {
    if (imageIndex === this.selectedFiles.length - 1) return;
    this.swapImages(imageIndex, imageIndex + 1);
  }

  private swapImages(imageIndex1: number, imageIndex2: number) {
    const tempFile = this.selectedFiles[imageIndex1];
    this.selectedFiles[imageIndex1] = this.selectedFiles[imageIndex2];
    this.selectedFiles[imageIndex2] = tempFile;

    const tempUrl = this.imageUrls[imageIndex1];
    this.imageUrls[imageIndex1] = this.imageUrls[imageIndex2];
    this.imageUrls[imageIndex2] = tempUrl;
  }
}
