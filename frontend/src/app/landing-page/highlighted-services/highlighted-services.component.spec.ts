import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HighlightedServicesComponent } from './highlighted-services.component';

describe('HighlightedServicesComponent', () => {
  let component: HighlightedServicesComponent;
  let fixture: ComponentFixture<HighlightedServicesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HighlightedServicesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HighlightedServicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
