import {Component, Input, Signal, WritableSignal} from '@angular/core';
import {NgForOf} from '@angular/common';

@Component({
    selector: 'app-pagination-bar',
    standalone: true,
    templateUrl: './pagination-bar.component.html',
    styleUrls: ['./pagination-bar.component.css'],
    imports: [NgForOf],
})
export class PaginationBarComponent {
    @Input() currentPage!: WritableSignal<number>;
    @Input() totalPages!: Signal<number>;
    @Input() goToPageFn!: (page: number | string) => void;
    @Input() goToPreviousPageFn!: () => void;
    @Input() goToNextPageFn!: () => void;

    getPaginationPages(): (number | string)[] {
        const total = this.totalPages();
        const current = this.currentPage();

        const pages: (number | string)[] = [];

        if (total <= 7) {
            for (let i = 0; i < total; i++) {
                pages.push(i);
            }
        } else {
            pages.push(0);
            const windowSize = 3;

            let start = Math.max(1, current - 1);
            let end = Math.min(total - 2, current + 1);

            if (current <= 2) {
                start = 1;
                end = 1 + windowSize - 1;
            } else if (current >= total - 3) {
                start = total - windowSize - 1;
                end = total - 2;
            }

            if (start > 1) {
                pages.push('...');
            }

            for (let i = start; i <= end; i++) {
                pages.push(i);
            }

            if (end < total - 2) {
                pages.push('...');
            }

            pages.push(total - 1);
        }

        return pages;
    }

    isPageNumber(page: number | string): page is number {
        return typeof page === 'number';
    }

    displayPageNumber(page: number | string): number {
        if (this.isPageNumber(page)) {
            return page + 1;
        }
        return 0;
    }
}
