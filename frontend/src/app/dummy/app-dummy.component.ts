import { Component, OnInit} from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-dummy',
  standalone: true,
  templateUrl: './app-dummy.component.html',
  styleUrls: ['./app-dummy.component.css']
})
export class AppDummyComponent implements OnInit {
  backendData: Record<string, any>[] = [];
  objectKeys = Object.keys;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchData();
  }

  fetchData(): void {
    this.http.get<Record<string, any>[]>('http://localhost:8080/appointments').subscribe({
      next: (data) => {
        this.backendData = data;
        console.log('Data received:', data);
      },
      error: (err) => {
        console.error('Error fetching data', err);
      }
    });
  }
}
