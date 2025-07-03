import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WeekNavigationComponent } from './week-navigation.component';

describe('WeekNavigationComponent', () => {
  let component: WeekNavigationComponent;
  let fixture: ComponentFixture<WeekNavigationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WeekNavigationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WeekNavigationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
