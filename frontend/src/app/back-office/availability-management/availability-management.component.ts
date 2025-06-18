import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface WeeklyAvailability {
  day: string;
  enabled: boolean;
  startTime: string; // formato HH:mm
  endTime: string;   // formato HH:mm
}

@Component({
  selector: 'app-availability-management',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './availability-management.component.html',
  styleUrls: ['./availability-management.component.css']
})
export class AvailabilityManagementComponent {
  weekDays = [
    'Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'
  ];

  weeklyAvailabilities: WeeklyAvailability[] = this.weekDays.map(day => ({
    day,
    enabled: false,
    startTime: '08:00',
    endTime: '17:00'
  }));

  period = {
    startDate: '',
    endDate: ''
  };

  gerarDisponibilidades() {
    console.log('Configuração semanal:', this.weeklyAvailabilities);
    console.log('Período:', this.period);
  }
}
