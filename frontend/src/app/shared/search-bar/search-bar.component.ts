import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.css']
})
export class SearchBarComponent implements OnInit {
  @Output() search = new EventEmitter<string>();

  form = new FormGroup({
    query: new FormControl('')
  });

  constructor(private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    if (this.isHeader) {
      this.route.queryParams.subscribe(params => {
        const query = params['q'];
        if (query) {
          this.form.controls.query.setValue(query);
        }
      });
    }
  }

  @Input() isHeader!: boolean;

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
