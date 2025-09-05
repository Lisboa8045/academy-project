import {Component, effect, OnInit, signal, WritableSignal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ProfileService} from './profile.service';
import {AuthStore} from '../auth/auth.store';
import {AuthService} from '../auth/auth.service';
import {LoadingComponent} from '../loading/loading.component';
import {MemberResponseDTO} from '../auth/member-response-dto.model';
import {UserProfileService} from './user-profile.service';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {CommonModule, DecimalPipe, NgIf} from '@angular/common';
import {AppConfigService} from '../shared/app-config.service';
import {strongPasswordValidator} from '../shared/validators/password.validator';
import {noSpecialCharsValidator} from '../shared/validators/no-special-chars.validator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarSuccess} from '../shared/snackbar/snackbar-success';
import {snackBarError} from '../shared/snackbar/snackbar-error';
import {passwordsMatchValidator} from '../shared/validators/password-match-validator';
import {MyServicesComponent} from '../service/my-services/my-services.component';
import {ConfirmationModalComponent} from '../shared/confirmation-component/confirmation-modal.component';
import {MemberStatusEnum} from '../models/member-status-enum.model';
import {ServiceReviewComponent} from '../service/service-review/service-review.component';
import {snackBarInfo} from '../shared/snackbar/snackbar-info';
import {ReviewAnalyseComponent} from '../service/review-analyse/review-analyse/review-analyse.component';
import {CountdownSnackbarComponent} from '../shared/snackbar/count-down-snackbar';

@Component({
  selector: 'app-profile',
  imports: [
    LoadingComponent,
    ReactiveFormsModule,
    NgIf,
    MyServicesComponent,
    ConfirmationModalComponent,
    MyServicesComponent,
    ServiceReviewComponent,
    DecimalPipe,
    CommonModule,
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  user: MemberResponseDTO | undefined;
  loading = signal(false);
  imageUrl:WritableSignal<string|null> = signal("");
  tempImageUrl = signal("");
  profileForm!: FormGroup;
  editMode = false;
  editPasswordMode = false;
  selectedFile: File | null = null;
  showDeleteModal = false;

  protected readonly MemberStatusEnum = MemberStatusEnum;
  upgradeWorkerRole = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    protected authStore: AuthStore,
    private authService: AuthService,
    private profileService: ProfileService,
    private userProfileService: UserProfileService,
    private appConfigService: AppConfigService,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
  ) {
    effect(() => {
      if (this.route.snapshot.paramMap.get('id')) return;
      const id = this.authStore.id();
      if (id > 0) {
        this.loading.set(true);
        this.getMember(id);
      }
    });
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      let id: number;

      if (idParam) {
        id = Number(idParam);
      } else {
        id = this.authStore.id();
      }

      if (!id || id <= 0) {
        this.loading.set(false);
        this.router.navigate(['/not-found']);
        return;
      }

      this.user = undefined;
      this.imageUrl.set("");
      this.tempImageUrl.set("");
      this.loading.set(true);

      this.getMember(id);
    });
  }

  getMember(id: number) {
    this.profileService.getMemberById(id).subscribe({
      next: (res: MemberResponseDTO) => {
        this.loading.set(false);
        this.user = res;
        this.userProfileService.getImage(res.profilePicture).then((url) => {
          const safeUrl = url ?? "";
          this.imageUrl.set(safeUrl);
          if(id === this.authStore.id()) {
            this.userProfileService.setImageUrl(safeUrl);
          }
        });
        this.loadForm(this.user);
      },
      error: (err: any) => {
        this.loading.set(false);
        console.error('Member Retrieval Failed', err);
        this.router.navigate(['/not-found']);
      }
    });
  }

  loadForm(user: MemberResponseDTO) {
    this.profileForm = this.fb.group({
      username: [user.username, [Validators.required, noSpecialCharsValidator(), Validators.minLength(4), Validators.maxLength(20)]],
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

  private updatePasswordValidators() {
    if (this.editPasswordMode) {
      this.profileForm.get('oldPassword')?.setValidators([Validators.required]);
      this.profileForm.get('newPassword')?.setValidators([Validators.required, Validators.minLength(8), Validators.maxLength(64), strongPasswordValidator()]);
      this.profileForm.get('confirmPassword')?.setValidators([Validators.required, Validators.maxLength(64)]);
    } else {
      this.profileForm.get('oldPassword')?.clearValidators();
      this.profileForm.get('newPassword')?.clearValidators();
      this.profileForm.get('confirmPassword')?.clearValidators();
    }
    this.profileForm.get('oldPassword')?.updateValueAndValidity();
    this.profileForm.get('newPassword')?.updateValueAndValidity();
    this.profileForm.get('confirmPassword')?.updateValueAndValidity();
  }

  private validatorsForWorker() {
    this.profileForm.get('address')?.setValidators([Validators.required]);
    this.profileForm.get('postalCode')?.setValidators([Validators.required, Validators.pattern(/^[0-9]{4}-[0-9]{3}$/)]);
    this.profileForm.get('phoneNumber')?.setValidators([Validators.required, Validators.pattern(/^\+[0-9]{12}$/)]);
  }

  private updateWorkerRoleValidators() {
    if (this.upgradeWorkerRole) {
      this.validatorsForWorker();
    } else {
      this.profileForm.get('address')?.clearValidators();
      this.profileForm.get('postalCode')?.clearValidators();
      this.profileForm.get('postalCode')?.setValidators([Validators.pattern(/^[0-9]{4}-[0-9]{3}$/)])
      this.profileForm.get('phoneNumber')?.clearValidators();
      this.profileForm.get('phoneNumber')?.setValidators([Validators.pattern(/^\+[0-9]{12}$/)])
    }

    this.profileForm.get('address')?.updateValueAndValidity();
    this.profileForm.get('postalCode')?.updateValueAndValidity();
    this.profileForm.get('phoneNumber')?.updateValueAndValidity();
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
      if (this?.user?.role === "WORKER") {
        this.validatorsForWorker();
      }
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
  toggleUpgradeRoleEditMode() {
    this.upgradeWorkerRole = !this.upgradeWorkerRole;
    this.updateWorkerRoleValidators();
    if (this.upgradeWorkerRole) {
      snackBarInfo(this.snackBar, 'Please validate your personal information');
      this.profileForm.enable();
    } else {
      this.profileForm.disable();
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    const updatedUser: Partial<MemberResponseDTO> = this.profileForm.value;

    const doUpdateMember = () => {
      const payload = this.upgradeWorkerRole
          ? { ...updatedUser, roleId: 3 }
          : updatedUser;

      this.profileService.updateMember(payload, this.authStore.id()).subscribe({
        next: () => {
          if (this.upgradeWorkerRole) {
            snackBarSuccess(this.snackBar, 'Member is now a Worker');
            this.authStore.setRole('WORKER');
            this.toggleUpgradeRoleEditMode();
          } else {
            snackBarSuccess(this.snackBar, 'Member Updated Successfully');
            this.toggleEdit();
          }
          this.authStore.setUsername(updatedUser.username ?? "");
          this.getMember(this.authStore.id()); // refresh view
        },
        error: (err) => {
          snackBarError(this.snackBar, 'Member Update failed. ' + err.error);
          console.error(err);
        }
      });
    };

    if (this.tempImageUrl()) {
      const formData = new FormData();
      formData.append('file', this.selectedFile!);

      this.profileService.uploadProfilePicture(formData, this.authStore.id()).subscribe({
        next: () => {
          this.authStore.setProfilePicture(this.tempImageUrl());
          doUpdateMember();
        },
        error: (err) => {
          snackBarError(this.snackBar, 'Upload failed. ' + err.error);
          console.error(err);
        }
      });
    } else {
      doUpdateMember();
    }
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

  canEdit(): Boolean{
    return this.user ? this.authStore.id() === this.user.id : false;
  }

  protected readonly Math = Math;

  deleteMember() {
    this.showDeleteModal = true;
  }

  onDeleteConfirmed() {
    this.profileService.deleteMember(this.authStore.id()).subscribe({
      next: () => {
        this.showDeleteCountdownAndLogout();
      },
      error: (err) => {
        snackBarError(this.snackBar, 'Delete failed: ' + (err.error?.message || err.statusText));
      }
    });
  }

  private showDeleteCountdownAndLogout() {
    let seconds = 5;
    const message = `Account deleted successfully. Logging out in ${seconds}...`;

    const snackBarRef = this.snackBar.openFromComponent(CountdownSnackbarComponent, {
      data: { message },
      duration: undefined, // keep open manually
      panelClass: ['success-snackbar'],
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });

    const interval = setInterval(() => {
      seconds--;
      snackBarRef.instance.data.message = `Account deleted successfully. Logging out in ${seconds}...`;

      // Force Angular change detection
      snackBarRef.instance = {...snackBarRef.instance};

      if (seconds <= 0) {
        clearInterval(interval);
        this.snackBar.dismiss();
        this.authService.logout().subscribe({
          next: () => {
            this.userProfileService.revoke();
            this.router.navigate(['/auth']);
          },
          error: () => {
            this.userProfileService.revoke();
            this.router.navigate(['/auth']);
          }
        });
      }
    }, 1000);
  }
}
