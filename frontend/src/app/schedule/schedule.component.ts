import {Component, inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import { addDays, startOfWeek, endOfWeek, isSameDay } from 'date-fns';
import { ServiceApiService } from '../shared/service-api.service';
import { ScheduleApiService } from './schedule.service';
import { SlotModel } from '../models/slot.model';
import { ServiceModel } from '../service/service.model';
import { AppointmentModel } from '../models/appointment.model';
import {CommonModule} from '@angular/common';
import {ProviderSelectionModalComponent} from './providerSelectionModalComponent/provider-selection-modal.component';
import {ConfirmationModalComponent} from './confirmationModalComponent/confirmation-modal.component';
import {SlotSelectionComponent} from './slotSelectionComponent/slot-selection.component';
import { ServiceProviderModel } from '../models/service-provider.model';
import {AuthStore} from '../auth/auth.store';
import { ActivatedRoute, Router } from '@angular/router';
import {ServiceDetailsService} from "../service/service-details.service";

@Component({
  selector: 'app-schedule',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SlotSelectionComponent,
    ConfirmationModalComponent,
    ProviderSelectionModalComponent,
  ],
  templateUrl: './schedule.component.html',
  styleUrls: ['./schedule.component.css']
})
export class ScheduleComponent implements OnInit {
  form: FormGroup;
  services: ServiceModel[] = [];
  slots: SlotModel[] = [];
  selectedSlot?: SlotModel;
  selectedServiceId?: number;
  showConfirmationModal = false;
  currentStep: 'service' | 'slots' | 'provider' | 'confirmation' = 'service';
  filteredSlots: SlotModel[] = [];
  providers: string[] = [];
  selectedProvider: string | null = null;
  currentWeekStart: Date = startOfWeek(new Date(), { weekStartsOn: 1 });
  currentWeekEnd: Date = endOfWeek(new Date(), { weekStartsOn: 1 });
  weekDays: Date[] = [];
  weeklySlots: { [key: string]: SlotModel[] } = {};
  providerOptions: SlotModel[] = [];
  showProviderModal = false;
  readonly username = inject(AuthStore).username;

  constructor(
      private fb: FormBuilder,
      private serviceApi: ServiceApiService,
      private scheduleApi: ScheduleApiService,
      private serviceDetailsService: ServiceDetailsService,
      private route: ActivatedRoute,
      private router: Router
) {
    this.form = this.fb.group({
      serviceId: [null]
    });
  }

  get selectedService(): ServiceModel | undefined {
    return this.services.find(s => s.id === this.form.value.serviceId);
  }

  updateWeekDays() {
    this.weekDays = [];
    for (let i = 0; i < 7; i++) {
      this.weekDays.push(addDays(this.currentWeekStart, i));
    }
    this.organizeSlotsByDay();
  }

  goToNextWeek() {
    this.currentWeekStart = addDays(this.currentWeekStart, 7);
    this.currentWeekEnd = addDays(this.currentWeekEnd, 7);
    this.updateWeekDays();
  }

  goToPreviousWeek() {
    this.currentWeekStart = addDays(this.currentWeekStart, -7);
    this.currentWeekEnd = addDays(this.currentWeekEnd, -7);
    this.updateWeekDays();
  }

  organizeSlotsByDay() {
    this.weeklySlots = {};

    for (const day of this.weekDays) {
      const key = day.toISOString().split('T')[0]; // YYYY-MM-DD
      const daySlots = this.filteredSlots.filter(slot =>
        isSameDay(new Date(slot.start), day)
      );

      // Group by start hour (ignoring provider)
      const uniqueSlotsMap = new Map<string, SlotModel[]>();

      for (const slot of daySlots) {
        const slotTimeKey = new Date(slot.start).toISOString().substring(0, 16); // YYYY-MM-DDTHH:mm

        if (!uniqueSlotsMap.has(slotTimeKey)) {
          uniqueSlotsMap.set(slotTimeKey, []);
        }

        uniqueSlotsMap.get(slotTimeKey)!.push(slot);
      }

      // Store one slot per time group (e.g., show just the first provider initially)
      this.weeklySlots[key] = Array.from(uniqueSlotsMap.values()).map(group => group[0]);
    }
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const serviceId = params.get('id');
      if (serviceId) {
        this.selectedServiceId = +serviceId;
        this.loadServiceAndSlots(this.selectedServiceId);
      }
    });
  }

  loadServiceAndSlots(serviceId: number) {
    // Fetch the service details
    this.serviceDetailsService.getServiceById(serviceId).subscribe({
      next: (service) => {
        this.selectedServiceId = service.id;
        this.form.get('serviceId')?.setValue(service.id); // Keeps form state updated

        this.selectedSlot = undefined;
        this.slots = [];
        this.scheduleApi.getFreeSlots(service.id).subscribe({
          next: data => {
            this.slots = data;
            this.providers = [...new Set(data.map(slot => slot.providerName))];
            this.filteredSlots = data;
            this.currentStep = 'slots';
            this.currentWeekStart = startOfWeek(new Date(), { weekStartsOn: 1 });
            this.currentWeekEnd = endOfWeek(new Date(), { weekStartsOn: 1 });
            this.updateWeekDays();
          },
          error: err => console.error('Failed to load slots', err)
        });
      },
      error: () => {
        console.error('Failed to load service');
      }
    });
  }

  filterByProvider(provider: string | null) {
    this.selectedProvider = provider;
    this.filteredSlots = this.slots.filter(slot =>
      !provider || slot.providerName === provider
    );
    this.organizeSlotsByDay();
  }

  selectSlot(slot: SlotModel) {
    const slotTime = new Date(slot.start).toISOString();
    const sameTimeSlots = this.slots.filter(s =>
      new Date(s.start).toISOString() === slotTime
    );

    const uniqueProviderMap = new Map<string, SlotModel>();
    for (const s of sameTimeSlots) {
      if (!uniqueProviderMap.has(s.providerName)) {
        uniqueProviderMap.set(s.providerName, s);
      }
    }

    this.providerOptions = Array.from(uniqueProviderMap.values());

    if (this.selectedProvider && slot.providerName === this.selectedProvider) {
      this.selectedSlot = slot;
      this.currentStep = 'confirmation';
      this.showProviderModal = false;
      this.showConfirmationModal = true;
      return;
    }

    if (this.providerOptions.length > 1) {
      // Se clicou no mesmo provider, "desmarca" e abre modal para escolha
      if (this.selectedSlot && this.selectedSlot.providerName === slot.providerName) {
        this.selectedSlot = undefined;
        this.showProviderModal = true;
      } else {
        this.selectedSlot = this.providerOptions.find(p => p.providerName === slot.providerName) ?? undefined;
        this.showProviderModal = true;
      }
      this.currentStep = 'provider';
    } else {
      // Só um provider -> vai direto à confirmação
      this.selectedSlot = this.providerOptions[0];
      this.currentStep = 'confirmation';
      this.showProviderModal = false;
      this.showConfirmationModal = true;
    }
  }

  cancelModal() {
    this.showConfirmationModal = false;
    this.currentStep = 'slots';
  }

  confirmInModal() {
    this.showConfirmationModal = false;
    this.confirmAppointment();
    this.currentStep = 'service';
  }

  confirmAppointment() {
    if (!this.selectedSlot || !this.form.value.serviceId) return;

    const serviceId = this.form.value.serviceId;
    const providerId = this.selectedSlot.providerId;

    this.scheduleApi.getServiceProvider(serviceId, providerId).subscribe({
      next: (serviceProvider: ServiceProviderModel) => {
        const appointment: AppointmentModel = {
          serviceProviderId: serviceProvider.id,
          startDateTime: this.selectedSlot!.start,
          endDateTime: this.selectedSlot!.end,
          status: 'PENDING'
        };

        this.scheduleApi.confirmAppointment(appointment).subscribe({
          next: () => alert('Appointment scheduled successfully!'),
          error: err => alert('Error scheduling appointment: ' + err.message)
        });
      },
      error: err => alert('Error obtaining service provider: ' + err.message)
    });
  }

  selectProviderSlot(slot: SlotModel) {
    this.selectedSlot = slot;
    this.showProviderModal = false;
    this.showConfirmationModal = true;
    this.currentStep = 'confirmation';
  }

  backToServices() {
    this.router.navigate(['/services']);
  }

}
