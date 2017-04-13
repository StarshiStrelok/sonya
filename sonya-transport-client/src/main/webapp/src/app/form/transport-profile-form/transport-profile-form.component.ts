import {Component} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {minNumberValidator} from '../../directive/validator/min-number.directive';
import {maxNumberValidator} from '../../directive/validator/max-number.directive';
import {TransportProfile} from '../../model/transport-profile';

@Component({
    selector: 'transport-profile-form-r',
    templateUrl: './transport-profile-form.component.html',
    styleUrls: ['./transport-profile-form.component.css']
})
export class TransportProfileFormComponent {
    transportProfileForm: FormGroup;
    constructor(private fb: FormBuilder) {
        this.createForm();
    }
    createForm() {
        this.transportProfileForm = this.fb.group({
            //name: ['', [Validators.required, Validators.maxLength(100)]],
            name: ['22', [Validators.required]]
//            southWestLat: ['', [Validators.required, minNumberValidator(-90), maxNumberValidator(90)]],
//            southWestLon: [''],
//            northEastLat: [''],
//            northEastLon: ['']
        });
//        this.transportProfileForm.valueChanges
//            .subscribe(data => this.onValueChanged(data));
//        this.onValueChanged();
    }
//    onValueChanged(data?: any) {
//        if (!this.transportProfileForm) {return;}
//        const form = this.transportProfileForm;
//        for (const field in this.formErrors) {
//            // clear previous error message (if any)
//            this.formErrors[field] = '';
//            const control = form.get(field);
//            if (control && control.dirty && !control.valid) {
//                const messages = this.validationMessages[field];
//                for (const key in control.errors) {
//                    this.formErrors[field] += messages[key] + ' ';
//                }
//            }
//        }
//    }
//    formErrors: any = {
//        'name': '',
//        'southWestLat': ''
//    };
//    validationMessages: any = {
//        'name': {
//            'required': 'Name is required.',
//            'maxlength': 'Profile name cannot be more than 100 characters long.'
//        },
//        'southWestLat': {
//            'required': 'Latutude required',
//            'maxNumber': 'Must be less than 90',
//            'minNumber': 'Must be more than -90'
//        }
//    };
}
