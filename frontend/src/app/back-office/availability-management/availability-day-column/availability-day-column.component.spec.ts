import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AvailabilityDayColumnComponent } from './availability-day-column.component';

describe('AvailabilityDayColumnComponent', () => {
  let component: AvailabilityDayColumnComponent;
  let fixture: ComponentFixture<AvailabilityDayColumnComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AvailabilityDayColumnComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AvailabilityDayColumnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
