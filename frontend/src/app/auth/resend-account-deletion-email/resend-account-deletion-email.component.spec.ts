import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResendAccountDeletionEmailComponent } from './resend-account-deletion-email.component';

describe('ResendAccountDeletionEmailComponent', () => {
  let component: ResendAccountDeletionEmailComponent;
  let fixture: ComponentFixture<ResendAccountDeletionEmailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResendAccountDeletionEmailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResendAccountDeletionEmailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
