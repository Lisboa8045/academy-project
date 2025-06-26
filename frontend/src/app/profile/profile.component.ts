import { Component, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ProfileService } from './profile.service';
import { AuthStore } from '../auth/auth.store';
import { AuthService } from '../auth/auth.service';
import { LoadingComponent } from '../loading/loading.component';
import { MemberResponseDTO } from '../auth/member-response-dto.model';
import { UserProfileService } from './user-profile.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { NgIf } from '@angular/common';
import { AppConfigService } from '../shared/app-config.service';
import { strongPasswordValidator } from '../shared/validators/password.validator';
import { noSpecialCharsValidator } from '../shared/validators/no-special-chars.validator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { snackBarSuccess } from '../shared/snackbar/snackbar-success';
import { snackBarError } from '../shared/snackbar/snackbar-error';
import {passwordsMatchValidator} from '../shared/validators/password-match-validator';

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
export class ProfileComponent {
  user: MemberResponseDTO | undefined;
  loading = signal(false);
  readonly imageUrl = inject(UserProfileService).imageUrl;
  tempImageUrl = signal("");
  profileForm!: FormGroup;
  editMode = false;
  editPasswordMode = false;
  selectedFile: File | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    protected authStore: AuthStore,
    private authService: AuthService,
    private profileService: ProfileService,
    private userProfileService: UserProfileService,
    private appConfigService: AppConfigService,
    private snackBar: MatSnackBar
  ) {
    effect(() => {
      this.loading.set(true);
      if (this.authStore.id() > -1) this.getMember(this.authStore.id());
    });
  }

  getMember(id: number) {
    this.profileService.getMemberById(id).subscribe({
      next: (res: MemberResponseDTO) => {
        this.loading.set(false);
        this.user = res;
        this.loadForm(this.user);
      },
      error: (err: any) => {
        this.loading.set(false);
        console.error('Member Retrieval Failed', err);
      }
    });
  }

  loadForm(user: MemberResponseDTO) {
    this.profileForm = this.fb.group({
      username: [user.username, [Validators.required, noSpecialCharsValidator()]],
      email: [user.email, Validators.required],
      address: [user.address],
      postalCode: [user.postalCode, Validators.pattern(/^[0-9]{4}-[0-9]{3}$/)],
      phoneNumber: [user.phoneNumber, Validators.pattern(/^\+[0-9]{12}$/)],
      oldPassword: null,
      newPassword: null,
      confirmPassword: null
    }, { validators: passwordsMatchValidator });

    this.profileForm.disable();
  }

  /*private passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('newPassword')?.value;
    const confirm = group.get('confirmPassword')?.value;
    if (!password || !confirm) return null;
    return password === confirm ? null : { passwordsMismatch: true };
  }

   */

  private updatePasswordValidators() {
    if (this.editPasswordMode) {
      this.profileForm.get('oldPassword')?.setValidators([Validators.required]);
      this.profileForm.get('newPassword')?.setValidators([Validators.required, strongPasswordValidator()]);
      this.profileForm.get('confirmPassword')?.setValidators([Validators.required]);
    } else {
      this.profileForm.get('oldPassword')?.clearValidators();
      this.profileForm.get('newPassword')?.clearValidators();
      this.profileForm.get('confirmPassword')?.clearValidators();
    }
    this.profileForm.get('oldPassword')?.updateValueAndValidity();
    this.profileForm.get('newPassword')?.updateValueAndValidity();
    this.profileForm.get('confirmPassword')?.updateValueAndValidity();
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
      if (!this.editMode) this.toggleEdit();
    };
  }

  clearImage() {
    this.tempImageUrl.set("");
  }

  toggleEdit() {
    this.editMode = !this.editMode;
    this.editPasswordMode = false;
    this.updatePasswordValidators();
    if (this.editMode) {
      this.profileForm.enable();
    } else {
      this.profileForm.disable();
      this.tempImageUrl.set("");
      this.profileForm.patchValue(this.user!);
    }
  }

  togglePasswordEditMode() {
    this.editPasswordMode = !this.editPasswordMode;
    this.updatePasswordValidators();
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    const updatedUser: Partial<MemberResponseDTO> = this.profileForm.value;
    if (this.tempImageUrl()) {
      const formData = new FormData();
      formData.append('file', this.selectedFile!);
      this.profileService.uploadProfilePicture(formData, this.authStore.id()).subscribe({
        next: () => console.log('Uploaded'),
        error: (err) => {
          alert('Upload failed.');
          console.error(err);
        }
      });
    }

    this.profileService.updateMember(updatedUser, this.authStore.id()).subscribe({
      next: (res) => {
        snackBarSuccess(this.snackBar, 'Member Updated Successfully');
        this.getMember(this.authStore.id());
        this.toggleEdit();
      },
      error: (err) => {
        snackBarError(this.snackBar, 'Member Update failed. ' + err.error);
        console.error(err);
      }
    });
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        this.userProfileService.revoke();
        this.router.navigate(['/'])
      },
      error: (err) => console.error('Logout failed', err)
    });
  }
}
