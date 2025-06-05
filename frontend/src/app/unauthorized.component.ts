import { Component } from '@angular/core';

@Component({
  selector: 'app-dont-have-permission',
  template: `<h2>You do not have permission to access this page.</h2>`,
  styles: [ `h2 { color: crimson; text-align: center; margin-top: 50px; }` ]
})
export class UnauthorizedComponent {}
