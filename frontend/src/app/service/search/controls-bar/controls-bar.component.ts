import {Component, Input, WritableSignal} from '@angular/core';

@Component({
    selector: 'app-controls-bar',
    templateUrl: './controls-bar.component.html',
    styleUrls: ['./controls-bar.component.css'],
})
export class ControlsBarComponent {
    @Input() pageSizeValue!: WritableSignal<number>;
    @Input() sortOrderValue!: WritableSignal<string>;

    onPageSizeChange(event: Event) {
        const value = +(event.target as HTMLSelectElement).value;
        this.pageSizeValue.set(value);
    }

    onSortOrderChange(event: Event) {
        const value = (event.target as HTMLSelectElement).value;
        this.sortOrderValue.set(value);
    }
}
