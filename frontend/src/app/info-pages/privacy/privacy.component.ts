import { Component } from '@angular/core';
import {environment} from '../../../enviroments/environments';

@Component({
    selector: 'app-about',
    templateUrl: './privacy.component.html',
    styleUrls: ['./privacy.component.css']
})
export class PrivacyComponent {
  protected readonly environment = environment;
}
