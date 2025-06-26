import {Component, signal} from '@angular/core';
import {LoadingComponent} from '../../loading/loading.component';

@Component({
  selector: 'app-edit-service',
  imports: [
    LoadingComponent
  ],
  templateUrl: './edit-service.component.html',
  styleUrl: './edit-service.component.css'
})
export class EditServiceComponent {
  loading = signal(false);
}
