import {Directive, OnChanges, Input, SimpleChanges} from '@angular/core';
import {NG_VALIDATORS, AbstractControl, Validator, Validators, ValidatorFn} from '@angular/forms';

@Directive({
    selector: '[minNumber]',
    providers: [
        {provide: NG_VALIDATORS, useExisting: MinNumberDirective, multi: true}
    ]
})
export class MinNumberDirective implements Validator, OnChanges {
    @Input() minNumber: number;
    private valFn = Validators.nullValidator;
    constructor() {}
    ngOnChanges(changes: SimpleChanges): void {
        const change = changes['minNumber'];
        if (change) {
            const limit: number = change.currentValue;
            this.valFn = minNumberValidator(limit);
        } else {
            this.valFn = Validators.nullValidator;
        }
    }
    validate(control: AbstractControl): {[key: string]: any} {
        return this.valFn(control);
    }
}
export function minNumberValidator(limit: number): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} => {
        const val = control.value;
        if (!val) {     // skip NaN
            return null;
        }
        const isValid = val >= limit
        return isValid ? null : {'minNumber': {val}};
    };
}
