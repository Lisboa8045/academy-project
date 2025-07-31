import {Component, OnInit} from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'app-confirm-email-prompt',
    templateUrl: './confirm-email-prompt.component.html',
    styleUrls: ['./confirm-email-prompt.component.css']
})
export class ConfirmEmailPromptComponent implements OnInit {
    email: string = '';

    constructor(private readonly router: Router) {}

    ngOnInit(): void {
        const email = sessionStorage.getItem('signupConfirmEmail');
        if (!email) {
            this.router.navigate(['/auth']);
            return;
        }

        this.email = email;
        sessionStorage.removeItem('signupConfirmEmail');
    }

    goToLogin(): void {
        this.router.navigate(['/auth']);
    }
}