import {Component, effect, inject, OnInit, signal} from '@angular/core';
import {Router} from '@angular/router';
import { ProfileService} from './profile.service';
import {AuthStore} from '../auth/auth.store';
import {AuthService} from '../auth/auth.service';
import {LoadingComponent} from '../loading/loading.component';
import {MemberResponseDTO} from '../auth/member-response-dto.model';
import {UserProfileService} from './user-profile.service';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';
import {AppConfigService} from '../shared/app-config.service';

@Component({
  selector: 'app-profile',
  imports: [
    LoadingComponent,
    ReactiveFormsModule,
    NgIf
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent{
  user: MemberResponseDTO | undefined;
  loading = signal(false);
  readonly imageUrl = inject(UserProfileService).imageUrl;
  tempImageUrl = signal("");
  profileForm!: FormGroup;
  editMode = false;
  selectedFile:File | null = null;


  constructor(private fb: FormBuilder, private router: Router, protected authStore: AuthStore, private authService :AuthService, private profileService: ProfileService, private userProfileService:UserProfileService, private appConfigService:AppConfigService) {
    effect(() => {
      this.loading.set(true);
      if(this.authStore.id() > -1)
        this.getMember(this.authStore.id());
    });
  }

  getMember(id: number) {
    console.log("Getting member for id " + id);
    this.profileService.getMemberById(id).subscribe({
      next: (res: MemberResponseDTO) => {
        this.loading.set(false)
        console.log(res)
        this.user = res;
        this.loadForm(this.user);
      },
      error: (err: any) => {
        this.loading.set(false)

        console.error('Member Retrieval Failed', err);
      }
    });
  }

  loadForm(user: MemberResponseDTO) {
    this.profileForm = this.fb.group({
      username: [user.username],
      email: [user.email],
      address: [user.address],
      postalCode: [user.postalCode],
      phoneNumber: [user.phoneNumber]
    });
    this.profileForm.disable();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    this.selectedFile = file;
    const { maxSizeMB, maxWidth, maxHeight } = this.appConfigService.imageUploadConfig;

    if (file.size > maxSizeMB * 1024 * 1024) {
      alert(this.appConfigService.messages.fileTooLarge(maxSizeMB));
    }

    const img = new Image();
    const objectUrl = URL.createObjectURL(file);
    img.src = objectUrl;
    img.onload = () => {

      if (img.width > maxWidth || img.height > maxHeight) {
        alert(this.appConfigService.messages.imageTooBig(maxWidth, maxHeight));
        input.value = '';
        URL.revokeObjectURL(objectUrl);
        return;
      }
      this.tempImageUrl.set(objectUrl);
    }
  }

  toggleEdit(){
    this.editMode = !this.editMode;
    if (this.editMode) {
      this.profileForm.enable();
    } else {
      this.profileForm.disable();
      this.profileForm.patchValue(this.user!);
      this.tempImageUrl.set("");
    }
  }

  onSubmit(): void {
    if (this.profileForm.valid) {
      const updatedUser: Partial<MemberResponseDTO> = this.profileForm.value;
      if(this.tempImageUrl()){
        const formData = new FormData();
        formData.append('file', this.selectedFile!);
        this.profileService.uploadProfilePicture(formData, this.authStore.id()).subscribe({
          next: (res) => {
            console.log('Uploaded', res);
          },
          error: (err) => {
            alert('Upload failed.');
            console.error(err);
          }
        });
      }

      this.profileService.updateMember(updatedUser, this.authStore.id()).subscribe({
        next: (res) => {
          console.log('Member Update Succesfull', res);
          this.getMember(this.authStore.id())
          this.toggleEdit()
        },
        error: (err) => {
          alert('Member Update failed.');
          console.error(err);
        }
      })
    }
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Logout failed', err);
      }
    });
    this.userProfileService.revoke()
  }
}
