import {Component} from '@angular/core';

@Component({
  selector: 'app-not-found',
  template: `<h2>Error 404. Resource Not Found.</h2>`,
  styles: [`h2 {
    color: crimson;
    text-align: center;
    margin-top: 50px;
  }`]
})
export class NotFoundComponent {
}
