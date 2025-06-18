import {Component, inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import { addDays, startOfWeek, endOfWeek, isSameDay } from 'date-fns';
import { ServiceApiService } from '../shared/service-api.service';
import { ScheduleApiService } from './schedule.service';
import { SlotModel } from '../models/slot.model';
import { ServiceModel } from '../service/service.model';
import { AppointmentModel } from '../models/appointment.model';
import { ServiceTypeModel } from '../models/service-type.model';
import {CommonModule, DatePipe} from '@angular/common';
import {ServiceSearchComponent} from './serviceSearchComponent/service-search.component';
import {ServiceListComponent} from './serviceListComponent/service-list.component';
import {ProviderSelectionModalComponent} from './providerSelectionModalComponent/provider-selection-modal.component';
import {ConfirmationModalComponent} from './confirmationModalComponent/confirmation-modal.component';
import {SlotSelectionComponent} from './slotSelectionComponent/slot-selection.component';
import { ServiceProviderModel } from '../models/service-provider.model';
import {AuthStore} from '../auth/auth.store';

@Component({
  selector: 'app-schedule',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ServiceSearchComponent,
    ServiceListComponent,
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
  serviceTypes: ServiceTypeModel[] = [];
  filteredServices: ServiceModel[] = [];
  selectedServiceTypeId: number | null = null;
  searchTerm = '';
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
    private scheduleApi: ScheduleApiService
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
    this.loadServices();
    console.log(this.services);
    this.loadServiceTypes();
  }

  loadServices() {
    this.serviceApi.searchServices().subscribe({
      next: (res: ServiceModel[]) => {
        this.services = res;
        this.filteredServices = res; // inicialmente todos
      },
      error: err => console.error('Erro ao carregar serviços:', err)
    });
  }

  loadServiceTypes() {
    this.scheduleApi.getServiceTypes().subscribe({
      next: (res: ServiceTypeModel[]) => {
        this.serviceTypes = res;
      },
      error: err => console.error('Erro ao carregar tipos de serviço:', err)
    });
  }

  filterByProvider(provider: string | null) {
    this.selectedProvider = provider;
    this.filteredSlots = this.slots.filter(slot =>
      !provider || slot.providerName === provider
    );
    this.organizeSlotsByDay(); // <-- Atualizar visualização
  }

  onSearchChange(searchTerm: string) {
    this.searchTerm = searchTerm.toLowerCase();
    this.applyFilters();
  }

    filterByType(typeId: number | null) {
      this.selectedServiceTypeId = typeId;
      this.applyFilters();
    }

    applyFilters() {
      this.filteredServices = this.services.filter(service => {
        const matchesType =
          this.selectedServiceTypeId == null || service.serviceTypeId === this.selectedServiceTypeId;
        const matchesSearch =
          !this.searchTerm || service.name.toLowerCase().includes(this.searchTerm);
        return matchesType && matchesSearch;
      });

      this.form.get('serviceId')?.setValue(null);
      this.selectedServiceId = undefined;
      this.slots = [];
    }


    selectService(service: ServiceModel) {
      this.form.get('serviceId')?.setValue(service.id);
      this.selectedServiceId = this.form.value.serviceId;
      this.selectedSlot = undefined;
      this.slots = [];

      if (this.selectedServiceId) {
        this.scheduleApi.getFreeSlots(this.selectedServiceId).subscribe({
          next: data => {
            this.slots = data;
            this.providers = [...new Set(data.map(slot => slot.providerName))];
            this.filteredSlots = data;
            this.currentStep = 'slots';
            this.currentWeekStart = startOfWeek(new Date(), { weekStartsOn: 1 });
            this.currentWeekEnd = endOfWeek(new Date(), { weekStartsOn: 1 });
            this.updateWeekDays();
          },
          error: err => console.error('Erro ao carregar slots:', err)
        });
      }
    }

    selectSlot(slot: SlotModel) {
      const slotTime = new Date(slot.start).toISOString();

      // Obter todos slots com o mesmo horário
      const sameTimeSlots = this.slots.filter(s =>
        new Date(s.start).toISOString() === slotTime
      );

      // Criar map único de providers
      const uniqueProviderMap = new Map<string, SlotModel>();
      for (const s of sameTimeSlots) {
        if (!uniqueProviderMap.has(s.providerName)) {
          uniqueProviderMap.set(s.providerName, s);
        }
      }

      this.providerOptions = Array.from(uniqueProviderMap.values());

      // Se filtro por provider estiver ativo E provider do slot for esse filtro
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
      this.currentStep = 'slots'; // Volta para escolher slot
    }

    confirmInModal() {
      this.showConfirmationModal = false;
      this.confirmAppointment();
      this.currentStep = 'service'; // Volta para começar
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
            status: 'CONFIRMED'
          };

          console.log('[LOG] Creating appointment with the following details:');
          console.log('Service Provider ID:', appointment.serviceProviderId);
          console.log('Start DateTime:', appointment.startDateTime);
          console.log('End DateTime:', appointment.endDateTime);
          console.log('Status:', appointment.status);

          this.scheduleApi.confirmAppointment(appointment).subscribe({
            next: () => alert('Marcação efetuada com sucesso!'),
            error: err => alert('Erro ao marcar: ' + err.message)
          });
        },
        error: err => alert('Erro ao obter prestador de serviço: ' + err.message)
      });
    }

    selectProviderSlot(slot: SlotModel) {
      this.selectedSlot = slot;
      this.showProviderModal = false;
      this.showConfirmationModal = true;
      this.currentStep = 'confirmation';
    }

}
