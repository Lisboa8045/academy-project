import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServiceTypeModel } from '../../models/service-type.model';

@Component({
  selector: 'app-service-search',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './service-search.component.html',
  styleUrls: ['../schedule.component.css']
})
export class ServiceSearchComponent {
  @Input() serviceTypes: ServiceTypeModel[] = [];  // Must match parent
  @Input() selectedServiceTypeId: number | null = null;  // Must match parent
  @Output() searchChange = new EventEmitter<string>();
  @Output() typeChange = new EventEmitter<number | null>();

  onSearchInput(event: Event) {
    const input = event.target as HTMLInputElement;
    this.searchChange.emit(input.value);
  }

  filterByType(typeId: number | null) {
    this.typeChange.emit(typeId);
  }
}
