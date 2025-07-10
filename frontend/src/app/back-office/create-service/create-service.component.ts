import {Component, OnInit, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {buildServiceForm} from '../service-form/service-form.component';
import {ServiceApiService} from '../../shared/service-api.service';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-create-service',
  imports: [
    NgIf,
    ReactiveFormsModule
  ],
  templateUrl: './create-service.component.html',
  styleUrl: './create-service.component.css'
})
export class CreateServiceComponent implements OnInit {
  form!: FormGroup;
  constructor(private fb: FormBuilder, private serviceApi: ServiceApiService) {}

  ngOnInit() {
    this.form = buildServiceForm(this.fb);
  }

  submit() {
    if(this.form.valid) {
      this.serviceApi.createService(this.form.value).subscribe();
    }
  }


}
