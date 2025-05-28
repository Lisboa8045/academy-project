import {Component, OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {AppHeaderComponent} from './header/app-header.component';
import {AppFooterComponent} from './footer/app-footer.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, AppHeaderComponent, AppFooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent{
}
