import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ReactiveFormsModule, FormGroup, FormArray} from '@angular/forms';
import { GlobalConfigurationService } from './global-configuration.service';
import { GlobalConfigurationFormService } from './global-configuration-form.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarSuccess} from '../shared/snackbar/snackbar-success';
import {snackBarError} from '../shared/snackbar/snackbar-error';

@Component({
  selector: 'app-global-configuration-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './global-configuration-edit.component.html',
  styleUrl: 'global-configuration-edit.component.css'
})
export class GlobalConfigurationEditComponent implements OnInit {
  readonly configService = inject(GlobalConfigurationService);
  readonly formService = inject(GlobalConfigurationFormService);
  readonly snackBar = inject(MatSnackBar);

  configForm: FormGroup = this.formService.buildForm();
  showPasswords: boolean[] = [];

  ngOnInit(): void {
    this.configService.getAll().subscribe((configs) => {
      this.formService.addConfigurations(this.configForm, configs);
      this.showPasswords = configs.map(() => false);
    });
  }

  submit(): void {
    if (this.configForm.valid) {
      const payload = this.formService.extractPayload(this.configForm);
      this.configService.editConfigs(payload).subscribe({
        next: () => {
          snackBarSuccess(this.snackBar, 'Configurations updated!');
        },
        error: (err) => {
          snackBarError(this.snackBar, err?.error?.message ?? 'Failed to update configurations. Please try again.')
        },
      });
    }
  }

  get configurations() {
    return this.configForm.get('configurations') as FormArray;
  }

  configTypeDescription(type: string): string {
    switch (type) {
      case 'INT':
        return 'Integer number';
      case 'STRING':
        return 'Text';
      case 'BOOLEAN':
        return 'True/False';
      case 'PASSWORD':
        return 'Password';
      default:
        return type;
    }
  }

  togglePasswordVisibility(index: number): void {
    this.showPasswords[index] = !this.showPasswords[index];
  }

}
