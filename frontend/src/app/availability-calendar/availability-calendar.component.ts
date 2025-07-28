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
import {snackBarError} from '../shared/snackbar/snackbar-error';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarSuccess} from '../shared/snackbar/snackbar-success';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FullCalendarModule, ReactiveFormsModule, NgSelectComponent],
  templateUrl: './availability-calendar.component.html',
  styleUrls: ['./availability-calendar.component.css']
})
export class CalendarComponent implements OnInit, AfterViewInit {
  @ViewChild('calendar') calendarComponent!: FullCalendarComponent;
  todayString = new Date().toISOString().split('T')[0];
  private originalAvailabilitySnapshot: { date: string, start: string, end: string }[] = [];

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
    eventOverlap: false,
    eventAllow: this.allowEventEdit.bind(this),
    views: {
      dayGridMonth: {
        editable: false,
        eventStartEditable: false,
        eventDurationEditable: false,
        selectable: true, // allow selecting a day to navigate
        selectMirror: false,
      }
    },
    datesSet: () => {
      // Force re-render of day headers and stack events after navigation
      this.forceHeaderRerender();
      this.scheduleStackEvents();
    },
    eventDidMount: (info) => {
      this.collectEventForStacking(info);
      // Only in month view (dayGridMonth)
      if (info.view.type === 'dayGridMonth' && info.el) {
        // info.el is the <a> element
        info.el.style.pointerEvents = 'none';
      }
    },
  };

  // Modal/replication state
  replicateForm: FormGroup;
  selectedRepeatDate: string | null = null;
  showRepeatModal = false;
  conflictModalVisible = false;
  conflictMessages: string[] = [];

  // Event tracking for "dirty" state
  originalNumberOfAvailabilitys = 0;

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
  availableColor = '#3B82F6';

  // --- Stacking event logic ---
  private eventStackingTimer: any = null;
  private eventsToStack: any[] = [];

  constructor(
    private fb: FormBuilder,
    private availabilityService: AvailabilityService,
    private snackBar: MatSnackBar,
  ) {
    this.replicateForm = this.fb.group({
      sourceDate: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      repeatDays: [[], Validators.required]
    });
  }

  ngOnInit(): void {
    // Load appointments as "busy"
    this.availabilityService.getAppointments().subscribe({
      next: (appointments) => {
        const calendarApi = this.calendarComponent.getApi();
        const calendarEvents = appointments.map(app => ({
          title: `${app.serviceName} ${app.status}`,
          start: app.startDateTime,
          end: app.endDateTime,
          color: this.statusColors[app.status],
          editable: false,
          durationEditable: false
        }));
        calendarEvents.forEach(event => calendarApi.addEvent(event));
        this.forceHeaderRerender();
        this.scheduleStackEvents();
      }
    });
    // Load availabilities
    this.availabilityService.getAvailabilities().subscribe({
      next: (availabilities) => {
        const calendarApi = this.calendarComponent.getApi();
        this.originalAvailabilitySnapshot = [];
        availabilities.daySchedules.forEach(daySchedule => {
          daySchedule.timeRanges.forEach(timeRange => {
            const date = daySchedule.date;
            const start = `${date}T${timeRange.start}`;
            const end = `${date}T${timeRange.end}`;
            const overlapping = calendarApi.getEvents().some(event => (
              (start < event.end!.toISOString() && end > event.start!.toISOString())
            ));
            calendarApi.addEvent({
              title: 'Available',
              start,
              end,
              allDay: false,
              color: this.availableColor,
              editable: !overlapping,
            });
            this.originalAvailabilitySnapshot.push({
              date: daySchedule.date,
              start: this.stripSeconds(timeRange.start),
              end: this.stripSeconds(timeRange.end)
            });
            this.originalNumberOfAvailabilitys++;
          });
        });
        this.forceHeaderRerender();
        this.scheduleStackEvents();
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

  private getCurrentAvailabilitySnapshot(): { date: string, start: string, end: string }[] {
    if (!this.calendarComponent) return []; // Return empty if not ready
    const calendarApi = this.calendarComponent.getApi();
    return calendarApi.getEvents()
      .filter(event => event.title === 'Available')
      .map(event => {
        const date = event.start!.toLocaleDateString('en-CA');
        return {
          date,
          start: this.stripSeconds(this.formatTime(event.start!)),
          end: this.stripSeconds(this.formatTime(event.end!))
        };
      })
      // Sort for stable comparison
      .sort((a, b) =>
        a.date.localeCompare(b.date) ||
        a.start.localeCompare(b.start) ||
        a.end.localeCompare(b.end)
      );
  }

  // ────────────── FullCalendar Core Handlers ──────────────

  handleDateSelect(selectInfo: DateSelectArg) {
    const now = new Date();
    if (selectInfo.end < now || selectInfo.start < now) {
      snackBarError(this.snackBar, 'Cannot add availability in the past.');
      selectInfo.view.calendar.unselect();
      return;
    }
    const calendarApi = selectInfo.view.calendar;

    if (selectInfo.view.type === 'dayGridMonth') {
      calendarApi.changeView('timeGridWeek', selectInfo.start);
      calendarApi.unselect();
      return;
    }
    calendarApi.unselect();
    calendarApi.addEvent({
      title: 'Available',
      start: selectInfo.start,
      end: selectInfo.end,
      allDay: false,
      color: this.availableColor,
    });
    this.forceHeaderRerender();
    this.scheduleStackEvents();
  }

  allowEventEdit(dropInfo: any) {
    // Only allow drag/resize if event ends in the future
    const now = new Date();
    // dropInfo.end can be null if allDay
    const eventEnd = dropInfo.end ? new Date(dropInfo.end) : new Date(dropInfo.start);
    if (eventEnd <= now) {
      snackBarError(this.snackBar, 'Cannot edit or move past events.');
      return false;
    }
    return true;
  }

  handleEventClick(clickInfo: EventClickArg) {
    const event = clickInfo.event;
    if (event.title !== 'Available') {
      return;
    }
    // Add any additional click logic here if needed
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

      let hasConflict = false;
      const newTimeRanges = sourceEvents.map(ev => ({
        start: { h: ev.start!.getHours(), m: ev.start!.getMinutes() },
        end: { h: ev.end!.getHours(), m: ev.end!.getMinutes() }
      }));

      const overlapEvents: string[] = [];

      existingEvents.forEach(ev => {
        const existingStart = ev.start;
        const existingEnd = ev.end;
        newTimeRanges.forEach(range => {
          // Create date objects for the *new* time range on this date
          const start = new Date(day);
          const end = new Date(day);
          start.setHours(range.start.h, range.start.m, 0, 0);
          end.setHours(range.end.h, range.end.m, 0, 0);

          // Check overlap: start < existingEnd && end > existingStart
          if (
            start < existingEnd! && end > existingStart!
          ) {
            hasConflict = true;
            overlapEvents.push(`${ev.title} (${this.formatTime(existingStart)} - ${this.formatTime(existingEnd)})`);
          }
        });
      });

      if (hasConflict) {
        conflicts[dateStr] = overlapEvents;
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

    this.forceHeaderRerender();
    this.scheduleStackEvents();
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
        snackBarSuccess(this.snackBar, 'Availability saved successfully.');
        this.originalNumberOfAvailabilitys = availableEvents.length;
        this.originalAvailabilitySnapshot = this.getCurrentAvailabilitySnapshot();
      },
      error: (err) => {
        snackBarError(this.snackBar, 'Error saving availability. Please try again.');
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

  private arraysEqual(a: any[], b: any[]): boolean {
    if (a.length !== b.length) {
      return false;
    }
    for (let i = 0; i < a.length; i++) {
      if (a[i].date !== b[i].date) {
        return false;
      }
      if (a[i].start !== b[i].start) {
        return false;
      }
      if (a[i].end !== b[i].end) {
        return false;
      }
    }
    return true;
  }

  canSave(): boolean {
    const current = this.getCurrentAvailabilitySnapshot();
    return !this.arraysEqual(current, this.originalAvailabilitySnapshot);
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
                  class="repeat-icon"
                  style="position: absolute; top: 0; right: 0; background: none; border: none; cursor: pointer; font-size: 16px; line-height: 1;"
                >🔁</button>`
        : ''
      }
        </div>
      `
    };
  }

  private stripSeconds(time: string): string {
    return time.split(':').slice(0, 2).join(':');
  }

  renderEventContent(arg: any) {
    const event = arg.event;
    const viewType = arg.view.type;
    const formatTime = (date: Date) =>
      `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;

    // Custom rendering for MONTH VIEW
    if (viewType === 'dayGridMonth') {
      const background = event.backgroundColor || event.color || this.availableColor;
      const container = document.createElement('div');
      container.style.display = 'flex';
      container.style.flexDirection = 'column';
      container.style.alignItems = 'flex-start';
      container.style.background = background;
      container.style.borderRadius = '10px';
      container.style.padding = '6px 12px';
      container.style.margin = '3px 0';
      container.style.fontSize = '13px';
      container.style.position = 'relative';
      container.style.minHeight = '32px';
      container.style.width = '100%';

      // Time
      if (!event.allDay) {
        const timeEl = document.createElement('div');
        timeEl.textContent = `${formatTime(event.start!)} - ${formatTime(event.end!)}`;
        timeEl.style.fontWeight = 'bold';
        timeEl.style.marginBottom = '2px';
        timeEl.style.width = '100%';
        timeEl.style.color = 'transparent';
        timeEl.style.textShadow = '0 0 0 white';
        container.appendChild(timeEl);
      }
      // Title
      const titleEl = document.createElement('div');
      titleEl.textContent = event.title;
      titleEl.style.overflow = 'hidden';
      titleEl.style.textOverflow = 'ellipsis';
      titleEl.style.whiteSpace = 'nowrap';
      titleEl.style.width = '100%';
      titleEl.style.pointerEvents = 'none';
      titleEl.style.color = 'transparent';
      titleEl.style.textShadow = '0 0 0 white';
      container.appendChild(titleEl);

      return {domNodes: [container]};
    }

    // Custom rendering for timeGrid
    const container = document.createElement('div');
    container.style.position = 'relative';

    const timeEl = document.createElement('div');
    timeEl.textContent = `${formatTime(event.start!)} - ${formatTime(event.end!)}`;
    timeEl.style.fontSize = '12px';

    const titleEl = document.createElement('div');
    titleEl.textContent = event.title;
    titleEl.style.fontWeight = 'bold';

    if (event.title === 'Available') {
      const now = new Date();
      // Only allow delete if event is in the future
      const isFuture = event.end > now;
      if (isFuture) {
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
            this.scheduleStackEvents();
          }
        });
        container.appendChild(deleteBtn);
      }
    }
    container.appendChild(timeEl);
    container.appendChild(titleEl);

    return {domNodes: [container]};
  }

  // ────────────── Event stacking logic (robust across view changes) ──────────────

  private collectEventForStacking(info: any) {
    if (info.view.type.startsWith('timeGrid')) {
      this.eventsToStack.push(info);
    }
  }

  private scheduleStackEvents() {
    if (this.eventStackingTimer) clearTimeout(this.eventStackingTimer);
    this.eventStackingTimer = setTimeout(() => {
      this.stackEvents();
    }, 20); // 20ms is usually enough, tweak if needed
  }

  private stackEvents() {
    // Group events by cell
    const cellToHarnesses = new Map<Element, Element[]>();
    this.eventsToStack.forEach(info => {
      const harness = info.el.parentElement;
      if (!harness) return;
      const cell = harness.parentElement;
      if (!cell) return;
      if (!cellToHarnesses.has(cell)) {
        cellToHarnesses.set(cell, []);
      }
      cellToHarnesses.get(cell)!.push(harness);
    });

    // For each cell, stack its events
    for (const harnesses of cellToHarnesses.values()) {
      harnesses.forEach((el: any, idx: number) => {
        el.style.left = `${(idx) * 12}px`;
        el.style.marginTop = `${(idx) * 14}px`;
        el.style.width = `calc(100% - ${(idx) * 12}px)`;
        el.style.zIndex = `${100 + idx}`;
        el.style.boxShadow = '0 2px 8px 0 rgba(30,41,59,0.10), 0 1.5px 3px 0 rgba(30,41,59,0.10)';
        el.style.borderRadius = '10px';
        el.style.transition = 'box-shadow 0.15s, left 0.15s, width 0.15s, margin-top 0.15s';
      });
    }
    this.eventsToStack = [];
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
