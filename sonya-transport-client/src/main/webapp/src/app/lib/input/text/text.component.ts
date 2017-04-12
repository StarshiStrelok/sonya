import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';

@Component({
    selector: 's-input-text',
    templateUrl: './text.component.html',
    styleUrls: ['./text.component.css']
})
export class TextComponent implements OnInit {
    constructor() {}
    ngOnInit() {
    }
    @Input() label: string;
    @Input() fieldName: string;
    id: string = "s-text-" + (Math.random() * (100000 - 1) + 1).toFixed(0);
    @Input() field: string;
    @Input() maxlength: number;
    @Input() minlength: number;
    @Output() fieldChange = new EventEmitter();
    change(newValue: string) {
        this.field = newValue;
        this.fieldChange.emit(newValue);
    }
    @Input() required: boolean;
}
