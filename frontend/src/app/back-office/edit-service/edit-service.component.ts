import {Component, inject, OnInit, signal} from '@angular/core';
import {LoadingComponent} from '../../loading/loading.component';
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-edit-service',
  imports: [
    LoadingComponent,
    NgIf,
    NgForOf
  ],
  templateUrl: './edit-service.component.html',
  styleUrl: './edit-service.component.css'
})
export class EditServiceComponent {
  loading = signal(false);
}
