import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'
import { ActivatedRoute, Router } from '@angular/router';
import {AppointmentService} from './appointment.service';

@Component({
  selector: 'app-confirm-appointment',
  templateUrl: './confirm-appointment.component.html',
  styleUrls: ['./confirm-appointment.component.css'],
  imports: [CommonModule],
})
export class ConfirmAppointmentComponent implements OnInit {
  message: string = '';
  error: string = '';
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private appointmentService: AppointmentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'Invalid verification link.';
      this.loading = false;
      return;
    }

    this.appointmentService.confirmAppointment(+id).subscribe({
      next: () => {
        this.message = 'Your appointment has been confirmed!';
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 400) {
          this.error = 'This confirmation link is invalid.';
        }
        else if (err.status === 410) {
          this.error = 'This confirmation link has expired. Please request a new one.'
        }
        else {
          this.error = 'An unexpected error occurred.';
        }
      }
    });
  }

  goToHomePage(): void {
    this.router.navigate(['/']);
  }
}
