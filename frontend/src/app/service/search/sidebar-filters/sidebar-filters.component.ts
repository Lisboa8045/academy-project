import type {WritableSignal} from '@angular/core';
import {Component, Input} from '@angular/core';
import {FormsModule} from "@angular/forms";

@Component({
    selector: 'app-sidebar-filters',
    templateUrl: './sidebar-filters.component.html',
    imports: [FormsModule],
    styleUrls: ['./sidebar-filters.component.css']
})
export class SidebarFiltersComponent {
    @Input() filters!: WritableSignal<{
        minPrice: number | null;
        maxPrice: number | null;
        minDuration: number | null;
        maxDuration: number | null;
        negotiable: boolean;
        serviceType: string;
    }>;

    @Input() serviceTypes: string[] = [];

    @Input() onFilterChange!: () => void;

    updateFilter(field: keyof ReturnType<WritableSignal<any>>, value: any) {
        this.filters.update(current => ({...current, [field]: value}));
        if (this.onFilterChange) {
            this.onFilterChange();
        }
    }

    clearFilter(field: keyof ReturnType<WritableSignal<any>>) {
        this.filters.update(current => ({...current, [field]: null}));
        if (this.onFilterChange) {
            this.onFilterChange();
        }
    }
}
