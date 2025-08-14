
import { Component, OnInit } from '@angular/core';
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
  }

  onNextWeek() {
    this.weekNavigationService.goToNextWeek();
  }

  onCurrentWeek() {
    this.weekNavigationService.resetToCurrentWeek();
  }
}
