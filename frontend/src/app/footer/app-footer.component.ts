import { Component, OnInit, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import {NgForOf} from '@angular/common';

interface GitHubUser {
  login: string;
  avatar_url: string;
  html_url: string;
}

@Component({
  selector: 'app-footer',
  standalone: true,
  templateUrl: './app-footer.component.html',
  styleUrls: ['./app-footer.component.css'],
  imports: [
    NgForOf
  ]
})
export class AppFooterComponent implements OnInit {
  users: GitHubUser[] = [];
  private http = inject(HttpClient);

  private readonly usernames = ['Adriano-Queiroz','BCorreia02','Calmskyy', 'FlavioMiguel27', 'Lisboa8045','Shrimpo22'];

  ngOnInit(): void {
    this.fetchUsers();
  }

  fetchUsers(): void {

    const requests = this.usernames.map(username =>
      this.http.get<GitHubUser>(`https://api.github.com/users/${username}`)
    );

    forkJoin(requests).subscribe({
      next: users => this.users = users,
      error: err => console.error('GitHub fetch failed', err)
    });
  }
}
