  import { Component, OnInit } from '@angular/core';
  import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
  import { CommonModule } from '@angular/common';
  import { ServiceApiService } from '../shared/service-api.service';
  import { ScheduleApiService } from './schedule.service';
  import { SlotModel } from '../schedule/slot.model';
  import { ServiceModel } from '../service/service.model';
  import { AppointmentModel } from './appointment.model';
  import {ServiceTypeModel} from './service-type.model';
  import { addDays, startOfWeek, endOfWeek, isSameDay } from 'date-fns';

  @Component({
    selector: 'app-schedule',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule],
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
    providerSearchTerm = '';
    filteredSlots: SlotModel[] = [];
    providers: string[] = [];
    selectedProvider: string | null = null;
    currentWeekStart: Date = startOfWeek(new Date(), { weekStartsOn: 1 }); // Segunda-feira
    currentWeekEnd: Date = endOfWeek(new Date(), { weekStartsOn: 1 });
    weekDays: Date[] = [];
    weeklySlots: { [key: string]: SlotModel[] } = {}; // chave: YYYY-MM-DD
    providerOptions: SlotModel[] = [];
    showProviderModal: boolean = false;

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

    onSearchChange(event: Event) {
      const input = event.target as HTMLInputElement;
      this.searchTerm = input.value.toLowerCase();
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


    onProviderSearchChange(event: Event) {
      const input = event.target as HTMLInputElement;
      this.providerSearchTerm = input.value.toLowerCase();
      this.applySlotFilters();
    }

    applySlotFilters() {
      this.filteredSlots = this.slots.filter(slot =>
        slot.providerName.toLowerCase().includes(this.providerSearchTerm)
      );
      this.organizeSlotsByDay(); // <-- Atualizar visualização
    }

    selectSlot(slot: SlotModel) {
      const sameTimeSlots = this.slots.filter(s => s.start === slot.start);

      if (sameTimeSlots.length > 1) {
        // Mostrar a lista de prestadores disponíveis (passo 'provider')
        this.providerOptions = sameTimeSlots;
        this.currentStep = 'provider';
      } else {
        this.selectedSlot = slot;
        this.currentStep = 'confirmation';
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

    onServiceTypeChange(event: Event) {
      const selectElement = event.target as HTMLSelectElement;
      const value = selectElement.value;

      console.log('Valor selecionado (value):', value); // <== AQUI

      if (!value) {
        this.filteredServices = this.services;
      } else {
        const selectedTypeId = parseInt(value, 10);
        console.log('Filtrando por serviceTypeId:', selectedTypeId); // <== E AQUI

        this.filteredServices = this.services.filter(s => {
          console.log('Comparando:', s.serviceTypeId, '===', selectedTypeId);
          return s.serviceTypeId === selectedTypeId;
        });
      }

      this.form.get('serviceId')?.setValue(null);
      this.selectedServiceId = undefined;
      this.slots = [];
    }



    confirmAppointment() {
      if (!this.selectedSlot || !this.form.value.serviceId) return;

      const serviceId = this.form.value.serviceId;
      const providerId = this.selectedSlot.providerId;

      this.scheduleApi.getServiceProviderId(serviceId, providerId).subscribe({
        next: (serviceProviderId: number) => {
          const appointment: AppointmentModel = {
            serviceProviderId: serviceProviderId,
            startDateTime: this.selectedSlot!.start,
            endDateTime: this.selectedSlot!.end,
            status: 'CONFIRMED'
          };

          this.scheduleApi.confirmAppointment(appointment).subscribe({
            next: () => alert('Marcação efetuada com sucesso!'),
            error: err => alert('Erro ao marcar: ' + err.message)
          });
        },
        error: err => alert('Erro ao obter prestador de serviço: ' + err.message)
      });
    }

    showProviderSelectionModal(slots: SlotModel[]) {
      this.providerOptions = slots;
      this.showProviderModal = true;
    }

    selectProviderSlot(slot: SlotModel) {
      this.selectedSlot = slot;
      this.showProviderModal = false;
      this.currentStep = 'confirmation';
      this.showConfirmationModal = true;
    }
  }
``
