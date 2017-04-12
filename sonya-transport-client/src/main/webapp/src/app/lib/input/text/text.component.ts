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
    @Input() id: string;
    @Input() field: number;
    @Input() maxlength: number;
    @Input() minlength: number;
    @Output() fieldChange = new EventEmitter();
    change(newValue: number) {
        this.field = newValue;
        this.fieldChange.emit(newValue);
    }
    @Input() required: boolean;
}
