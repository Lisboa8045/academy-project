import {AfterViewInit, Component, ViewChild} from '@angular/core';
import { CommonModule } from '@angular/common';
import {FullCalendarComponent, FullCalendarModule} from '@fullcalendar/angular';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { CalendarOptions, DateSelectArg, EventClickArg } from '@fullcalendar/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgSelectComponent } from '@ng-select/ng-select';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FullCalendarModule, ReactiveFormsModule, NgSelectComponent],
  templateUrl: './availability-calendar.component.html',
  styleUrls: ['./availability-calendar.component.css']
})
export class CalendarComponent implements AfterViewInit{
  @ViewChild('calendar') calendarComponent!: FullCalendarComponent;
  calendarOptions: CalendarOptions = {
    initialView: 'timeGridWeek',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    events: [],
    editable: true,
    selectable: true,
    selectMirror: true,
    allDaySlot: false,
    firstDay: 1,
    select: this.handleDateSelect.bind(this),
    eventClick: this.handleEventClick.bind(this),
    dayHeaderContent: this.renderDayHeader.bind(this),
  };

  replicateForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.replicateForm = this.fb.group({
      sourceDate: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      repeatDays: [[], Validators.required]
    });
  }

  selectedRepeatDate: string | null = null;
  showRepeatModal = false;
  weekDays = [
    { label: 'Sunday', value: 0 },
    { label: 'Monday', value: 1 },
    { label: 'Tuesday', value: 2 },
    { label: 'Wednesday', value: 3 },
    { label: 'Thursday', value: 4 },
    { label: 'Friday', value: 5 },
    { label: 'Saturday', value: 6 },
  ];


  ngAfterViewInit(): void {
    (window as any).handleRepeatIconClick = (e: MouseEvent, date: string) => {
      e.stopPropagation();          // Stop bubbling
      e.preventDefault();           // Prevent default browser behavior
      this.handleRepeatClick(date); // Call Angular method
    };

    document.addEventListener('mousedown', (e: any) => {
      if (e.target && e.target.classList.contains('repeat-icon')) {
        e.stopPropagation();
        e.preventDefault();
      }
    }, true);
  }

  renderDayHeader(arg: any) {
    const date = arg.date;
    const dayNum = date.getDate();
    const weekday = date.toLocaleDateString('en-US', { weekday: 'short' });
    const dateStr = date.toISOString().split('T')[0];

    return {
      html: `
      <div class="fc-day-header-inner">
        <div class="day-header-text">
          <div class="day-num">${dayNum}</div>
          <div class="weekday">${weekday}</div>
        </div>
        <button
          type="button"
          class="repeat-icon"
          title="Repeat this day’s availability"
          onclick="handleRepeatIconClick(event, '${dateStr}')"
        >🔁</button>
      </div>
    `
    };
  }

  handleRepeatClick(date: string) {
    console.log("Repeat clicked for", date);
    this.selectedRepeatDate = date;
    this.replicateForm.patchValue({
      sourceDate: date
    });
    this.showRepeatModal = true;
  }

  // Automatically add a new "Available" event
  handleDateSelect(selectInfo: DateSelectArg) {
    console.log('handleDateSelect', selectInfo);
    const calendarApi = selectInfo.view.calendar;
    calendarApi.unselect();

    calendarApi.addEvent({
      title: 'Available',
      start: selectInfo.start,
      end: selectInfo.end,
      allDay: false,
    });
  }

  // Allow user to edit time of an existing event
  handleEventClick(clickInfo: EventClickArg) {
    const newStart = prompt('New start time (HH:mm)', this.formatTime(clickInfo.event.start));
    const newEnd = prompt('New end time (HH:mm)', this.formatTime(clickInfo.event.end!));

    if (newStart && newEnd) {
      const [startHour, startMinute] = newStart.split(':').map(Number);
      const [endHour, endMinute] = newEnd.split(':').map(Number);

      const start = new Date(clickInfo.event.start!);
      const end = new Date(clickInfo.event.end!);

      start.setHours(startHour, startMinute, 0);
      end.setHours(endHour, endMinute, 0);

      clickInfo.event.setStart(start);
      clickInfo.event.setEnd(end);
    }
  }

  private formatTime(date: Date | null): string {
    if (!date) return '';
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  // Replicate selected day's events across a range
  replicateAvailability() {
    if (this.replicateForm.invalid) return;

    const { sourceDate, startDate, endDate } = this.replicateForm.value;
    const calendarApi = (document.querySelector('full-calendar') as any)?.getApi();
    if (!calendarApi) return;

    const events = calendarApi.getEvents();
    const source = new Date(sourceDate);
    const startRange = new Date(startDate);
    const endRange = new Date(endDate);
    endRange.setHours(23, 59, 59);

    const sourceEvents = events.filter((event: { start: string | number | Date; }) => {
      const eventDate = new Date(event.start!);
      return eventDate.toDateString() === source.toDateString();
    });

    if (sourceEvents.length === 0) {
      alert('No availability found on the selected source date.');
      return;
    }

    for (let day = new Date(startRange); day <= endRange; day.setDate(day.getDate() + 1)) {
      if (day.toDateString() === source.toDateString()) continue;

      sourceEvents.forEach((event: { start: Date; end: Date; }) => {
        const { hours: sh, minutes: sm } = this.extractTime(event.start!);
        const { hours: eh, minutes: em } = this.extractTime(event.end!);

        const start = new Date(day);
        const end = new Date(day);

        start.setHours(sh, sm, 0);
        end.setHours(eh, em, 0);

        calendarApi.addEvent({
          title: 'Available',
          start,
          end,
          allDay: false,
        });
      });
    }

    this.replicateForm.reset();
  }

  private extractTime(date: Date): { hours: number; minutes: number } {
    return {
      hours: date.getHours(),
      minutes: date.getMinutes(),
    };
  }


  submitModalRepeat() {
    if (!this.selectedRepeatDate || this.replicateForm.invalid) return;

    const calendarApi = this.calendarComponent.getApi();
    const events = calendarApi.getEvents();

    const source = new Date(this.selectedRepeatDate);
    const { startDate, endDate } = this.replicateForm.value;
    const startRange = new Date(startDate);
    const endRange = new Date(endDate);
    endRange.setHours(23, 59, 59);

    const sourceEvents = events.filter((event) => {
      const eventDate = new Date(event.start!);
      return eventDate.toDateString() === source.toDateString();
    });

    const repeatDays: number[] = this.replicateForm.value.repeatDays;

    for (let day = new Date(startRange); day <= endRange; day.setDate(day.getDate() + 1)) {
      if (day.toDateString() === source.toDateString()) continue;

      if (!repeatDays.includes(day.getDay())) continue;

      sourceEvents.forEach((event) => {
        const start = new Date(day);
        const end = new Date(day);
        const { hours: sh, minutes: sm } = this.extractTime(event.start!);
        const { hours: eh, minutes: em } = this.extractTime(event.end!);

        start.setHours(sh, sm, 0);
        end.setHours(eh, em, 0);

        calendarApi.addEvent({
          title: 'Available',
          start,
          end,
          allDay: false,
        });
      });
    }

    this.closeRepeatModal();
  }

  closeRepeatModal() {
    this.showRepeatModal = false;
    this.selectedRepeatDate = null;
    this.replicateForm.reset();
  }
}
