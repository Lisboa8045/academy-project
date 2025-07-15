import { Injectable } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { GlobalConfiguration } from './global-configuration.model';

@Injectable({ providedIn: 'root' })
export class GlobalConfigurationFormService {
  constructor(readonly fb: FormBuilder) {}

  buildForm(): FormGroup {
    return this.fb.group({
      configurations: this.fb.array([]),
    });
  }

  addConfigurations(form: FormGroup, configs: GlobalConfiguration[]): void {
    const array = form.get('configurations') as FormArray;
    configs.forEach((config) => {
      array.push(
        this.fb.group({
          configName: [{ value: config.configName, disabled: true }],
          configValue: [config.configValue, Validators.required],
          configType: [{ value: config.configType, disabled: true }],
        })
      );
    });
  }

  extractPayload(form: FormGroup): GlobalConfiguration[] {
    const array = form.get('configurations') as FormArray;
    return array.controls.map((group) => ({
      configName: group.get('configName')?.value,
      configKey: group.get('configKey')?.value,
      configValue: group.get('configValue')?.value,
      configType: group.get('configType')?.value,
    }));
  }
}
