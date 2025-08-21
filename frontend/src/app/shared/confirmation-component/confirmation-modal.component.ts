import { Component, EventEmitter, Input, Output } from '@angular/core';
import {Router} from '@angular/router';
@Component({
  selector: 'app-confirmation-modal',
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['./confirmation-modal.component.css']
})
export class ConfirmationModalComponent {

  constructor(private router: Router) {}

  @Input({required: true}) title?: string;
  @Input({required: true}) text?: string;
  @Output() confirm = new EventEmitter<void>();
  @Output() close = new EventEmitter<void>();

  onConfirm() {
    this.confirm.emit();
  }

  onClose() {
    this.close.emit();
  }
}
