import {Component} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {minNumberValidator} from '../../directive/validator/min-number.directive';
import {maxNumberValidator} from '../../directive/validator/max-number.directive';
import {TransportProfile} from '../../model/transport-profile';

@Component({
    selector: 'transport-profile-form',
    templateUrl: './transport-profile.form.html',
})
export class TransportProfileForm {
    transportProfileForm: FormGroup;
    constructor(private fb: FormBuilder) {
        this.createForm();
    }
    createForm() {
        this.transportProfileForm = this.fb.group({
            name: ['', [Validators.required, Validators.maxLength(100)]],
            southWestLat: ['', [Validators.required, minNumberValidator(-90), maxNumberValidator(90)]],
            southWestLon: ['', [Validators.required, minNumberValidator(-180), maxNumberValidator(180)]],
            northEastLat: ['', [Validators.required, minNumberValidator(-90), maxNumberValidator(90)]],
            northEastLon: ['', [Validators.required, minNumberValidator(-180), maxNumberValidator(180)]],
            initialZoom: ['', [Validators.required, minNumberValidator(0), maxNumberValidator(19)]],
            minZoom: ['', [Validators.required, minNumberValidator(0), maxNumberValidator(19)]]
        });
    }
}
