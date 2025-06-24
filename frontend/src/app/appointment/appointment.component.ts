import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import {
  AppointmentResponseDetailedDTO,
  AppointmentResponseDTO
} from '../appointment-history/appointment-response-dto.model';
import {AppointmentService} from '../appointment-history/appointment.service';

@Component({
  selector: 'app-appointment',
  templateUrl: './appointment.component.html',
  styleUrls: ['./appointment.component.css'],
  standalone: true,
  imports: [CommonModule]
})
export class AppointmentComponent implements OnInit {
  appointment: AppointmentResponseDetailedDTO | null = null;

  constructor(
    private route: ActivatedRoute,
    private appointmentService: AppointmentService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.appointmentService.getAppointmentById(id).subscribe({
      next: data => this.appointment = data,
      error: err => alert('Error loading appointment.')
    });
  }
}
