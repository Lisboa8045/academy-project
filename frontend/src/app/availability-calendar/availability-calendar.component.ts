import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FullCalendarComponent, FullCalendarModule} from '@fullcalendar/angular';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import {CalendarOptions, DateSelectArg, EventClickArg} from '@fullcalendar/core';
import {ReactiveFormsModule, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgSelectComponent} from '@ng-select/ng-select';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FullCalendarModule, ReactiveFormsModule, NgSelectComponent],
  templateUrl: './availability-calendar.component.html',
  styleUrls: ['./availability-calendar.component.css']
})
export class CalendarComponent implements AfterViewInit {
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
    eventContent: this.renderEventContent.bind(this),
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
    {label: 'Monday', value: 1},
    {label: 'Tuesday', value: 2},
    {label: 'Wednesday', value: 3},
    {label: 'Thursday', value: 4},
    {label: 'Friday', value: 5},
    {label: 'Saturday', value: 6},
    {label: 'Sunday', value: 0},
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
    const weekday = date.toLocaleDateString('en-US', {weekday: 'short'});
    const dateStr = date.toLocaleDateString('en-CA'); // ✅ 'YYYY-MM-DD' in local time

    const calendarApi = this.calendarComponent?.getApi?.();
    let hasEvents = false;

    if (calendarApi) {
      const events = calendarApi.getEvents();
      hasEvents = events.some(event => {
        const eventDate = new Date(event.start!);
        return eventDate.toDateString() === date.toDateString();
      });
    }

    return {
      html: `
  <div class="day-header-text">
        <div style="font-size: 24px; font-weight: bold;">${dayNum}</div>
        <div style="font-size: 12px; color: #555; margin-top: 2px;">${weekday}</div>
        ${
        hasEvents
          ? `<button
                type="button"
                title="Repeat this day’s availability"
                onclick="handleRepeatIconClick(event, '${dateStr}')"
                style="position: absolute; top: 0; right: 0; background: none; border: none; cursor: pointer; font-size: 16px; line-height: 1;"
              >🔁</button>`
          : ''
      }
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

  renderEventContent(arg: any) {
    const event = arg.event;

    // Format the time manually (HH:mm)
    const formatTime = (date: Date) =>
      `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;

    const startTime = formatTime(event.start!);
    const endTime = formatTime(event.end!);

    const container = document.createElement('div');
    container.style.position = 'relative';

    // Time range
    const timeEl = document.createElement('div');
    timeEl.textContent = `${startTime} - ${endTime}`;
    timeEl.style.fontSize = '12px';
    container.appendChild(timeEl);

    // Title (e.g. "Available")
    const titleEl = document.createElement('div');
    titleEl.textContent = event.title;
    titleEl.style.fontWeight = 'bold';
    container.appendChild(titleEl);

    // Delete button
    const deleteBtn = document.createElement('span');
    deleteBtn.innerHTML = '❌';
    deleteBtn.title = 'Delete availability';
    deleteBtn.style.cssText = `
    position: absolute;
    top: 2px;
    right: 4px;
    font-size: 14px;
    cursor: pointer;
    display: none;
    z-index: 1000;
    pointer-events: auto;
  `;

    container.addEventListener('mouseenter', () => {
      deleteBtn.style.display = 'block';
    });
    container.addEventListener('mouseleave', () => {
      deleteBtn.style.display = 'none';
    });

    deleteBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      if (confirm('Delete this availability?')) {
        event.remove();
        this.forceHeaderRerender();
      }
    });

    container.appendChild(deleteBtn);

    return { domNodes: [container] };
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
    this.forceHeaderRerender();
  }

  forceHeaderRerender() {
    // Clone the existing options to trigger Angular change detection
    this.calendarOptions = {
      ...this.calendarOptions,
      dayHeaderContent: this.renderDayHeader.bind(this)
    };
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
    const {startDate, endDate} = this.replicateForm.value;
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
        const {hours: sh, minutes: sm} = this.extractTime(event.start!);
        const {hours: eh, minutes: em} = this.extractTime(event.end!);

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
