import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-service-types',
  imports: [],
  templateUrl: './service-types.component.html',
  styleUrl: './service-types.component.css'
})
export class ServiceTypesComponent implements OnInit {
  serviceTypes: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any[]>('/api/service-types').subscribe(data => {
      this.serviceTypes = data;
    });
  }
}
