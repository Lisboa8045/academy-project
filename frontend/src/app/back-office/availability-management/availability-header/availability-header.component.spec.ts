import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AvailabilityHeaderComponent } from './availability-header.component';

describe('AvailabilityHeaderComponent', () => {
  let component: AvailabilityHeaderComponent;
  let fixture: ComponentFixture<AvailabilityHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AvailabilityHeaderComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AvailabilityHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
