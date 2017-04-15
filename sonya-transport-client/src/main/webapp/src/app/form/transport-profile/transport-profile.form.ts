import {Component} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';

import {minNumberValidator} from '../../lib/module/validator/min-number.directive';
import {maxNumberValidator} from '../../lib/module/validator/max-number.directive';
import {TransportProfile} from '../../model/transport-profile';
import {DataService} from '../../service/data.service';

@Component({
    selector: 'transport-profile-form',
    templateUrl: './transport-profile.form.html',
})
export class TransportProfileForm {
    transportProfileForm: FormGroup;
    profile: TransportProfile = new TransportProfile(null, null, null, null,
    null, null, null, null, null, null);
    constructor(private fb: FormBuilder, private dataService: DataService,
        private router: Router) {
        this.createForm();
    }
    createForm() {
        this.transportProfileForm = this.fb.group({
            id: [''],
            name: ['', [Validators.required, Validators.maxLength(100)]],
            southWestLat: ['', [Validators.required, minNumberValidator(-90), maxNumberValidator(90)]],
            southWestLon: ['', [Validators.required, minNumberValidator(-180), maxNumberValidator(180)]],
            northEastLat: ['', [Validators.required, minNumberValidator(-90), maxNumberValidator(90)]],
            northEastLon: ['', [Validators.required, minNumberValidator(-180), maxNumberValidator(180)]],
            initialZoom: ['', [Validators.required, minNumberValidator(0), maxNumberValidator(19)]],
            minZoom: ['', [Validators.required, minNumberValidator(0), maxNumberValidator(19)]],
            centerLat: ['', [Validators.required, minNumberValidator(-90), maxNumberValidator(90)]],
            centerLon: ['', [Validators.required, minNumberValidator(-180), maxNumberValidator(180)]]
        });
        this.transportProfileForm.setValue(this.profile);
    }
    goBack() {
        this.router.navigate(['/ui/admin/profile-list']);
    }
    onSubmit() {
        if (!this.transportProfileForm.valid) {
            return;
        }
        let values = this.transportProfileForm.value;
        Object.getOwnPropertyNames(values).map((key: string) => 
            this.profile[key] = values[key]);
        this.dataService.create(this.profile).then(profile =>
            this.router.navigate(['/ui/admin/profile-list'])
        );
    }
}
