import {Component, forwardRef, Input} from '@angular/core';
import {NG_VALUE_ACCESSOR, ControlValueAccessor} from '@angular/forms'

@Component({
    selector: 'textfield',
    templateUrl: './textfield.component.html',
    styleUrls: ['./textfield.component.css'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => TextfieldComponent),
        multi: true
    }]
})
export class TextfieldComponent implements ControlValueAccessor {
    private id: string = "textfield-" + (Math.random() * (100000 - 1) + 1).toFixed(0);
    @Input() label: string;
    _value: any = '';
    get value(): any {
        return this._value;
    };
    set value(v: any) {
        if (v !== this._value) {
            this._value = v;
            this.onChange(v);
        }
    }
    onChange = (_: any) => {};
    onTouched = () => {};
    registerOnChange(fn: (_: any) => void): void {this.onChange = fn;}
    registerOnTouched(fn: () => void): void {this.onTouched = fn;}
    writeValue(value: any) {
        this._value = value;
        this.onChange(value);
    }
}
