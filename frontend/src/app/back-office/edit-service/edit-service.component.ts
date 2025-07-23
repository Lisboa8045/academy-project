import {Component, OnInit, signal} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {buildServiceForm} from '../service-form/service-form.builder';
import {ServiceApiService} from '../../shared/service-api.service';
import {NgForOf, NgIf} from '@angular/common';
import {ServiceTypeResponseDTO} from '../../shared/models/service-type.model';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import {faPlus, faTimes} from '@fortawesome/free-solid-svg-icons';
import {ActivatedRoute} from '@angular/router';
import {ServiceDetailsService} from '../../service/service-details.service';
import {ServiceModel} from '../../service/service.model';
import {LoadingComponent} from '../../loading/loading.component';
import {snackBarSuccess} from '../../shared/snackbar/snackbar-success';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarError} from '../../shared/snackbar/snackbar-error';
import {ManageWorkersComponent} from './manage-workers/manage-workers.component';


@Component({
  selector: 'app-edit-service',
  imports: [
    NgIf,
    ReactiveFormsModule,
    NgForOf,
    FontAwesomeModule,
    FormsModule,
    LoadingComponent,
    ManageWorkersComponent
  ],
  templateUrl: './edit-service.component.html',
  standalone: true,
  styleUrl: './edit-service.component.css'
})
export class EditServiceComponent implements OnInit {
  loading = signal(false);
  service?: ServiceModel;
  selectedFiles: File[] = [];
  readonly imageUrl = 'https://placehold.co/300x200?text=No+Image';
  faPlus=faPlus
  faTimes=faTimes
  form!: FormGroup;
  serviceTypeOptions!: ServiceTypeResponseDTO[];
  imageUrls: string[] = [];
  currentImageIndex = 0;
  newTag: string = '';

  constructor(private fb: FormBuilder,
              private serviceApi: ServiceApiService,
              private serviceDetailsService: ServiceDetailsService,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.loading.set(true);
    this.fetchService(id);
    this.fetchServiceTypes();
  }

  private fetchService(id: number): void {
    this.serviceDetailsService.getServiceById(id).subscribe({
      next: (data) => {
        this.service = data;
        if (this.service?.images && this.service.images.length > 0) {
          this.loadImages(this.service.images);
        }
        this.form = buildServiceForm(this.fb, this.service);
        this.loading.set(false);
      },
      error: () => {
        console.error("Error loading service");
      }
    });
  }

  async loadImages(fileNames: string[]) {
    if (!fileNames || fileNames.length === 0) {
      return;
    }

    for (const fileName of fileNames) {
      try {
        console.log("Fetching image..." + fileName);
        const res = await fetch(`http://localhost:8080/auth/uploads/${fileName}`);
        if (!res.ok) return;

        console.log("Fetched image..." + fileName);

        const blob = await res.blob();
        const objectUrl = URL.createObjectURL(blob);
        this.imageUrls.push(objectUrl);
        this.selectedFiles.push(this.blobToFile(blob, fileName));
      } catch (error) {
        console.error("Error loading the image", fileName, error)
      }
    }
  }

  fetchServiceTypes() {
    this.serviceApi.getServiceTypes().subscribe((response: ServiceTypeResponseDTO[]) => {
      this.serviceTypeOptions = response;
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
    console.log("SUBMITTED");
    if(this.form.valid) {
      this.serviceApi.editService(this.form.value, this.service!.id).subscribe({
        next: (res) => {
          const formData = new FormData();
          this.selectedFiles.forEach(file => {
            formData.append('files', file);
          });
          this.serviceApi.uploadServiceImages(formData, res.id).subscribe({
            next: () => console.log('Uploaded'),
            error: (err) => {
              snackBarError(this.snackBar, 'Image Upload failed.');
              console.error(err);
            }
          });
          snackBarSuccess(this.snackBar, 'Service Updated Successfully');
        },
        error: (err) => {
          snackBarError(this.snackBar, 'Service update failed.');
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

  blobToFile(blob: Blob, fileName: string): File {
    return new File([blob], fileName, {
      type: blob.type,
      lastModified: Date.now(),
    });
  }
}
