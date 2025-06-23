import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from "../auth.service";
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-confirm-email',
    templateUrl: './confirm-email.component.html',
    imports: [CommonModule],
    styleUrls: ['./confirm-email.component.css']
})
export class ConfirmEmailComponent implements OnInit {
    email: string = '';
    message: string = '';
    error: string = '';
    resendDisabled: boolean = false;
    showBackToLogin: boolean = false;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private authService: AuthService
    ) {
    }

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => {
            this.email = params['email'] || '';
        });
    }

    resend(): void {
        if (!this.email || this.resendDisabled) return;
        this.resendDisabled = true;
        this.showBackToLogin = true;
        /*
                this.authService.resendConfirmation(this.email).subscribe({
                    next: () => {
                        this.message = 'Verification email sent!';
                        this.error = '';
                        this.resendDisabled = true;
                        this.showBackToLogin = true;
                    },
                    error: () => {
                        this.message = '';
                        this.error = 'Failed to resend verification. Please try again later.';
                    }
                });
                */
    }

    goToLogin(): void {
        this.router.navigate(['/auth']);
    }
}
