import {Component, inject, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {forkJoin} from "rxjs";

interface GitHubUser {
    login: string;
    avatar_url: string;
    html_url: string;
}

@Component({
    selector: 'app-about',
    templateUrl: './about.component.html',
    styleUrls: ['./about.component.css']
})
export class AboutComponent implements OnInit {
    users: GitHubUser[] = [];
    private readonly http = inject(HttpClient);

    private readonly usernames = ['Adriano-Queiroz','BCorreia02','Calmskyy', 'FlavioMiguel27', 'Lisboa8045','Shrimpo22'];

    ngOnInit(): void {
       // this.fetchUsers();
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
