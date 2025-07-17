import { Component, EventEmitter, Input, Output } from '@angular/core';
import {WeekNavigationComponent} from '../../../shared/week-navigation/week-navigation.component';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-availability-header',
  templateUrl: './availability-header.component.html',
  styleUrls: ['./availability-header.component.css'],
  imports: [
    WeekNavigationComponent,
    NgIf
  ],
  standalone: true
})
export class AvailabilityHeaderComponent {
  @Input() isSettingDefaults = false;
  @Input() hasChanges = false;
  @Output() saveChanges = new EventEmitter<void>();
}
