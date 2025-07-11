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

  onClickedItem(item: MenuItem) {
    if (item.command) {
      item.command();
    }
    else {
      console.log("No command for " + item.label);
    }
  }
}
