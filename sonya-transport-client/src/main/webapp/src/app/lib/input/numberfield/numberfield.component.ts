import {Component, forwardRef, Input} from '@angular/core';
import {NG_VALUE_ACCESSOR, FormControl} from '@angular/forms'
import {ValueAccessorBase} from '../../common/value-accessor-base';

@Component({
    selector: 'numberfield',
    templateUrl: './numberfield.component.html',
    styleUrls: ['./numberfield.component.css'],
    providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => NumberfieldComponent),
        multi: true
    }]
})
export class NumberfieldComponent extends ValueAccessorBase<number> {
    id: string = "numberfield-" + (Math.random() * (100000 - 1) + 1).toFixed(0);
    @Input() label: string;
    @Input() control: FormControl;
    @Input() placeholder: string;
}
