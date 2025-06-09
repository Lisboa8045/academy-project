import {Component, input} from '@angular/core';
import {MenuItem} from './menu.model';

@Component({
  selector: 'app-menu',
  imports: [],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent {
  items = input.required<MenuItem[]>();
}
