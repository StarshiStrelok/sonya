import {Directive, OnChanges, Input, SimpleChanges} from '@angular/core';
import {NG_VALIDATORS, AbstractControl, Validator, Validators, ValidatorFn} from '@angular/forms';

@Directive({
    selector: '[maxNumber]',
    providers: [
        {provide: NG_VALIDATORS, useExisting: MaxNumberDirective, multi: true}
    ]
})
export class MaxNumberDirective implements Validator, OnChanges {
    @Input() maxNumber: number;
    private valFn = Validators.nullValidator;
    constructor() {}
    ngOnChanges(changes: SimpleChanges): void {
        const change = changes['maxNumber'];
        if (change) {
            const limit: number = change.currentValue;
            this.valFn = maxNumberValidator(limit);
        } else {
            this.valFn = Validators.nullValidator;
        }
    }
    validate(control: AbstractControl): {[key: string]: any} {
        return this.valFn(control);
    }
}
export function maxNumberValidator(limit: number): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} => {
        const val = control.value;
        if (!val) {     // skip NaN
            return null;
        }
        const isValid = val <= limit
        return isValid ? null : {'maxNumber': {val}};
    };
}
