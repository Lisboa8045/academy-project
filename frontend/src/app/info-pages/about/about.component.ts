import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css'],
  imports: [CommonModule]
})
export class AboutComponent {

  readonly usernames = ['Adriano-Queiroz', 'BCorreia02', 'Calmskyy', 'FlavioMiguel27', 'Lisboa8045', 'Shrimpo22'];
}
