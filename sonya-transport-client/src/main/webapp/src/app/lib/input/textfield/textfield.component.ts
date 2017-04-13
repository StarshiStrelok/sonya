import {Component, forwardRef, Input} from '@angular/core';
import {NG_VALUE_ACCESSOR, FormControl} from '@angular/forms'
import {ValueAccessorBase} from '../../common/value-accessor-base';

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
export class TextfieldComponent extends ValueAccessorBase<string> {
    id: string = "textfield-" + (Math.random() * (100000 - 1) + 1).toFixed(0);
    @Input() label: string;
    @Input() control: FormControl;
    @Input() placeholder: string;
}
