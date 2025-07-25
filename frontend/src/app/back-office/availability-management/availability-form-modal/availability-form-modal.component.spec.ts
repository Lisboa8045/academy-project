import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AvailabilityFormModalComponent } from './availability-form-modal.component';

describe('AvailabilityFormModalComponent', () => {
  let component: AvailabilityFormModalComponent;
  let fixture: ComponentFixture<AvailabilityFormModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AvailabilityFormModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AvailabilityFormModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
