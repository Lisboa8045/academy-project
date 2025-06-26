import { Component, EventEmitter, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.css']
})
export class SearchBarComponent {
  @Output() search = new EventEmitter<string>();

  form = new FormGroup({
    query: new FormControl('')
  });

  onSearch(): void {
    const query = this.form.value.query?.trim() ?? '';

    if (query.length > 100) {
      this.form.controls.query.setValue(query.slice(0, 100));
    } else {
      this.form.controls.query.setValue(query);
    }

    if (query) {
      this.search.emit(query);
    }
  }
}
