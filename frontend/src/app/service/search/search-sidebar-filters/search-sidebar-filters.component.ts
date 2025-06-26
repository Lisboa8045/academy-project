import {Component, Input, WritableSignal} from '@angular/core';
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";

@Component({
    selector: 'app-sidebar-filters',
    templateUrl: './search-sidebar-filters.component.html',
    imports: [CommonModule, FormsModule],
    styleUrls: ['./search-sidebar-filters.component.css']
})
export class SearchSidebarFiltersComponent {
    @Input() filters!: WritableSignal<{
        minPrice: number | null;
        maxPrice: number | null;
        minDuration: number | null;
        maxDuration: number | null;
        negotiable: boolean;
        serviceType: string;
    }>;
    @Input() appliedFilters!: WritableSignal<{
        minPrice: number | null;
        maxPrice: number | null;
        minDuration: number | null;
        maxDuration: number | null;
        negotiable: boolean;
        serviceType: string
    }>;

    @Input() serviceTypes: string[] = [];

    @Input() onFilterChange!: () => void;

    clearFilter(field: keyof ReturnType<WritableSignal<any>>) {
        this.filters.update(current => ({...current, [field]: null}));
        if (this.onFilterChange) {
            this.onFilterChange();
        }
    }
}