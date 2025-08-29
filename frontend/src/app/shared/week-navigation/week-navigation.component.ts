
import {Component, OnInit, output} from '@angular/core';
import { WeekNavigationService } from '../week-navigation.service';
import {DatePipe} from '@angular/common';

@Component({
  selector: 'app-week-navigation',
  templateUrl: './week-navigation.component.html',
  imports: [
    DatePipe
  ],
  styleUrls: ['./week-navigation.component.css']
})
export class WeekNavigationComponent implements OnInit {
  currentDate!: Date;
  currentWeekStart!: Date;
  currentWeekEnd!: Date;

  onPreviousWeekEmitter = output<void>();
  onNextWeekEmitter = output<void>();
  onCurrentWeekEmitter = output<void>();

  constructor(private weekNavigationService: WeekNavigationService) {}

  ngOnInit() {
    this.weekNavigationService.currentDate$.subscribe(date => {
      this.currentDate = date;
      this.currentWeekStart = this.weekNavigationService.getWeekStart(date);
      this.currentWeekEnd = this.weekNavigationService.getWeekEnd(date);
    });
  }

  onPreviousWeek() {
    this.weekNavigationService.goToPreviousWeek();
    this.onPreviousWeekEmitter.emit();
  }

  onNextWeek() {
    this.weekNavigationService.goToNextWeek();
    this.onNextWeekEmitter.emit();
  }

  onCurrentWeek() {
    this.weekNavigationService.resetToCurrentWeek();
    this.onCurrentWeekEmitter.emit();
  }
}
