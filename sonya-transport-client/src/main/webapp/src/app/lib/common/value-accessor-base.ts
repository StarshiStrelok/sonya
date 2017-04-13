import {ControlValueAccessor, FormControl} from '@angular/forms';


export abstract class ValueAccessorBase<T> implements ControlValueAccessor {
    private innerValue: T;

    private changed = new Array<(value: T) => void>();
    private touched = new Array<() => void>();

    get value(): T {
        return this.innerValue;
    }

    set value(value: T) {
        if (this.innerValue !== value) {
            this.innerValue = value;
            this.changed.forEach(f => f(value));
        }
    }

    touch() {
        this.touched.forEach(f => f());
    }

    writeValue(value: T) {
        this.innerValue = value;
    }

    registerOnChange(fn: (value: T) => void) {
        this.changed.push(fn);
    }

    registerOnTouched(fn: () => void) {
        this.touched.push(fn);
    }
    // bootstrap-dependent 
    validationClassBlock(control: FormControl) {
        let flag = this.isValid(control);
        return flag === null ? '' : (flag ? 'has-success' : 'has-danger')
    }
    validationClassControl(control: FormControl) {
        let flag = this.isValid(control);
        return flag === null ? '' : (flag ? 'form-control-success' : 'form-control-danger')
    }
    private isValid(control: FormControl): boolean {
        if (!control.valid && (!control.pristine || !control.untouched)) {
            return false;
        } else if (control.valid && !control.pristine) {
            return true;
        } else {
            return null;
        }
    }
}