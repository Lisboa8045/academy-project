import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ServiceApiService } from '../shared/service-api.service';
import { ScheduleApiService } from './schedule.service';
import { SlotModel } from '../schedule/slot.model';
import { ServiceModel } from '../service/service.model';
import { AppointmentModel } from './appointment.model';

@Component({
  selector: 'app-schedule',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './schedule.component.html',
  styleUrls: ['./schedule.component.css']
})
export class ScheduleComponent implements OnInit {
  form: FormGroup;
  services: ServiceModel[] = [];
  slots: SlotModel[] = [];
  selectedSlot?: SlotModel;

  constructor(
    private fb: FormBuilder,
    private serviceApi: ServiceApiService,
    private scheduleApi: ScheduleApiService
  ) {
    this.form = this.fb.group({
      serviceId: [null]
    });
  }

  ngOnInit(): void {
    this.loadServices();
  }

  loadServices() {
    this.serviceApi.searchServices().subscribe({
      next: (res: ServiceModel[]) => {
        this.services = res;
        console.log('Serviços carregados:', this.services);
      },
      error: err => console.error('Erro ao carregar serviços:', err)
    });
  }

  onServiceChange() {
    const selectedServiceId = this.form.value.serviceId;
    this.selectedSlot = undefined;
    this.slots = [];

    if (selectedServiceId) {
      this.scheduleApi.getFreeSlots(selectedServiceId).subscribe({
        next: data => this.slots = data,
        error: err => console.error('Erro ao carregar slots:', err)
      });
    }
  }

  selectService(service: ServiceModel) {
    this.form.get('serviceId')?.setValue(service.id);
    this.onServiceChange();
  }

  selectSlot(slot: SlotModel) {
    this.selectedSlot = slot;
  }

  confirmAppointment() {

    const selectedServiceId = this.form.value.serviceId;
    const selectedSlot = this.form.value.slot;

    if (!this.selectedSlot || !this.selectedSlot) return;

    this.scheduleApi.confirmAppointment().subscribe({
      next: () => alert('Marcação efetuada com sucesso!'),
      error: err => alert('Erro ao marcar: ' + err.message)
    });
  }
}
