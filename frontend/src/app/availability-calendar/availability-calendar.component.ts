import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FullCalendarComponent, FullCalendarModule} from '@fullcalendar/angular';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import {CalendarOptions, DateSelectArg, EventApi, EventClickArg} from '@fullcalendar/core';
import {ReactiveFormsModule, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgSelectComponent} from '@ng-select/ng-select';
import {AvailabilityDTO, DateTimeRange} from './availability.models';
import {AvailabilityService} from './availability.service';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FullCalendarModule, ReactiveFormsModule, NgSelectComponent],
  templateUrl: './availability-calendar.component.html',
  styleUrls: ['./availability-calendar.component.css']
})
export class CalendarComponent implements OnInit, AfterViewInit {
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
  originalNumberOfAvailabilitys = 0;
  selectedRepeatDate: string | null = null;
  showRepeatModal = false;
  conflictModalVisible = false;
  conflictMessages: string[] = [];

  weekDays = [
    {label: 'Monday', value: 1},
    {label: 'Tuesday', value: 2},
    {label: 'Wednesday', value: 3},
    {label: 'Thursday', value: 4},
    {label: 'Friday', value: 5},
    {label: 'Saturday', value: 6},
    {label: 'Sunday', value: 0},
  ];

  statusColors: Record<string, string> = {
    PENDING: '#f0ad4e',    // Softer amber
    CONFIRMED: '#2ecc71',  // Lighter green
    CANCELLED: '#e74c3c',  // Bright red
    FINISHED: '#95a5a6',   // Muted gray-blue
  };
  availableColor = '#1dda10';


  constructor(private fb: FormBuilder, private availabilityService: AvailabilityService) {
    this.replicateForm = this.fb.group({
      sourceDate: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      repeatDays: [[], Validators.required]
    });
  }

  ngOnInit(): void {
    this.availabilityService.getAvailabilities().subscribe({
      next: (availabilities) => {
        const calendarApi = this.calendarComponent.getApi();
        availabilities.daySchedules.forEach(daySchedule => {
          daySchedule.timeRanges.forEach(timeRange => {
            const date = daySchedule.date;
            const start = `${date}T${timeRange.start}`;
            const end = `${date}T${timeRange.end}`;

            calendarApi.addEvent({
              title: 'Available',
              start,
              end,
              allDay: false,
              color: this.availableColor,
            });
            this.originalNumberOfAvailabilitys ++;
          });
        });
        this.forceHeaderRerender();
      }
    });
    this.availabilityService.getAppointments().subscribe({
      next: (appointments) => {
        const calendarApi = this.calendarComponent.getApi();
        const calendarEvents = appointments.map(app => ({
          title: `${app.serviceName} ${app.status}`,
          start: app.startDateTime,
          end: app.endDateTime,
          color: this.statusColors[app.status],
        }));
        calendarEvents.forEach(event => calendarApi.addEvent(event));
        this.forceHeaderRerender();
      }
    });
  }

  ngAfterViewInit(): void {
    (window as any).handleRepeatIconClick = (e: MouseEvent, date: string) => {
      e.stopPropagation();
      e.preventDefault();
      this.handleRepeatClick(date);
    };

    document.addEventListener('mousedown', (e: any) => {
      if (e.target?.classList.contains('repeat-icon')) {
        e.stopPropagation();
        e.preventDefault();
      }
    }, true);

    const calendarApi = this.calendarComponent.getApi();
    this.originalNumberOfAvailabilitys = calendarApi.getEvents().filter(event => event.title === 'Available').length;
  }

  // ────────────── FullCalendar Core Handlers ──────────────

  handleDateSelect(selectInfo: DateSelectArg) {
    const calendarApi = selectInfo.view.calendar;
    calendarApi.unselect();

    calendarApi.addEvent({
      title: 'Available',
      start: selectInfo.start,
      end: selectInfo.end,
      allDay: false,
      color: this.availableColor,
    });

    this.forceHeaderRerender();
  }

  handleEventClick(clickInfo: EventClickArg) {
    const event = clickInfo.event;

    if (event.title !== 'Available') {
      return;
    }
  }

  // ────────────── Modal Logic ──────────────

  handleRepeatClick(date: string) {
    this.selectedRepeatDate = date;
    this.replicateForm.patchValue({sourceDate: date});
    this.showRepeatModal = true;
  }

  closeRepeatModal() {
    this.showRepeatModal = false;
    this.selectedRepeatDate = null;
    this.replicateForm.reset();
  }

  showConflictsModal(messages: string[]) {
    this.conflictMessages = messages;
    this.conflictModalVisible = true;
  }

  closeConflictsModal() {
    this.conflictModalVisible = false;
    this.conflictMessages = [];
  }

  submitModalRepeat(): void {
    if (!this.selectedRepeatDate || this.replicateForm.invalid) return;

    const calendarApi = this.calendarComponent.getApi();
    const sourceEvents = this.getEventsByDate(this.selectedRepeatDate);
    const {startDate, endDate, repeatDays} = this.replicateForm.value;

    const range = this.getDateRange(startDate, endDate);
    const conflicts: Record<string, string[]> = {};

    range.forEach(day => {
      const dateStr = day.toLocaleDateString('en-CA');

      if (day.toDateString() === new Date(this.selectedRepeatDate!).toDateString()) return;
      if (!repeatDays.includes(day.getDay())) return;

      const existingEvents = this.getEventsByDate(dateStr);

      if (existingEvents.length > 0) {
        conflicts[dateStr] = existingEvents.map(ev => {
          const start = this.formatTime(ev.start!);
          const end = this.formatTime(ev.end!);
          return `${ev.title} (${start} - ${end})`;
        });
        return;
      }


      this.cloneEventsForDay(sourceEvents, day, calendarApi);
    });

    this.closeRepeatModal();

    if (Object.keys(conflicts).length > 0) {
      const messages = Object.entries(conflicts).map(([date, reasons]) =>
        `🗓 ${date}: ${reasons.map(r => `• ${r}`).join('\n')}`
      );
      this.showConflictsModal(messages);
      return;
    }
  }

  // ────────────── Save / DTO Logic ──────────────

  saveAvailability(): void {
    const calendarApi = this.calendarComponent.getApi();
    const allEvents = calendarApi.getEvents();

    const availableEvents = allEvents.filter(event => event.title === 'Available');
    const dto = this.mapEventsToDTO(availableEvents);

    console.log('DTO ready to send:', dto);

    this.availabilityService.saveAvailabilities(dto).subscribe({
      next: () => {
        console.log('Availability saved successfully.');
        this.originalNumberOfAvailabilitys = availableEvents.length;
      },
      error: (err) => {
        console.error('Error saving availability:', err);
      }
    });
  }

  private mapEventsToDTO(events: EventApi[]): AvailabilityDTO {
    const grouped: Record<string, DateTimeRange[]> = {};

    events.forEach(event => {
      const dateStr = event.start!.toLocaleDateString('en-CA');
      if (!grouped[dateStr]) grouped[dateStr] = [];

      grouped[dateStr].push({
        start: this.formatTime(event.start!),
        end: this.formatTime(event.end!)
      });
    });

    return {
      daySchedules: Object.entries(grouped).map(([date, timeRanges]) => ({
        date,
        timeRanges
      }))
    };
  }

  canSave(): boolean {
    const current = this.calendarComponent?.getApi?.()
      .getEvents().filter(event => event.title === 'Available').length;
    return current !== this.originalNumberOfAvailabilitys;
  }

  // ────────────── Calendar Rendering ──────────────

  renderDayHeader(arg: any) {
    const date = arg.date;
    const dayNum = date.getDate();
    const weekday = date.toLocaleDateString('en-US', {weekday: 'short'});
    const dateStr = date.toLocaleDateString('en-US');

    const calendarApi = this.calendarComponent?.getApi?.();
    const dayHasEvents =
      calendarApi?.getEvents().some(event =>
        event.title === 'Available' &&
        new Date(event.start!).toDateString() === date.toDateString()
      ) ?? false;

    return {
      html: `
        <div class="day-header-text">
          <div style="font-size: 24px; font-weight: bold;">${dayNum}</div>
          <div style="font-size: 12px; color: #555; margin-top: 2px;">${weekday}</div>
          ${dayHasEvents
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

  renderEventContent(arg: any) {
    const event = arg.event;
    const formatTime = (date: Date) =>
      `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;

    const container = document.createElement('div');
    container.style.position = 'relative';

    const timeEl = document.createElement('div');
    timeEl.textContent = `${formatTime(event.start!)} - ${formatTime(event.end!)}`;
    timeEl.style.fontSize = '12px';

    const titleEl = document.createElement('div');
    titleEl.textContent = event.title;
    titleEl.style.fontWeight = 'bold';

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
      color: transparent;
      text-shadow: 0 0 0 white;
    `;

    container.addEventListener('mouseenter', () => deleteBtn.style.display = 'block');
    container.addEventListener('mouseleave', () => deleteBtn.style.display = 'none');

    deleteBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      if (confirm('Delete this availability?')) {
        event.remove();
        this.forceHeaderRerender();
      }
    });

    container.appendChild(timeEl);
    container.appendChild(titleEl);
    container.appendChild(deleteBtn);

    return {domNodes: [container]};
  }

  // ────────────── Utility Methods ──────────────

  private forceHeaderRerender() {
    this.calendarOptions = {
      ...this.calendarOptions,
      dayHeaderContent: this.renderDayHeader.bind(this)
    };
  }

  private getDateRange(start: string, end: string): Date[] {
    const range: Date[] = [];
    const current = new Date(start);
    const endDate = new Date(end);
    endDate.setHours(23, 59, 59);

    while (current <= endDate) {
      range.push(new Date(current));
      current.setDate(current.getDate() + 1);
    }

    return range;
  }

  private getEventsByDate(dateStr: string) {
    const calendarApi = this.calendarComponent.getApi();
    const targetDate = new Date(dateStr);

    return calendarApi.getEvents().filter(event =>
      new Date(event.start!).toDateString() === targetDate.toDateString()
    );
  }

  private cloneEventsForDay(events: EventApi[], day: Date, calendarApi: any): void {
    events.forEach(event => {
      const {hours: sh, minutes: sm} = this.extractTime(event.start!);
      const {hours: eh, minutes: em} = this.extractTime(event.end!);

      const start = new Date(day);
      const end = new Date(day);
      start.setHours(sh, sm, 0);
      end.setHours(eh, em, 0);

      calendarApi.addEvent({
        title: 'Available',
        start,
        end,
        allDay: false,
        color: this.availableColor,
      });
    });
  }

  private formatTime(date: Date | null): string {
    if (!date) return '';
    const h = date.getHours().toString().padStart(2, '0');
    const m = date.getMinutes().toString().padStart(2, '0');
    return `${h}:${m}`;
  }

  private extractTime(date: Date): { hours: number; minutes: number } {
    return {
      hours: date.getHours(),
      minutes: date.getMinutes()
    };
  }
}
