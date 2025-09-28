import { Component } from '@angular/core';
import {environment} from '../../../enviroments/environments';

@Component({
    selector: 'app-about',
    templateUrl: './terms.component.html',
    styleUrls: ['./terms.component.css']
})
export class TermsComponent {
  protected readonly environment = environment;
}
