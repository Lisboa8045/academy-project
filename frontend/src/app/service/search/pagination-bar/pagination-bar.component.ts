// pagination-bar.component.ts
import type {Signal, WritableSignal} from '@angular/core';
import {Component, Input} from '@angular/core';
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-pagination-bar',
    standalone: true,
    templateUrl: './pagination-bar.component.html',
    imports: [CommonModule],
    styleUrls: ['./pagination-bar.component.css'],
})
export class PaginationBarComponent {
    @Input() currentPage!: Signal<number>;
    @Input() totalPages!: Signal<number>;
    @Input() getPaginationPages!: () => (number | string)[];

    @Input() pageChange!: WritableSignal<number>;

    goToPreviousPage() {
        if (this.currentPage() > 0) {
            this.pageChange.set(this.currentPage() - 1);
        }
    }

    goToNextPage() {
        if (this.currentPage() + 1 < this.totalPages()) {
            this.pageChange.set(this.currentPage() + 1);
        }
    }

    goToPage(page: number | string) {
        if (typeof page === 'number') {
            this.pageChange.set(page);
        }
    }

    displayPageNumber(page: number | string): number | string {
        return typeof page === 'number' ? page + 1 : page;
    }
}
