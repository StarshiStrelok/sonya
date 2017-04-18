import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Params} from '@angular/router';

import {minNumberValidator} from '../lib/validator/min-number.directive';
import {maxNumberValidator} from '../lib/validator/max-number.directive';
import {TransportProfile} from '../model/transport-profile';
import {DataService} from '../service/data.service';
import {ModelClass} from '../model/abs.model';

@Component({
    selector: 'transport-profile-form',
    templateUrl: './transport-profile.form.html',
})
export class TransportProfileForm implements OnInit {
    transportProfileForm: FormGroup;
    profile: TransportProfile = new TransportProfile(null, null, null, null,
        null, null, null, null, null, null, []);
    routeProfiles: FormGroup[];
    routeProfilesNames: string[];
    constructor(
        private fb: FormBuilder,
        private dataService: DataService,
        private location: Location,
        private activatedRoute: ActivatedRoute
    ) {}
    ngOnInit() {
        this.activatedRoute.params.subscribe((params: Params) => {
            let id = params['id'];
            if (id) {
                this.dataService.findById<TransportProfile>(
                    id, ModelClass.TRANSPORT_PROFILE).then(profile => {
                        this.profile = profile;
                        this.createForm();
                    });
            }
            this.createForm();
        });
    }
    createForm() {
        this.routeProfiles = [];
        this.routeProfilesNames = [];
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
        let _routeProfiles = this.profile.routeProfiles;
        delete this.profile.routeProfiles;
        this.transportProfileForm.setValue(this.profile);
        _routeProfiles.forEach((profile) => {
            let rpGroup = this.addRouteProfileGroup();
            rpGroup.setValue(profile);
        });
    }
    addRouteProfileGroup(): FormGroup {
        let groupName = 'routeProfile' + this.routeProfiles.length;
        console.log('add route profile [' + groupName + ']');
        let routeGroup: FormGroup = this.fb.group({
            id: [''],
            name: ['', [Validators.required, Validators.maxLength(30)]],
            avgSpeed: ['', [Validators.required, minNumberValidator(1), maxNumberValidator(500)]]
        });
        this.transportProfileForm.addControl(groupName, routeGroup);
        this.routeProfiles.push(routeGroup);
        this.routeProfilesNames.push(groupName);
        return routeGroup;
    }
    removeRouteProfileGroup(group: FormGroup) {
        var index = this.routeProfiles.indexOf(group, 0);
        if (index > -1) {
            let groupName = this.routeProfilesNames[index];
            this.transportProfileForm.removeControl(groupName);
            this.routeProfiles.splice(index, 1);
            this.routeProfilesNames.slice(index, 1);
        }
    }
    goBack() {
        this.location.back();
    }
    onSubmit() {
        if (!this.transportProfileForm.valid) {
            return;
        }
        let values = this.transportProfileForm.value;
        let _routeProfiles: any[] = [];
        Object.getOwnPropertyNames(values).map(
            (key: string) => {
                if (key.indexOf('routeProfile') != -1) {
                    _routeProfiles.push(values[key]);
                } else {
                    this.profile[key] = values[key];
                }
            }
        );
        this.profile.routeProfiles = _routeProfiles;
        console.log(JSON.stringify(this.profile));
        if (this.profile.id) {
            this.dataService.update<TransportProfile>(this.profile, ModelClass.TRANSPORT_PROFILE)
                .then(profile => this.goBack());
        } else {
            this.dataService.create<TransportProfile>(this.profile, ModelClass.TRANSPORT_PROFILE)
                .then(profile => this.goBack());
        }
    }
}