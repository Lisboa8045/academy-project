import {Component, Input} from '@angular/core';

@Component({
    selector: 'app-controls-bar',
    templateUrl: './controls-bar.component.html',
    styleUrls: ['./controls-bar.component.css'],
})
export class ControlsBarComponent {
    @Input() pageSize!: { (): number; set: (value: number) => void };
    @Input() sortOrder!: { (): string; set: (value: string) => void };
    @Input() onChange!: () => void;

    onPageSizeChange(event: Event) {
        const target = event.target as HTMLSelectElement;
        this.pageSize.set(Number(target.value));
        this.onChange();
    }

    onSortOrderChange(event: Event) {
        const target = event.target as HTMLSelectElement;
        this.sortOrder.set(target.value);
        this.onChange();
    }
}
