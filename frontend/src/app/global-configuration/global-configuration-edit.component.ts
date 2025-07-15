import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ReactiveFormsModule, FormGroup, FormArray} from '@angular/forms';
import { GlobalConfigurationService } from './global-configuration.service';
import { GlobalConfigurationFormService } from './global-configuration-form.service';
import {ConfigType} from './config-type.enum';

@Component({
  selector: 'app-global-configuration-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './global-configuration-edit.component.html',
})
export class GlobalConfigurationEditComponent implements OnInit {
  readonly configService = inject(GlobalConfigurationService);
  readonly formService = inject(GlobalConfigurationFormService);

  configForm: FormGroup = this.formService.buildForm();

  ngOnInit(): void {
    this.configService.getAll().subscribe((configs) => {
      this.formService.addConfigurations(this.configForm, configs);
    });
  }

  submit(): void {
    if (this.configForm.valid) {
      const payload = this.formService.extractPayload(this.configForm);
      this.configService.editConfigs(payload).subscribe(() => {
        alert('Configurations updated!');
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

}
