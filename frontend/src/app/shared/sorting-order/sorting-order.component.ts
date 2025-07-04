import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-sorting-order',
  templateUrl: './sorting-order.component.html',
  styleUrls: ['./sorting-order.css'],
  standalone: true,
  imports: [
    FormsModule
  ]
})
export class SortingOrderComponent {
  @Input() order: 'asc' | 'desc' = 'asc';
  @Input({ required: true }) sortBy?: string;
  @Output() orderChange = new EventEmitter<'asc' | 'desc'>();

  onSortChange(event: Event) {
    const newValue = (event.target as HTMLInputElement).value as 'asc' | 'desc';
    this.orderChange.emit(newValue);
  }
}
