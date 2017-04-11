import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';

@Component({
    selector: 's-input-number',
    templateUrl: './number.component.html',
    styleUrls: ['./number.component.css']
})
export class NumberComponent implements OnInit {
    constructor() {}
    ngOnInit() {
    }
    @Input() label: string
    @Input() field: number;
    @Output() fieldChange = new EventEmitter();
    change(newValue: number) {
        this.field = newValue;
        this.fieldChange.emit(newValue);
    }
    @Input() required: boolean;
    @Input() max: number;
    @Input() min: number;
}
